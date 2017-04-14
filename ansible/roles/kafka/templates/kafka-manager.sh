#!/bin/bash

case "$1" in
start)  echo "[kafka-manager] starting kafka"
        {{ kafka_home }}/bin/zookeeper-server-start.sh {{ kafka_home }}/config/zookeeper.properties > /dev/null &
        sleep 3
        {{ kafka_home }}/bin/kafka-server-start.sh -daemon {{ kafka_home }}/config/server.properties
        echo "[kafka-manager] kafka started"
        ;;
stop)   echo "[kafka-manager] stopping kafka..."
        {{ kafka_home }}/bin/kafka-server-stop.sh
        {{ kafka_home }}/bin/zookeeper-server-stop.sh
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
