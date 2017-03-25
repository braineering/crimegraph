#!/bin/bash

case "$1" in
start)  echo "[kafka-manager] starting kafka"
        {{ kafka_home }}/bin/zookeeper-server-start.sh {{ kafka_home }}/config/zookeeper.properties > /dev/null &
        sleep 10
        {{ kafka_home }}/bin/kafka-server-start.sh -daemon {{ kafka_home }}/config/server.properties
        if [[ "main-topic" != "$({{ kafka_home }}/bin/kafka-topics.sh --list --zookeeper localhost:{{ kafka_http_port }})" ]]; then
          {{ kafka_home }}/bin/kafka-topics.sh --create --topic {{ kafka_topic }} --zookeeper localhost:{{ kafka_zookeeper_port }} --partitions 1 --replication-factor 1
        fi
        echo "[kafka-manager] waiting kafka to warm up (max: {{ kafka_start_wait }} seconds)"
        end="$(( SECONDS + {{ kafka_start_wait }} ))"
        while true; do
            [[ "{{ kafka_topic }}" == "$({{ kafka_home }}/bin/kafka-topics.sh --list --zookeeper localhost:{{ kafka_http_port }})" ]] && break
            [[ "${SECONDS}" -ge "${end}" ]] && exit 1
            sleep "{{ kafka_start_pool }}"
        done
        echo "[kafka-manager] kafka started"
        ;;
stop)   echo "[kafka-manager] stopping kafka..."
        {{ kafka_home }}/bin/zookeeper-server-stop.sh
        echo "[kafka-manager] kafka stopped"
        ;;
restart) echo "[kafka-manager] restarting kafka..."
        service kafka stop
        service kafka start
        echo "[kafka-manager] kafka restarted"
        ;;
reload|force-reload) echo "[kafka-manager] Not yet implemented"
        ;;
*)      echo "Usage: kafka.sh {start|stop|restart|reload|force-reload}"
        exit 2
        ;;
esac
exit 0
