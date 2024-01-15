
Запускаем rabbit и kafka
```
docker-compose up -d
```

Инициализируем rabbit
```
./rabbit_schema_initialize.sh
```

Инициализируем kafka
```
./kafka_schema_initialize.sh
```

Запускаем приложение
```
mvn clean package
mvn spring-boot:run
```

Пробуем отправить сообщения в rabbit и kafka через наше приложение
```
./send_to_rabbit.sh
./send_to_kafka.sh
```
