#!/bin/bash
# ============================================
# QuickTap Server 生产环境启动脚本
# 服务器路径: /andre/QuickTap-Server/java/
# ============================================
# 使用方式：
#   ./start.sh           # 后台启动（默认，生产推荐）
#   ./start.sh foreground # 前台启动（调试用）
#   ./start.sh stop      # 停止服务
#   ./start.sh restart   # 重启服务
#   ./start.sh status    # 查看运行状态
# ============================================

# ==================== 配置 ====================
SERVER_PORT=8080
JAR_NAME="quicktap-server-1.0.0.jar"
WORK_DIR="/andre/QuickTap-Server/java"
ENV_FILE="$WORK_DIR/.env.prod"
PID_FILE="$WORK_DIR/quicktap.pid"
LOG_DIR="$WORK_DIR/logs"
LOG_FILE="$LOG_DIR/quicktap-server.log"
ERR_LOG_FILE="$LOG_DIR/quicktap-server.err.log"

# ==================== 检查 ====================
if [ ! -f "$WORK_DIR/$JAR_NAME" ]; then
    echo "[ERROR] 找不到 jar 包: $WORK_DIR/$JAR_NAME"
    exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
    echo "[ERROR] 找不到环境变量文件: $ENV_FILE"
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "[ERROR] 未找到 java 命令，请先安装 JDK 17"
    exit 1
fi

# ==================== 函数 ====================

load_env() {
    set -a
    source "$ENV_FILE"
    set +a
}

get_pid() {
    if [ -f "$PID_FILE" ]; then
        local pid=$(cat "$PID_FILE")
        if kill -0 "$pid" 2>/dev/null; then
            echo "$pid"
            return 0
        fi
    fi
    return 1
}

start_foreground() {
    if pid=$(get_pid); then
        echo "[WARN] 服务已在运行，PID: $pid"
        exit 1
    fi
    load_env
    mkdir -p "$LOG_DIR"
    echo "[INFO] 前台启动 QuickTap Server..."
    echo "[INFO] 端口: $SERVER_PORT"
    echo "[INFO] 日志: $LOG_FILE"
    exec java $JAVA_OPTS -jar "$WORK_DIR/$JAR_NAME" --server.port=$SERVER_PORT 2>&1 | tee -a "$LOG_FILE"
}

start_daemon() {
    if pid=$(get_pid); then
        echo "[WARN] 服务已在运行，PID: $pid"
        exit 1
    fi
    load_env
    mkdir -p "$LOG_DIR"
    echo "[INFO] 后台启动 QuickTap Server..."
    echo "[INFO] 端口: $SERVER_PORT"
    nohup java $JAVA_OPTS -jar "$WORK_DIR/$JAR_NAME" --server.port=$SERVER_PORT > "$LOG_FILE" 2> "$ERR_LOG_FILE" &
    echo $! > "$PID_FILE"
    sleep 5
    if pid=$(get_pid); then
        echo "[OK] 启动成功，PID: $pid"
        echo "[INFO] 日志: $LOG_FILE"
        echo "[INFO] 错误日志: $ERR_LOG_FILE"
        echo "[INFO] 查看日志: tail -f $LOG_FILE"
        echo "[INFO] 停止服务: $0 stop"
        echo "[INFO] 健康检查: curl http://localhost:$SERVER_PORT/api/health"
    else
        echo "[ERROR] 启动失败，请查看错误日志: $ERR_LOG_FILE"
        exit 1
    fi
}

stop() {
    if ! pid=$(get_pid); then
        echo "[WARN] 服务未运行"
        rm -f "$PID_FILE"
        exit 0
    fi
    echo "[INFO] 停止服务，PID: $pid"
    kill "$pid"
    for i in {1..30}; do
        if ! kill -0 "$pid" 2>/dev/null; then
            rm -f "$PID_FILE"
            echo "[OK] 已停止"
            exit 0
        fi
        sleep 1
    done
    echo "[WARN] 优雅停止超时，强制 kill -9"
    kill -9 "$pid" 2>/dev/null
    rm -f "$PID_FILE"
    echo "[OK] 已强制停止"
}

status() {
    if pid=$(get_pid); then
        echo "[OK] 服务运行中，PID: $pid"
        ps -p "$pid" -o pid,user,pcpu,pmem,etime,cmd
        echo ""
        echo "[INFO] 端口: $SERVER_PORT"
        echo "[INFO] 健康检查: curl http://localhost:$SERVER_PORT/api/health"
    else
        echo "[INFO] 服务未运行"
        exit 1
    fi
}

restart() {
    stop
    sleep 2
    start_daemon
}

# ==================== 入口 ====================
case "${1:-start}" in
    start)
        start_daemon
        ;;
    foreground)
        start_foreground
        ;;
    daemon)
        start_daemon
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    *)
        echo "用法: $0 {start|foreground|daemon|stop|restart|status}"
        echo ""
        echo "  start       后台启动（默认）"
        echo "  foreground  前台启动（调试用，日志直接输出）"
        echo "  stop        停止"
        echo "  restart     重启"
        echo "  status      查看状态"
        exit 1
        ;;
esac
