#!/bin/bash

case "$1" in
start)  echo "[flink-manager] starting Apache Flink"
        {{ flink_home }}/bin/start-local.sh
        echo "[flink-manager] waiting Apache Flink to warm up (max: {{ flink_start_wait }} seconds)"
        end="$(( SECONDS + {{ flink_start_wait }} ))"
        while true; do
            [[ "200" = "$(curl --silent --write-out %{http_code} --output /dev/null http://localhost:{{ flink_port }})" ]] && break
            [[ "${SECONDS}" -ge "${end}" ]] && exit 1
            sleep "{{ flink_start_pool }}"
        done
        echo "[flink-manager] Apache Flink started"
        ;;
stop)   echo "[flink-manager] stopping Apache Flink..."
        {{ flink_home }}/bin/stop-local.sh
        echo "[flink-manager] Apache Flink stopped"
        ;;
restart) echo "[flink-manager] restarting Apache Flink..."
        {{ flink_home }}/bin/stop-local.sh
        {{ flink_home }}/bin/start-local.sh
        echo "[flink-manager] Apache Flink restarted"
        ;;
reload|force-reload) echo "[flink-manager] Not yet implemented"
        ;;
*)      echo "Usage: flink.sh {start|stop|restart|reload|force-reload}"
        exit 2
        ;;
esac
exit 0
