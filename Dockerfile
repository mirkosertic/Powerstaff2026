FROM eclipse-temurin:25
LABEL org.opencontainers.image.source=https://github.com/mirkosertic/Powerstaff2026
LABEL org.opencontainers.image.description="Powerstaff 2026"
LABEL org.opencontainers.image.licenses=Apache-2.0
LABEL org.opencontainers.image.authors="Mirko Sertic <mirko.sertic@web.de>"
ENV JAVA_OPTS="-Xmx2g"
COPY ./target/powerstaff-1.0-SNAPSHOT.jar /tmp
COPY ./entrypoint.sh /tmp
WORKDIR /tmp
EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]