#!/usr/bin/env sh

docker exec hh-school-rabbit-kafka-kafka-1 \
  /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic example_topic --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3
