# solo-spring-scaffold

<details open>
<summary>🇨🇳 中文</summary>

> Spring Boot 3.x 多模块项目脚手架生成器 — 用 CLI 一键生成企业级项目骨架。

`solo-spring-scaffold` 是一个 Python CLI 工具，根据模板生成标准化的 Spring Boot 3.x 多模块项目。开箱即用（H2 内存库），也可以一键切换到 MySQL + MyBatis-Plus 代码生成。

**这不是一个框架，而是一个项目生成器。** 它把架构决策打包成可复用的模板，让你不用每次从零搭项目。

---

## 快速开始

```bash
# 1. 生成项目（基础骨架）
python solo-spring-scaffold init my-app --package com.example.myapp --jdk 21

# 2. 生成项目 + 启用插件
python solo-spring-scaffold init my-app --package com.example.myapp --jdk 21 \
    --plugins security,docker,test

# 3. 进入项目、启动（H2 内存库，零配置）
cd my-app
mvn spring-boot:run -pl my-app-web -am

# 4. 打开浏览器
# http://localhost:8080/swagger-ui.html
```

Windows 用户也可以用 `solo-spring-scaffold.bat`（需将所在目录加入 PATH）。

### 带 MySQL 代码生成

```bash
python solo-spring-scaffold init my-app \
    --package com.example.myapp \
    --jdk 21 \
    --db-url jdbc:mysql://192.168.1.100:3306/mydb \
    --db-user root \
    --db-pass xxx
```

生成的 `bin/gen-tables.sh` 可从数据库表自动生成 Entity/Mapper/Service/Controller。

---

## 插件系统

插件是本项目的核心差异化特性。每个插件独立提供一组功能模板，按需启用。

### 查看可用插件

```bash
python solo-spring-scaffold list-plugins
```

### 内置插件

| 插件 | 描述 | 优先级 |
|------|------|--------|
| `security` | Spring Security + JWT 认证/鉴权（登录、注册、令牌刷新） | P0 🔴 |
| `docker` | Dockerfile + docker-compose.yml（分层构建、健康检查） | P0 🔴 |
| `test` | 单元/集成测试模板（JUnit 5 + Mockito + MockMvc） | P0 🔴 |
| `redis` | Redis + Spring Cache 配置（Lettuce + JSON 序列化） | P1 🟠 |
| `actuator` | Spring Boot Actuator + Prometheus 指标 + 健康检查 | P1 🟠 |

### 插件用法

```bash
# 启用单个插件
python solo-spring-scaffold init my-app --package com.example.myapp --jdk 21 \
    --plugins security

# 启用多个插件（逗号分隔）
python solo-spring-scaffold init my-app --package com.example.myapp --jdk 21 \
    --plugins security,docker,test,redis,actuator
```

### 插件架构

每个插件包含：
- `plugin.yaml` — 元数据 + 补丁声明
- `templates/` — 新增的 Java/配置文件模板
- `patches/` — 对已有文件的修改（YAML 追加、依赖注入、代码注入等）

详见 `python/plugins/` 目录。

---

## 前置依赖

| 工具 | 版本要求 |
|------|---------|
| JDK | 17 / 21 / 25 |
| Maven | 3.8+ |
| Python | 3.10+ |
| IDEA (推荐) | 2023+ |

---

## 生成的项目包含什么

```
my-app/
├── my-app-common/       # 公共模块
│   └── api/             # Result, PageResult 统一返回体
│   └── exception/       # BizException, ErrorCode, 全局异常处理器
│   └── web/             # LoggingAspect（请求日志AOP）, MDC Filter
│   └── security/        # [插件] Security + JWT 配置
├── my-app-dao/          # 数据层
│   └── config/          # MyBatis-Plus 配置 + MetaObjectHandler
│   └── user/            # User Entity + Mapper（演示）
│   └── generator/       # MyBatis-Plus 代码生成器（可选）
├── my-app-service/      # 业务层
│   └── user/            # UserService + DTO + 实现
├── my-app-web/          # Web 层
│   └── user/            # UserController（CRUD 演示）
│   └── auth/            # [插件] AuthController（登录/注册）
│   └── resources/
│       ├── application.yml          # 主配置（H2 内存库）
│       ├── application-dev.yml      # 开发环境
│       ├── application-prod.yml     # 生产环境（MySQL）
│       ├── logback-spring.xml       # 日志分级切割
│       ├── db/migration/            # Flyway 迁移脚本
│       └── messages/validation.properties  # 校验国际化
├── bin/                              # 部署脚本
│   ├── start.sh / start.bat
│   ├── stop.sh / stop.bat
│   ├── deploy.sh
│   └── gen-tables.sh / .bat（可选）
├── Dockerfile                        # [插件] 分层构建
├── docker-compose.yml                # [插件] MySQL + App
└── pom.xml                           # 父 POM
```

### 内置的技术选型

| 维度 | 选择 | 理由 |
|------|------|------|
| ORM | MyBatis-Plus 3.5.9 | 灵活写 SQL，分页/自动填充开箱即用 |
| 数据库迁移 | Flyway | 版本化、可回滚、团队协作友好 |
| API 文档 | SpringDoc OpenAPI 2.6 | 原生支持 OpenAPI 3，UI 现代 |
| 参数校验 | Jakarta Validation + 国际化 | 统一错误消息 |
| 日志 | Logback + MDC traceId | 分级切割、分布式追踪准备 |
| 开发期数据库 | H2（内存） | 零配置启动，PR 评审无需搭数据库 |

---

## Roadmap

### P0 — 生产就绪 ✅ (已完成)
- [x] Spring Security + JWT 认证模板
- [x] Dockerfile + docker-compose.yml
- [x] 单元/集成测试模板

### P1 — 架构增强
- [x] Redis + Spring Cache 配置
- [x] Spring Boot Actuator + Prometheus
- [ ] 多 Domain 示例（订单、商品）
- [ ] 文件上传/下载示例
- [ ] 交互式 CLI 向导

### P2 — 产品化
- [ ] pip install 分发
- [ ] 在线生成平台
- [ ] 插件市场（社区贡献）
- [ ] 行业解决方案模板（电商/SaaS/IoT）

---

## License

[MIT](../LICENSE)

</details>
