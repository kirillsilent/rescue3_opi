FROM openjdk:14-alpine
COPY target/rescue-*.jar rescue.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "rescue.jar"]