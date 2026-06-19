# Droidvisor AGENTS_README

## 项目注意事项

- 本项目是基于 Android AVF (Android Virtualization Framework) 的虚拟机管理应用
- MVP 阶段已完成核心功能（VM管理、Docker集成、Jetpack Compose UI）
- 测试环境必须使用 Docker 容器执行（docker-compose up --build）
- 测试运行：./scripts/test-docker.sh 或在 Docker 容器内 ./gradlew testDebugUnitTest

## 特殊规范

- 分支策略：所有开发在 agent-develop 分支进行
- 提交规范：遵循 conventional commits 格式
- 代码检查：必须通过 detekt静态分析
- 测试要求：所有测试必须通过，不得跳过

## 本地配置要求

- Android SDK 35 (compileSdk 35 / minSdk 34)
- Java JDK 17+
- Gradle 9.4.1
- Kotlin 2.2.10
- AGP 9.2.1
- Docker 24+ (for testing)
- Docker Compose 2.0+

## 已知的项目特殊问题

- SSH密钥为只读权限，无法直接push到远程（需要手动推送或配置密钥）
- 部分测试存在mock相关问题（如 Mockito stubbings 警告）
- 详见 [PROBLEM_INVENTORY.md](./PROBLEM_INVENTORY.md)（**25 个已知问题，25 全部 verified_closed / 0 打开** - 2026-06-18 Sprint3 更新）

## 测试状态（2026-06-18 Sprint3 Verification 更新）

| 类别 | 状态 | 说明 |
|------|------|------|
| Lint (detekt) | ✅ 通过 | 1038 code smells, 无阻塞性问题 |
| Lint (lintDebug) | ✅ 通过 | HTML/SARIF 报告已生成 |
| Build (assembleDebug) | ✅ 通过 | BUILD SUCCESSFUL, 62 tasks |
| 编译验证 (compileDebugKotlin) | ✅ 通过 | 零 errors |
| 编译验证 (compileDebugUnitTestKotlin) | ✅ 通过 | 零 errors |
| 单元测试 (testDebugUnitTest) | ✅ **通过** | **1042/1042 全部通过, 0 失败** |
| 集成测试 | ⏭️ 跳过 | 项目无独立集成测试目标 |
| E2E 测试 | ⏭️ 跳过 | 需要物理设备/模拟器 |
| Prod 构建 | ⏭️ 跳过 | 需要签名密钥 |

### Sprint2 修复成果（2026-06-17）

**修复问题数**: 7/7 (100%)
- ✅ P025 ICE 测试编译问题（**核心目标达成**）
- ✅ P022 网络配置持久化
- ✅ P021 设置联动机制
- ✅ P003 BackupManagerService 异步化
- ✅ P005 VsockService 阻塞读取
- ✅ P006 Logger 堆栈保护
- ✅ P009 VsockService 并发安全

### Sprint3 修复成果（2026-06-18）

**修复问题数**: 4/4 (100%)
- ✅ P010 Kotlin serialization 插件版本不匹配（1.9.23→2.2.10）
- ✅ 5 个失败测试修复（DockerDashboardViewModelErrorStateTest ×4 + VmTemplateTest ×1）
- ✅ GlobalScope 替换为 rememberCoroutineScope（2 处）
- ✅ PROBLEM_INVENTORY.md 同步更新（P004/P010 状态）

**Verification 结论**: ✅ **三层审核链全部通过**（L1 代码审核 ✅ | L2 自动化测试 1042/1042 ✅ | L3 QA 源代码分析 ✅）

## 项目结构说明

```
com.droidvisor/
├── MainActivity.kt           # 主入口
├── datastore/                # DataStore 配置
├── docker/                   # Docker 集成
│   ├── model/               # Docker 数据模型
│   └── DockerDashboardViewModel.kt
├── ui/
│   ├── components/          # 可复用组件
│   ├── screen/              # 页面组件
│   └── viewmodel/           # ViewModel
├── util/                    # 工具类
└── vm/
    ├── model/               # VM 数据模型
    └── vsock/               # Vsock 通信
```

## 关键信息

- Dockerfile: 提供 Android SDK 测试环境
- docker-compose.yml: 包含测试环境和服务配置
- detekt-config.yml: 代码静态分析配置
- gradle.properties: 构建参数配置