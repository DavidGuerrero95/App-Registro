FROM openjdk:12
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} Registro.jar
ENTRYPOINT ["java","-jar","/Registro.jar"]