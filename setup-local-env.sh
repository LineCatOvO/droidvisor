#!/bin/bash

# 本地环境配置脚本
# 用于快速设置国内镜像源，不影响仓库原有配置

set -e

echo "=========================================="
echo "   本地开发环境配置工具"
echo "=========================================="
echo ""

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查操作系统
OS=$(uname -s)

echo -e "${GREEN}[1/4] 配置 Gradle 镜像...${NC}"
if [ ! -d "$HOME/.gradle" ]; then
    mkdir -p "$HOME/.gradle"
fi

if [ -f "init.gradle.kts" ]; then
    cp init.gradle.kts "$HOME/.gradle/init.gradle.kts"
    echo -e "${GREEN}   ✓ Gradle 初始化脚本已配置${NC}"
else
    echo -e "${YELLOW}   ⚠ init.gradle.kts 不存在，跳过${NC}"
fi

if [ -f "gradle.properties.local" ]; then
    if [ ! -f "gradle.properties" ]; then
        cp gradle.properties.local gradle.properties
        echo -e "${GREEN}   ✓ Gradle 属性配置已创建${NC}"
    else
        echo -e "${YELLOW}   ⚠ gradle.properties 已存在，跳过${NC}"
    fi
fi

echo ""
echo -e "${GREEN}[2/4] 配置 Docker 镜像...${NC}"
if [ -f "daemon.json" ]; then
    if [ "$OS" = "Linux" ]; then
        if [ -d "/etc/docker" ]; then
            echo -e "${YELLOW}   ⚠ 需要手动复制 daemon.json 到 /etc/docker/ 并重启 Docker${NC}"
            echo -e "${YELLOW}     命令: sudo cp daemon.json /etc/docker/daemon.json && sudo systemctl restart docker${NC}"
        else
            echo -e "${YELLOW}   ⚠ Docker 目录不存在，跳过${NC}"
        fi
    elif [ "$OS" = "Darwin" ]; then
        echo -e "${YELLOW}   ⚠ macOS 请在 Docker Desktop 中手动配置镜像源${NC}"
    fi
else
    echo -e "${YELLOW}   ⚠ daemon.json 不存在，跳过${NC}"
fi

echo ""
echo -e "${GREEN}[3/4] 备份原始 gradle-wrapper.properties...${NC}"
WRAPPER_FILE="gradle/wrapper/gradle-wrapper.properties"
if [ -f "$WRAPPER_FILE" ]; then
    if [ ! -f "${WRAPPER_FILE}.bak" ]; then
        cp "$WRAPPER_FILE" "${WRAPPER_FILE}.bak"
        echo -e "${GREEN}   ✓ 已备份原始配置${NC}"
    fi
fi

echo ""
echo -e "${GREEN}[4/4] 配置完成！${NC}"
echo ""
echo "=========================================="
echo "使用说明："
echo "  - Gradle 镜像已配置到 ~/.gradle/init.gradle.kts"
echo "  - 如需使用 Gradle Wrapper 镜像，请编辑："
echo "    $WRAPPER_FILE"
echo ""
echo "验证命令："
echo "  ./gradlew tasks --init-script init.gradle.kts"
echo ""
echo "更多详情请查看 LOCAL_SETUP.md"
echo "=========================================="
