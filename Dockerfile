# Build + execução do back-end Java (usado no deploy: Railway, Render, Fly.io...)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B compile dependency:copy-dependencies -DoutputDirectory=target/dependency

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/classes ./classes
COPY --from=build /app/target/dependency ./lib
# PORT é injetada pela plataforma; ORACLE_URL/ORACLE_USUARIO/ORACLE_SENHA
# devem ser configuradas nas variáveis de ambiente do serviço.
EXPOSE 8080
CMD ["java", "-cp", "classes:lib/*", "api.ServidorApi"]
