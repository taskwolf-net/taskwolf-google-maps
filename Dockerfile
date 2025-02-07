FROM alpine

COPY /build/libs/google-maps-1.0.0-SNAPSHOT.jar google-maps.jar
COPY /locale/ /locale/