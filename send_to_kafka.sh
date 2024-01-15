#!/usr/bin/env sh

curl -X POST --data 'key=exampleKey&slow=true&blocking=true' '127.0.0.1:8080/kafka/topic/example_topic' -v
