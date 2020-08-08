# ENV set FIREBASE_CONFIG, MONGODB_DBNAME, MONGODB_URI, PRIVACY_URI, TERMS_URI
FROM openjdk:8-jdk-alpine
WORKDIR /srv/app
COPY build/bin/ffc-api.jar .
EXPOSE 8080
CMD java -server -Xmx512m -Xss512k -Dfile.encoding=UTF-8 -jar /srv/app/ffc-api.jar -port 8080
