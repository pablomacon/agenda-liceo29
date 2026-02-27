# Etapa 1: Construcción (Build)
# Usamos una imagen de Maven con Java 21 para compilar el código
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Damos permisos de ejecución al wrapper de Maven y compilamos el .jar
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Etapa 2: Ejecución (Runtime)
# Usamos una imagen ligera de Java 21 solo para correr la aplicación
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copiamos solo el archivo .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]