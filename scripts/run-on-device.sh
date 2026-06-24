#!/bin/bash
# Droidvisor 运行配置脚本
# 功能: 构建debug APK → 安装到设备 → 授予AVF权限 → 启动应用
# 用法: ./scripts/run-on-device.sh [options]
#   --skip-build    跳过构建步骤（使用已有APK）
#   --verify-only   仅验证环境，不执行部署

set -e  # 遇错即停

# 配置常量
APP_ID="com.droidvisor"
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
PERMISSION="android.permission.MANAGE_VIRTUAL_MACHINE"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() { echo -e "${GREEN}✅ $1${NC}"; }
log_warn() { echo -e "${YELLOW}⚠️  $1${NC}"; }
log_error() { echo -e "${RED}❌ $1${NC}"; }

# 参数解析
SKIP_BUILD=false
VERIFY_ONLY=false

for arg in "$@"; do
    case $arg in
        --skip-build) SKIP_BUILD=true ;;
        --verify-only) VERIFY_ONLY=true ;;
        *) log_error "Unknown option: $arg"; exit 1 ;;
    esac
done

# Step 0: 环境检查
check_environment() {
    echo "🔍 Checking environment..."

    # 检查ADB
    if ! command -v adb &> /dev/null; then
        log_error "ADB not found. Please install Android SDK Platform Tools."
        exit 1
    fi

    # 检查设备连接
    local devices=$(adb devices | grep -v "List" | grep "device$" | wc -l)
    if [ "$devices" -eq 0 ]; then
        log_error "No ADB device connected."
        exit 1
    fi

    local device_serial=$(adb devices | grep "device$" | head -1 | cut -f1)
    log_info "Device connected: $device_serial"
}

# Step 1: 构建APK
build_apk() {
    if [ "$SKIP_BUILD" = true ]; then
        log_warn "Skipping build (using existing APK)"
        return
    fi

    echo "📦 Building debug APK..."
    ./gradlew assembleDebug

    if [ ! -f "$APK_PATH" ]; then
        log_error "APK not found at $APK_PATH"
        exit 1
    fi

    local size=$(du -h "$APK_PATH" | cut -f1)
    log_info "APK built successfully ($size)"
}

# Step 2: 安装APK
install_apk() {
    echo "📲 Installing APK to device..."
    adb install -r "$APK_PATH"
    log_info "APK installed successfully"
}

# Step 3: 授予AVF权限
grant_permission() {
    echo "🔐 Granting MANAGE_VIRTUAL_MACHINE permission..."
    adb shell pm grant "$APP_ID" "$PERMISSION"

    # 验证权限授予
    local granted=$(adb shell dumpsys package "$APP_ID" | grep "$PERMISSION")
    if [ -z "$granted" ]; then
        log_warn "Permission verification failed (may need root)"
    else
        log_info "Permission granted and verified"
    fi
}

# Step 4: 启动应用
launch_app() {
    echo "🚀 Launching app..."
    adb shell am start -n "$APP_ID/.MainActivity"
    log_info "App launched successfully"
}

# ========== 主程序 ==========
main() {
    echo ""
    echo "========================================"
    echo "  Droidvisor 运行配置脚本"
    echo "  Build → Install → Grant → Launch"
    echo "========================================"
    echo ""

    # 环境检查
    check_environment

    # 如果仅验证模式，退出
    if [ "$VERIFY_ONLY" = true ]; then
        log_info "Environment verification complete."
        exit 0
    fi

    # 执行部署流程
    build_apk
    install_apk
    grant_permission
    launch_app

    echo ""
    echo "========================================"
    log_info "🎉 Deployment completed successfully!"
    echo "========================================"
}

main "$@"