#!/bin/bash

case "$1" in
start)  echo "[neo4j-manager] starting Neo4J"
        {{ neo4j_home }}/bin/neo4j start
        echo "[neo4j-manager] waiting Neo4J to warm up (max: {{ neo4j_start_wait }} seconds)"
        end="$(( SECONDS + {{ neo4j_start_wait }} ))"
        while true; do
            [[ "200" = "$(curl --silent --write-out %{http_code} --output /dev/null http://localhost:{{ neo4j_http_port }})" ]] && break
            [[ "${SECONDS}" -ge "${end}" ]] && exit 1
            sleep "{{ neo4j_start_pool }}"
        done
        echo "[neo4j-manager] Neo4J started"
        ;;
stop)   echo "[neo4j-manager] stopping Neo4J..."
        {{ neo4j_home }}/bin/neo4j stop
        echo "[neo4j-manager] Neo4J stopped"
        ;;
restart) echo "[neo4j-manager] restarting Neo4J..."
        service neo4j stop
        service neo4j start
        echo "[neo4j-manager] Neo4J restarted"
        ;;
reload|force-reload) echo "[neo4j-manager] Not yet implemented"
        ;;
*)      echo "Usage: neo4j.sh {start|stop|restart|reload|force-reload}"
        exit 2
        ;;
esac
exit 0
