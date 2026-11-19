
# Сборка приложения
FROM maven:3.9-eclipse-temurin-17 AS build
LABEL authors="Gordon Freeman"
WORKDIR /app

# копируем зависимости проекта, кешируем зависимости
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

# сбор JAR
# выполняется при каждом изменении кода, но уже без скачивания зависимостей
RUN mvn -B package -Dmaven.test.skip=true

# Запуск приложения
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# копируем готовый JAR из этапа сборки
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
