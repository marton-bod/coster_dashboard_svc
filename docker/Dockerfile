FROM adoptopenjdk/openjdk11:alpine-slim
ADD /dashboard_svc-*.jar /dashboard_svc.jar
ARG JAVA_ARGS
ENTRYPOINT ["sh", "-c", "java $JAVA_ARGS -jar dashboard_svc.jar"]
