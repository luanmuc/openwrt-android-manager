# OpenWrt管家

一款原生安卓应用，用于管理 OpenWrt / ImmortalWrt 路由器。

## 功能特性

- 📱 **Material Design 3** - 现代化 UI 设计，支持深色/浅色模式
- 🔐 **安全存储** - 密码 AES 加密本地存储
- 📡 **多设备管理** - 支持管理多台 OpenWrt 路由器
- ⚡ **实时状态** - 查看路由器运行状态、系统信息
- 🔌 **插件管理** - 管理 LuCI 插件（开发中）
- 🌐 **LuCI API** - 基于官方 LuCI RPC API 开发

## 技术栈

- **语言**: Kotlin
- **架构**: MVVM
- **UI**: Jetpack Compose + Material Design 3
- **网络**: Retrofit + OkHttp
- **异步**: Kotlin Coroutines + Flow
- **存储**: DataStore Preferences
- **最低 SDK**: API 30 (Android 11)
- **目标 SDK**: API 34 (Android 14)

## 项目结构

```
app/src/main/java/org/openwrt/manager/
├── data/
│   ├── model/          # 数据模型
│   ├── repository/     # 数据仓库
│   └── api/            # API 接口
├── ui/
│   ├── theme/          # 主题配置
│   ├── home/           # 首页
│   ├── devices/        # 设备页
│   ├── plugins/        # 插件页
│   ├── profile/        # 我的页
│   └── addrouter/      # 添加路由器
├── util/               # 工具类
├── MainActivity.kt     # 主Activity
└── OpenWrtApp.kt       # Application
```

## 构建

```bash
# Debug 构建
./gradlew assembleDebug

# Release 构建
./gradlew assembleRelease
```

## 许可证

MIT License
