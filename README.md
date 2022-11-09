# Sweater

project for learning Spring Boot

### DB preparation

    $ docker pull postgres
    $ docker run --name postgres -e POSTGRES_DB=sweater -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=asdasd123 -p 54321:5432 postgres
