# Sweater

project for learning Spring Boot

### DB preparation

    $ docker pull postgres
    $ docker run --name toy_twitter -e POSTGRES_DB=toy_twitter -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=asdasd123 -p 5432:5432 postgres
