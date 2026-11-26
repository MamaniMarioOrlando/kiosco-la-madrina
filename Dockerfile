# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy

# Usuario sin privilegios: si alguien llegara a ejecutar código en el backend, no sería root dentro del
# contenedor (no podría instalar paquetes, tocar archivos del sistema ni aprovechar fallas del runtime).
# UID numérico fijo: así la plataforma puede comprobar que no es 0 (p. ej. runAsNonRoot en Kubernetes).
RUN groupadd --system --gid 10001 kiosco \
    && useradd --system --uid 10001 --gid kiosco --no-create-home --shell /usr/sbin/nologin kiosco

WORKDIR /app
# El .jar queda de root y solo de lectura para la app: el proceso no puede reemplazar su propio código.
COPY --from=build /app/target/*.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
