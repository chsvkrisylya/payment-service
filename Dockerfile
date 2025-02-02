FROM openjdk:21-jdk

WORKDIR /payment-service

COPY build/libs/payment-service-0.0.1-SNAPSHOT.jar /payment-service/payment-service.jar

ENTRYPOINT ["java", "-jar", "/payment-service/payment-service.jar"]