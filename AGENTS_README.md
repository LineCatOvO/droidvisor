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

- Android SDK 34
- Java JDK 17+
- Gradle 8.5
- Docker 24+ (for testing)
- Docker Compose 2.0+

## 已知的项目特殊问题

- SSH密钥为只读权限，无法直接push到远程（需要手动推送或配置密钥）
- 部分测试存在mock相关问题（如 Mockito stubbings 警告）
- 详见 [PROBLEM_INVENTORY.md](./PROBLEM_INVENTORY.md)（24 个已知问题，3 已修复/21 打开）

## 测试状态（2026-06-14 验证）

| 类别 | 状态 | 说明 |
|------|------|------|
| Lint (detekt) | ✅ 通过 | 999 code smells, 无阻塞性问题 |
| Lint (lintDebug) | ✅ 通过 | HTML/SARIF 报告已生成 |
| Build (assembleDebug) | ✅ 通过 | 62 tasks |
| 单元测试 | ❌ 失败 | P010: Kotlin 编译器兼容性问题 |
| 集成测试 | ❌ 跳过 | 依赖单元测试编译 |
| E2E 测试 | ❌ 跳过 | 需要物理设备/模拟器 |
| Prod 构建 | ❌ 跳过 | 需要签名密钥 |

## QA 审核结果（2026-06-14）

- 测试路径：100 条（7 页面覆盖）
- 发现新问题：13 个（P012-P024）
- QA 结论：❌ 拒绝（4 HIGH 阻塞性缺陷）

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