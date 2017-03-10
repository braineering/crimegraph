#!/bin/bash
### BEGIN INIT INFO
# Provides:          flink.sh
# Required-Start:
# Required-Stop:
# Should-Start:
# Should-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Apache Flink service
# Description:       Apache Flink service
### END INIT INFO

case "$1" in
start)  echo "[service] starting Apache Flink" >&2
        {{ flink_home }}/bin/start-local.sh
        echo "[service] Apache Flink started" >&2
        ;;
stop)   echo "[service] stopping Apache Flink..." >&2
        {{ flink_home }}/bin/stop-local.sh
        echo "[service] Apache Flink stopped" >&2
        ;;
restart) echo "[service] restarting Apache Flink..." >&2
        {{ flink_home }}/bin/stop-local.sh
        {{ flink_home }}/bin/start-local.sh
        echo "[service] Apache Flink restarted" >&2
        ;;
reload|force-reload) echo "Not yet implemented" >&2
        ;;
*)      echo "Usage: /etc/init.d/flink.sh {start|stop|restart|reload|force-reload}" >&2
        exit 2
        ;;
esac
exit 0
