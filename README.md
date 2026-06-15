# hm-finnhjelpemiddel-news

## Running hm-finnhjelpemiddel-news locally:

```
cd hm-finnhjelpemiddel-news
docker-compose up -d

export DB_DRIVER=org.postgresql.Driver
export DB_JDBC_URL=jdbc:postgresql://localhost:5435/finnhjelpemiddelnews
export SERVER_PORT=1338

./gradlew build run
```