# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Run stage ----------
FROM eclipse-temurin:17-jre
# install tesseract (English)
RUN apt-get update && apt-get install -y tesseract-ocr tesseract-ocr-eng && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata
ENTRYPOINT ["java","-jar","/app/app.jar"]
