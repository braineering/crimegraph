#!/bin/bash
### BEGIN INIT INFO
# Provides:          neo4j.sh
# Required-Start:
# Required-Stop:
# Should-Start:
# Should-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Neo4J service
# Description:       Neo4J service
### END INIT INFO

case "$1" in
start)  echo "[service] starting Neo4J" >&2
        {{ neo4j_home }}/bin/neo4j start
        echo "[service] waiting Neo4J to warm up (max: {{ neo4j_start_wait }} seconds)" >&2
        end="$(( SECONDS + {{ neo4j_start_wait }} ))"
        while true; do
            [[ "200" = "$(curl --silent --write-out %{http_code} --output /dev/null http://localhost:{{ neo4j_http_port }})" ]] && break
            [[ "${SECONDS}" -ge "${end}" ]] && exit 1
            sleep "{{ neo4j_start_pool }}"
        done
        echo "[service] Neo4J started" >&2
        ;;
stop)   echo "[service] stopping Neo4J..." >&2
        {{ neo4j_home }}/bin/neo4j stop
        echo "[service] Neo4J stopped" >&2
        ;;
restart) echo "[service] restarting Neo4J..." >&2
        service neo4j stop
        service neo4j start
        echo "[service] Neo4J restarted" >&2
        ;;
reload|force-reload) echo "Not yet implemented" >&2
        ;;
*)      echo "Usage: /etc/init.d/neo4j.sh {start|stop|restart|reload|force-reload}" >&2
        exit 2
        ;;
esac
exit 0
