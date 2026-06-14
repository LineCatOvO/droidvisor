# Droidvisor 项目问题清单 (PROBLEM_INVENTORY)

## 元信息
- project: projects/droidvisor
- created: 2026-06-14
- last_updated: 2026-06-14

## 问题列表

### P001 — build.gradle 硬编码签名凭证 [已修复]
- **ID**: P001
- **状态**: ✅ 已修复
- **严重度**: HIGH
- **类别**: D3-安全
- **文件**: app/build.gradle:42-44
- **描述**: 签名密钥密码有硬编码默认值 `"droidvisor123"`，任何人未设置环境变量即可使用默认凭证
- **修复**: 移除硬编码默认值，改为空字符串，强制环境变量配置
- **发现日期**: 2026-06-14

### P002 — test-docker.sh 违反 Docker 规则 [已修复]
- **ID**: P002
- **状态**: ✅ 已修复
- **严重度**: MEDIUM
- **类别**: D4-规范合规性
- **文件**: scripts/test-docker.sh
- **描述**: 
  1. 使用 `docker-compose up -d`（违反 docker-rules.md 禁止 -d/--detach）
  2. 使用 `docker exec`（违反禁止 docker exec 规则）
  3. 使用 `sleep` 固定等待（违反禁止固定等待规则）
  4. 引用未定义的 `droidvisor-dind` 服务
- **修复**: 重写脚本，使用 `docker-compose build` + `docker-compose run --rm` 模式
- **发现日期**: 2026-06-14

### P003 — BackupManagerService 异步返回值不准确 [待修复]
- **ID**: P003
- **状态**: 🔴 打开
- **严重度**: LOW
- **类别**: D2-代码质量
- **文件**: app/src/main/java/com/droidvisor/vm/BackupManagerService.kt:71-153
- **描述**: `createBackup()` 在协程中异步执行备份，但同步返回 `BackupResult.Success`，调用方无法感知异步操作的实际结果
- **建议**: 改为 suspend 函数或使用回调/Flow
- **发现日期**: 2026-06-14

### P004 — QemuProcessManager 使用 Thread.sleep 阻塞 [待修复]
- **ID**: P004
- **状态**: 🔴 打开
- **严重度**: LOW
- **类别**: D2-代码质量
- **文件**: app/src/main/java/com/droidvisor/vm/qemu/QemuProcessManager.kt:428,435
- **描述**: `gracefulShutdown()` 使用 `Thread.sleep()` 阻塞线程，在 Dispatchers.IO 上运行会占用线程池
- **建议**: 替换为 `kotlinx.coroutines.delay()`
- **发现日期**: 2026-06-14

### P005 — VsockService.receive() 使用 InputStream.available() [待修复]
- **ID**: P005
- **状态**: 🔴 打开
- **严重度**: LOW
- **类别**: D2-代码质量
- **文件**: app/src/main/java/com/droidvisor/vm/vsock/VsockService.kt:198-212
- **描述**: `receive()` 使用 `InputStream.available()` 判断数据可用性，此方法不可靠（可能返回 0 即使有数据）
- **建议**: 使用阻塞读取 `read()` 或 NIO Channel
- **发现日期**: 2026-06-14

### P006 — Logger 泄露堆栈信息 [待修复]
- **ID**: P006
- **状态**: 🔴 打开
- **严重度**: LOW
- **类别**: D3-安全
- **文件**: app/src/main/java/com/droidvisor/util/Logger.kt:35,55,67,71
- **描述**: `throwable.stackTraceToString()` 可能在生产环境泄露内部实现细节
- **建议**: 生产构建中关闭堆栈追踪或使用条件判断
- **发现日期**: 2026-06-14

### P007 — docker-compose.yml healthcheck 不可靠 [待修复]
- **ID**: P007
- **状态**: 🔴 打开
- **严重度**: LOW
- **类别**: D4-规范合规性
- **文件**: docker-compose.yml:19-24
- **描述**: healthcheck 使用 `ps aux | grep gradle`，不可靠且脆弱
- **建议**: 使用基于文件或端口的健康检查
- **发现日期**: 2026-06-14

### P008 — VirtualMachineManagerService 反射依赖 Android 内部 API [待修复]
- **ID**: P008
- **状态**: 🔴 打开
- **严重度**: MEDIUM
- **类别**: D7-衍生问题
- **文件**: app/src/main/java/com/droidvisor/vm/VirtualMachineManagerService.kt
- **描述**: 大量使用反射调用 `android.system.virtualmachine.*` 内部 API，系统版本升级时 API 可能变更导致崩溃
- **建议**: 监控 Android 版本升级，考虑使用官方 SDK 接口
- **发现日期**: 2026-06-14

### P009 — VsockService 竞态条件风险 [待修复]
- **ID**: P009
- **状态**: 🔴 打开
- **严重度**: MEDIUM
- **类别**: D7-衍生问题
- **文件**: app/src/main/java/com/droidvisor/vm/vsock/VsockService.kt:52,93-125
- **描述**: `vsockChannel` 字段在 `connect()` 函数中无同步保护，多协程并发调用可能导致状态不一致
- **建议**: 添加 `synchronized` 或 `Mutex` 保护
- **发现日期**: 2026-06-14

### P010 — Kotlin 编译器内部错误导致测试编译失败 [待修复]
- **ID**: P010
- **状态**: 🔴 打开
- **严重度**: HIGH
- **类别**: D5-问题发现与测试闭环
- **文件**: app/build.gradle:66 (composeOptions)
- **描述**: 测试编译 (`compileDebugUnitTestKotlin`) 持续失败，报 Internal Compiler Error: `NoSuchMethodError: IrLazyClass$Companion.<init>`。主代码编译正常。推测原因：Kotlin 1.9.23 + Compose Compiler 1.5.14 版本不匹配（Compose Compiler 1.5.14 标准兼容 Kotlin 1.9.22），尽管使用了 `suppressKotlinVersionCompatibilityCheck` 抑制警告，但可能仍存在 ABI 不兼容。
- **建议**: 升级 Compose Compiler 到兼容 Kotlin 1.9.23 的版本，或降级 Kotlin 到 1.9.22
- **发现日期**: 2026-06-14
- **验证环境**: Docker (Ubuntu 22.04 + OpenJDK 17.0.19 + Gradle 8.6)

### P011 — Dockerfile 缺少 NDK/Build-Tools 预装 [已修复]
- **ID**: P011
- **状态**: ✅ 已修复
- **严重度**: MEDIUM
- **类别**: D4-规范合规性
- **文件**: Dockerfile:33-35
- **描述**: Dockerfile 未预装 NDK 26.1.10909125、Build-Tools 34.0.0、CMake 3.22.1，导致每次 `docker run --rm` 都需要重新下载 ~1.5GB NDK
- **修复**: 在 Dockerfile 的 sdkmanager 命令中添加 `"build-tools;34.0.0" "ndk;26.1.10909125" "cmake;3.22.1"`；同时创建持久化 SDK 卷 `/workspaces/agent-workspace/.cache/android-sdk` 作为备选方案
- **发现日期**: 2026-06-14

### P012 — VM 管理刷新按钮无效 [QA 发现]
- **ID**: P012
- **状态**: 🔴 打开
- **严重度**: MEDIUM
- **类别**: 功能缺陷
- **文件**: app/src/main/java/com/droidvisor/ui/screen/VmManagementScreen.kt:95
- **描述**: 刷新按钮的 onClick 仅访问 `vmManagerService?.vmInstances?.let {}`，不执行任何实际操作
- **建议**: 改为调用实际的刷新方法
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P013 — 系统信息页面显示硬编码假数据 [QA 发现]
- **ID**: P013
- **状态**: 🔴 打开
- **严重度**: MEDIUM
- **类别**: 功能缺陷
- **文件**: app/src/main/java/com/droidvisor/ui/screen/SettingsScreen.kt:157-160
- **描述**: SystemInfoSection 显示硬编码的 "AVF Support: Supported" 等假数据，不反映实际设备状态
- **建议**: 从 AvfCapabilityChecker 和 Build.VERSION 读取真实数据
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P014 — 备份管理返回按钮无效 [QA 发现]
- **ID**: P014
- **状态**: 🔴 打开
- **严重度**: MEDIUM
- **类别**: 功能缺陷
- **文件**: app/src/main/java/com/droidvisor/ui/screen/BackupManagementScreen.kt:52-55
- **描述**: 返回按钮的 onClick 为空注释 `/* 关闭界面 */`，无法导航返回
- **建议**: 添加 onBack 回调参数或使用 Navigation 返回
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P015 — 网络配置返回按钮无效 [QA 发现]
- **ID**: P015
- **状态**: 🔴 打开
- **严重度**: MEDIUM
- **类别**: 功能缺陷
- **文件**: app/src/main/java/com/droidvisor/ui/screen/NetworkConfigScreen.kt:74-77
- **描述**: 返回按钮的 onClick 为空注释 `/* 关闭界面 */`，无法导航返回
- **建议**: 添加 onBack 回调参数或使用 Navigation 返回
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P016 — 终端粘贴功能在 clipboardManager 为 null 时静默失败 [QA 发现]
- **ID**: P016
- **状态**: 🔴 打开
- **严重度**: LOW
- **类别**: 边界行为
- **文件**: app/src/main/java/com/droidvisor/ui/screen/TerminalScreen.kt:156-159
- **描述**: clipboardManager 为 null 时粘贴和复制操作无用户反馈
- **建议**: 添加 else 分支显示 Toast 提示
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P017 — 终端特殊键 Ctrl+C/D/L 仅在模拟模式下使用硬编码值 [QA 发现]
- **ID**: P017
- **状态**: 🔴 打开
- **严重度**: LOW
- **类别**: 代码质量
- **文件**: app/src/main/java/com/droidvisor/ui/screen/TerminalScreen.kt:273-284
- **描述**: Ctrl+C/D/L 使用 Unicode 控制字符硬编码，无法通过标准键盘输入
- **建议**: 添加专用按钮或确认触发方式
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P018 — VM 删除无确认对话框 [QA 发现]
- **ID**: P018
- **状态**: 🔴 打开
- **严重度**: HIGH
- **类别**: 功能缺陷/UX
- **文件**: app/src/main/java/com/droidvisor/ui/screen/VmManagementScreen.kt:137
- **描述**: 删除 VM 操作直接执行，无确认对话框，可能导致误删
- **建议**: 添加确认对话框（复用 DeleteConfirmDialog）
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P019 — Docker 容器删除无确认对话框 [QA 发现]
- **ID**: P019
- **状态**: 🔴 打开
- **严重度**: HIGH
- **类别**: 功能缺陷/UX
- **文件**: app/src/main/java/com/droidvisor/ui/screen/DockerDashboardScreen.kt:468,642
- **描述**: 容器删除直接执行，无确认对话框
- **建议**: 添加确认对话框
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P020 — Docker 镜像删除无确认对话框 [QA 发现]
- **ID**: P020
- **状态**: 🔴 打开
- **严重度**: HIGH
- **类别**: 功能缺陷/UX
- **文件**: app/src/main/java/com/droidvisor/ui/screen/DockerDashboardScreen.kt:755,866
- **描述**: 镜像删除直接执行，无确认对话框
- **建议**: 添加确认对话框
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P021 — 设置变更不联动 VM 创建默认值 [QA 发现]
- **ID**: P021
- **状态**: 🔴 打开
- **严重度**: MEDIUM
- **类别**: 功能缺陷/数据流
- **文件**: SettingsViewModel.kt ↔ VmTemplate.kt ↔ VmManagementScreen.kt
- **描述**: 用户在设置中修改的默认内存/CPU 配置不会影响创建 VM 时的模板默认值
- **建议**: 创建设置→模板默认值的映射逻辑
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P022 — 网络配置保存回调为空 [QA 发现]
- **ID**: P022
- **状态**: 🔴 打开
- **严重度**: HIGH
- **类别**: 功能缺陷
- **文件**: app/src/main/java/com/droidvisor/ui/screen/VmManagementScreen.kt:615
- **描述**: `onSave = { /* 保存网络配置 */ }` 回调为空，网络配置变更无法保存
- **建议**: 实现网络配置持久化逻辑
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P023 — 设置内存滑块下限与验证器不一致 [QA 发现]
- **ID**: P023
- **状态**: 🔴 打开
- **严重度**: MEDIUM
- **类别**: 数据一致性
- **文件**: SettingsScreen.kt:81 vs VmConfigValidator.kt:6
- **描述**: 设置滑块最小 128MB，但 VmConfigValidator 要求最小 512MB，用户可设置一个无法通过验证的值
- **建议**: 统一最低内存限制为 512MB
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

### P024 — Docker 操作错误静默忽略 [QA 发现]
- **ID**: P024
- **状态**: 🔴 打开
- **严重度**: MEDIUM
- **类别**: 错误处理
- **文件**: app/src/main/java/com/droidvisor/docker/DockerDashboardViewModel.kt:308-309, 699-700, 717-718, 762-763, 781-782
- **描述**: 多个 catch 块使用 `// ignore` 忽略异常，用户无法感知操作失败
- **建议**: 添加错误状态 StateFlow 并在 UI 中显示错误提示
- **发现日期**: 2026-06-14
- **发现阶段**: Layer 3 QA

---

## 问题统计
| 状态 | 数量 |
|------|------|
| ✅ 已修复 | 3 |
| 🔴 打开 | 21 |
| **合计** | **24** |

## Layer 2 自动化测试结果

| 类别 | Gradle 目标 | 状态 | 说明 |
|------|-------------|------|------|
| Lint 检查 | detekt | ✅ 通过 | BUILD SUCCESSFUL (5m 53s), 999 code smells, 6d 8h debt |
| Lint 检查 | lintDebug | ✅ 通过 | HTML/SARIF 报告已生成 |
| Build 构建 | assembleDebug | ✅ 通过 | 62 actionable tasks: 35 executed, 25 from cache |
| 单元测试 | testDebugUnitTest | ❌ 失败 | Kotlin Internal Compiler Error (P010) |
| 集成测试 | testDebugUnitTest --tests "*IntegrationTest" | ❌ 跳过 | 依赖单元测试编译 |
| E2E 测试 | connectedAndroidTest | ❌ 跳过 | 需要物理设备/模拟器 |
| Prod 构建 | assembleRelease | ❌ 跳过 | 需要签名密钥 |
| 其他目标 | jacocoTestReport | ❌ 跳过 | 依赖单元测试 |