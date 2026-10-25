# Etapa 1: compilación con Maven Wrapper (Java 21)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src/ src/
RUN ./mvnw -q -B -DskipTests package

# Etapa 2: ejecución con JRE mínimo y usuario sin privilegios
FROM eclipse-temurin:21-jre
RUN useradd --system --create-home --uid 10001 huertos \
    && mkdir -p /datos/evidencias && chown -R huertos /datos
WORKDIR /app
COPY --from=build /app/target/huertos-medicinales-*.jar app.jar
USER huertos
ENV EVIDENCIAS_DIR=/datos/evidencias
VOLUME /datos/evidencias
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
