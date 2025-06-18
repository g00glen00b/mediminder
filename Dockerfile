# Build layer
FROM amazoncorretto:21.0.7-alpine3.21 AS build
ENV MAVEN_USER_HOME=/app/.m2
ENV NPM_CONFIG_CACHE=/app/.npm
RUN adduser -D app
RUN apk add --no-cache libstdc++
WORKDIR /app
RUN mkdir -p /app/.npm
# Copy sources
ADD .mvn ./.mvn/
ADD mvnw .
ADD pom.xml .
ADD mediminder-api/ ./mediminder-api/
ADD mediminder-fe/ ./mediminder-fe/
# Set user
RUN chown -R app .
USER app
# Run build
RUN ./mvnw -Dmaven.repo.local=${MAVEN_USER_HOME}/repository clean package -Dmaven.test.skip

# Optimization layer
FROM amazoncorretto:21.0.7-alpine3.21 AS optimizer
RUN adduser -D app
WORKDIR /app
# Copy JAR
COPY --from=build /app/mediminder-api/target/*.jar mediminder.jar
# Set user
RUN chown -R app .
USER app
# Extract
RUN java -Djarmode=tools -jar mediminder.jar extract --layers --launcher

# JRE build layer
FROM amazoncorretto:21.0.7-alpine3.21 AS jre-builder
RUN adduser -D app
RUN apk add --no-cache binutils
WORKDIR /app
# Copy JAR + dependencies
COPY --from=optimizer /app/dependencies/ .
COPY --from=optimizer /app/mediminder.jar .
# Set user
RUN chown -R app .
USER app
# Build custom JRE
RUN jdeps \
    --ignore-missing-deps \
    -q \
    --recursive \
    --multi-release 21 \
    --print-module-deps \
    --class-path 'BOOT-INF/lib/*' \
    mediminder.jar > deps.info
RUN jlink \
    --add-modules $(cat deps.info) \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=zip-6 \
    --output /app/jre

# Application layer
FROM alpine:3.21
RUN adduser -D app
WORKDIR /app
VOLUME /tmp
ENV JAVA_HOME=/app/jre
ENV PATH "${JAVA_HOME}/bin:${PATH}"
# Copy the extracted JAR file contents
COPY --from=jre-builder /app/jre ./jre
COPY --from=optimizer /app/dependencies/ .
COPY --from=optimizer /app/spring-boot-loader/ .
COPY --from=optimizer /app/snapshot-dependencies/ .
COPY --from=optimizer /app/application/ .
# Set user
RUN chown app .
USER app
# Run application
EXPOSE 8080
ENTRYPOINT ["java", "-XX:TieredStopAtLevel=1", "org.springframework.boot.loader.launch.JarLauncher"]