# Stage 1: Build stage with Maven
FROM eclipse-temurin:21-jdk-alpine AS builder

# Installer Maven
RUN apk add --no-cache maven

WORKDIR /app

# Copier les fichiers de configuration Maven en premier (pour cache Docker)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Télécharger les dépendances (layer mise en cache si pom.xml ne change pas)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src/ src/

# Build de l'application (skip tests pour build Docker plus rapide)
RUN mvn clean package -DskipTests -B

# Extraire les layers du JAR pour optimisation Spring Boot
RUN java -Djarmode=layertools -jar target/vimoo-*.jar extract


# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

# Installer dumb-init pour gestion des signaux
RUN apk add --no-cache dumb-init

# Créer utilisateur non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Créer répertoires nécessaires
WORKDIR /app
RUN mkdir -p /app/logs /app/tmp && \
    chown -R appuser:appgroup /app


# Copier les layers extraites du JAR (optimisation Spring Boot)
COPY --from=builder --chown=appuser:appgroup /app/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /app/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/application/ ./


# Passer à l'utilisateur non-root
USER appuser

# Variables d'environnement pour optimisation JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.backgroundpreinitializer.ignore=true"

# Répertoire temporaire pour Spring Boot
ENV TMPDIR=/app/tmp

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

# Point d'entrée avec dumb-init
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]