# 本地开发环境配置指南

## 概述
本指南说明如何配置本地开发环境，使用国内镜像源加速依赖下载，而不影响仓库的原有配置。

## 配置步骤

### 1. Gradle 镜像配置

#### 方法一：使用 init.gradle.kts (推荐)
将项目根目录的 `init.gradle.kts` 复制到 Gradle 用户目录：

```bash
# Linux/macOS
cp init.gradle.kts ~/.gradle/init.gradle.kts

# Windows
copy init.gradle.kts %USERPROFILE%\.gradle\init.gradle.kts
```

或者在项目根目录执行 Gradle 命令时指定：
```bash
./gradlew assembleDebug --init-script init.gradle.kts
```

#### 方法二：使用 gradle.properties
```bash
cp gradle.properties.local gradle.properties
```

### 2. Docker 镜像配置

将 `daemon.json` 复制到 Docker 配置目录：

```bash
# Linux
sudo cp daemon.json /etc/docker/daemon.json
sudo systemctl restart docker

# macOS (Docker Desktop)
# 在 Docker Desktop -> Settings -> Docker Engine 中添加配置
```

### 3. Gradle Wrapper 镜像配置

编辑 `gradle/wrapper/gradle-wrapper.properties`，将 distributionUrl 替换为国内镜像：

```properties
# 原配置
# distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip

# 腾讯云镜像
distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-8.5-bin.zip
```

**注意**：此修改建议在本地进行，不要提交到仓库！

## 验证配置

### 验证 Gradle 配置
```bash
./gradlew dependencies --init-script init.gradle.kts
```

### 验证 Docker 配置
```bash
docker info | grep -A 10 "Registry Mirrors"
```

## 开发流程

1. **本地开发**：使用上述镜像配置进行测试和构建
2. **问题解决**：在本地环境中先测试和修复问题
3. **代码提交**：确保提交的代码不包含任何本地镜像配置
4. **推送**：确认本地测试通过后再推送到远程仓库

## 注意事项

- 所有本地配置文件已添加到 `.gitignore`，不会被提交到仓库
- 不要修改项目原有的 `settings.gradle` 或 `build.gradle` 中的仓库配置
- 在推送代码前，务必确保代码在不使用镜像配置的情况下也能正常构建
