FROM openjdk:17-oracle

LABEL maintainer="dobryagin.mihail12@mail.ru"

RUN mkdir -p app
WORKDIR /app
COPY ./ .
RUN rm -rf .idea .gradle build .env
RUN bash gradlew build

CMD ["bash", "gradlew", "run"]
