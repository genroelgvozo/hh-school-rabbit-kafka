#!/usr/bin/env sh

user=guest
password=guest
host=127.0.0.1
port=15672

curl -i -u $user:$password -H "content-type:application/json" -XPUT -d'{"durable":true}' http://$host:$port/api/queues/%2f/queue_1
curl -i -u $user:$password -H "content-type:application/json" -XPUT -d'{"durable":true}' http://$host:$port/api/queues/%2f/queue_2
curl -i -u $user:$password -H "content-type:application/json" -XPUT -d'{"durable":true}' http://$host:$port/api/queues/%2f/queue_3

curl -i -u $user:$password -H "content-type:application/json" -XPUT -d'{"type":"fanout","auto_delete":false,"durable":true,"internal":false,"arguments":{}}' http://$host:$port/api/exchanges/%2f/fanout
curl -i -u $user:$password -H "content-type:application/json" -XPOST http://$host:$port/api/bindings/%2f/e/fanout/q/queue_1
curl -i -u $user:$password -H "content-type:application/json" -XPOST http://$host:$port/api/bindings/%2f/e/fanout/q/queue_2
curl -i -u $user:$password -H "content-type:application/json" -XPOST http://$host:$port/api/bindings/%2f/e/fanout/q/queue_3

curl -i -u $user:$password -H "content-type:application/json" -XPUT -d'{"type":"direct","auto_delete":false,"durable":true,"internal":false,"arguments":{}}' http://$host:$port/api/exchanges/%2f/direct
curl -i -u $user:$password -H "content-type:application/json" -XPOST -d'{"routing_key":"to_queue_1"}' http://$host:$port/api/bindings/%2f/e/direct/q/queue_1
curl -i -u $user:$password -H "content-type:application/json" -XPOST -d'{"routing_key":"to_queue_2"}' http://$host:$port/api/bindings/%2f/e/direct/q/queue_2
curl -i -u $user:$password -H "content-type:application/json" -XPOST -d'{"routing_key":"to_queue_3"}' http://$host:$port/api/bindings/%2f/e/direct/q/queue_3
curl -i -u $user:$password -H "content-type:application/json" -XPOST -d'{"routing_key":"to_queue_1_and_3"}' http://$host:$port/api/bindings/%2f/e/direct/q/queue_1
curl -i -u $user:$password -H "content-type:application/json" -XPOST -d'{"routing_key":"to_queue_1_and_3"}' http://$host:$port/api/bindings/%2f/e/direct/q/queue_3
