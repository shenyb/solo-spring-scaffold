# solo-spring-scaffold 在线平台方案

> 版本: 1.0 | 日期: 2025-06

---

## 一、产品定位

**一句话：** Spring Boot 项目生成器在线版 —— 像搭积木一样拼装你的后端项目。

**核心差异：**
- Spring Initializr 只生成空壳，我们需要 30 秒生成**可运行的生产级骨架**
- RuoYi/JeecgBoot 是完整后台系统，我们提供的是**可插拔的建筑蓝图**
- 竞品要么太轻（Initializr），要么太重（RuoYi），我们卡在**中间甜点区**

---

## 二、技术架构

```
┌─────────────────────────────────────────────────────┐
│                   Frontend (Next.js)                 │
│  ┌─────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ │
│  │ 模块选择 │ │ 架构预览  │ │ 代码预览  │ │ 一键下载 │ │
│  └─────────┘ └──────────┘ └──────────┘ └─────────┘ │
└────────────────────────┬────────────────────────────┘
                         │ REST API
┌────────────────────────▼────────────────────────────┐
│                  Backend (FastAPI)                    │
│  ┌──────────┐ ┌──────────┐ ┌─────────────────────┐ │
│  │ /generate │ │ /plugins │ │ /preview (streaming) │ │
│  └──────────┘ └──────────┘ └─────────────────────┘ │
│  ┌────────────────────────────────────────────────┐ │
│  │       Plugin Engine (Python CLI 复用)           │ │
│  └────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────┘
                         │
              ┌──────────▼──────────┐
              │   S3 / 本地存储      │
              │   (生成的 ZIP 文件)  │
              └─────────────────────┘
```

### 2.1 前端 (Next.js 14 + Tailwind)

| 页面 | 功能 |
|------|------|
| `/` | 首页：项目名 + 包名 + JDK 选择 |
| `/configure` | 插件选择：卡片式勾选，实时显示依赖关系 |
| `/preview` | 代码预览：虚拟文件树 + 语法高亮 |
| `/download` | 下载 + 推送到 GitHub |

**关键交互：**

```
用户填写项目信息
    ↓
选择插件（卡片式，显示兼容性）
    ↓                    实时
架构图预览 ←───────────────────┘
    ↓
代码预览（可展开每个文件）
    ↓
一键下载 ZIP / 推送 GitHub
```

### 2.2 后端 (FastAPI)

| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/plugins` | GET | 返回插件列表 + 元数据 |
| `/api/validate` | POST | 验证插件组合兼容性 |
| `/api/generate` | POST | 生成项目，返回下载链接 |
| `/api/preview` | POST | 流式返回文件树 + 代码内容 |
| `/api/github/push` | POST | OAuth 推送到用户 GitHub 仓库 |

### 2.3 生成引擎

**核心思路：** 复用现有 Python CLI 的 `generate_project()` + `apply_plugin()` 函数。

```python
# 在线版生成逻辑（复用 CLI 代码）
from solo_spring_scaffold import generate_project

@app.post("/api/generate")
async def generate(request: GenerateRequest):
    with tempfile.TemporaryDirectory() as tmpdir:
        project_path = Path(tmpdir) / request.project_name
        generate_project(
            project_path=project_path,
            project_name=request.project_name,
            base_package=request.package,
            jdk_version=request.jdk,
            plugins=request.plugins,
            db_url=request.db_url,
        )
        # 打包 ZIP
        zip_path = shutil.make_archive(str(project_path), "zip", str(project_path))
        # 上传到 S3 或直接返回
        return {"download_url": upload_to_s3(zip_path)}
```

---

## 三、插件系统设计

### 3.1 插件目录结构

```
python/plugins/
├── security/                    # Spring Security + JWT
│   ├── plugin.yaml              # 元数据 + 补丁声明
│   ├── templates/               # 新增文件模板
│   │   ├── common/src/main/java/__pkg__/common/security/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   ├── SecurityExceptionHandler.java
│   │   │   └── TokenPair.java
│   │   ├── service/src/main/java/__pkg__/user/
│   │   │   ├── AuthService.java
│   │   │   ├── impl/AuthServiceImpl.java
│   │   │   └── dto/
│   │   │       ├── LoginRequest.java
│   │   │       ├── LoginResponse.java
│   │   │       └── RegisterRequest.java
│   │   └── web/src/main/java/__pkg__/web/auth/
│   │       └── AuthController.java
│   └── patches/                 # 对已有文件的补丁
│       ├── application-security.yml    # YAML 追加
│       ├── validation-security.properties  # Properties 追加
│       ├── V3__add_user_password.sql   # Flyway 迁移
│       └── dependencies.txt            # POM 依赖注入
├── docker/                      # Dockerfile + compose
│   ├── plugin.yaml
│   └── templates/
│       ├── Dockerfile
│       ├── docker-compose.yml
│       └── .dockerignore
├── test/                        # 测试模板
│   ├── plugin.yaml
│   ├── templates/
│   │   ├── service/src/test/java/__pkg__/user/UserServiceTest.java
│   │   ├── web/src/test/java/__pkg__/user/UserControllerTest.java
│   │   └── dao/src/test/java/__pkg__/user/UserMapperTest.java
│   └── patches/
│       └── dependencies.txt
├── redis/                       # Redis + Spring Cache
│   ├── plugin.yaml
│   ├── templates/
│   │   └── dao/src/main/java/__pkg__/config/RedisConfig.java
│   └── patches/
│       ├── application-redis.yml
│       └── dependencies.txt
├── actuator/                    # 监控 + Prometheus
│   ├── plugin.yaml
│   ├── templates/
│   │   └── dao/src/main/java/__pkg__/config/DatabaseHealthIndicator.java
│   └── patches/
│       ├── application-actuator.yml
│       └── dependencies.txt
└── ... (更多插件)
```

### 3.2 plugin.yaml 规范

```yaml
# 必填字段
name: security              # 插件唯一标识
version: 1.0.0              # 语义化版本
description: Spring Security + JWT 认证/鉴权
author: solo-spring-scaffold
category: core              # core | infra | quality | biz

# 分类标签（用于在线平台搜索/筛选）
tags: [security, jwt, auth]

# 插件依赖（需要先启用哪些插件）
requires: []

# 模板文件（新增到项目中）
templates:
  - dest: common/src/main/java/__pkg__/common/security
    files:
      - SecurityConfig.java
      - JwtTokenProvider.java
      # ...

# 补丁文件（修改/追加到已有文件）
patches:
  - type: yaml-append           # 追加到 YAML 文件
    src: application-security.yml
    dest: web/src/main/resources/application.yml
  - type: properties-append     # 追加到 Properties 文件
    src: validation-security.properties
    dest: web/src/main/resources/messages/validation.properties
  - type: copy                  # 拷贝新文件
    src: V3__add_user_password.sql
    dest: web/src/main/resources/db/migration/V3__add_user_password.sql
  - type: dependencies          # POM 依赖注入
    src: dependencies.txt

# 实体字段注入（修改已有 Entity）
entity-patches:
  - entity: User
    fields:
      - name: password
        type: String
        annotation: |
          @Schema(description = "密码（加密存储）")
        column: password VARCHAR(128)

# 文件修改（代码注入）
file-modifications:
  - file: dao/src/main/java/__pkg__/user/User.java
    type: field-inject
    after: "private String username;"
    content: |
      
          @Schema(description = "密码（加密存储）")
          private String password;
```

### 3.3 补丁类型详细说明

| 类型 | 用途 | 工作原理 |
|------|------|---------|
| `yaml-append` | 追加配置到 YAML | 将内容追加到文件末尾，YAML 合并 |
| `properties-append` | 追加 i18n 消息 | 追加到 properties 文件末尾 |
| `copy` | 拷贝新文件 | 直接复制到指定路径 |
| `dependencies` | 注入 Maven 依赖 | 解析 `module=g:a:v` 格式，注入到对应 pom.xml |
| `field-inject` | 注入 Entity 字段 | 在指定行后插入 Java 字段代码 |

### 3.4 插件兼容性矩阵

| 插件 | security | docker | test | redis | actuator |
|------|----------|--------|------|-------|----------|
| security | — | ✅ | ✅ | ✅ | ✅ |
| docker | ✅ | — | ✅ | ✅ | ✅ |
| test | ✅ | ✅ | — | ✅ | ✅ |
| redis | ✅ | ✅ | ✅ | — | ✅ |
| actuator | ✅ | ✅ | ✅ | ✅ | — |

所有 P0/P1 插件互相兼容，无冲突。

---

## 四、在线平台页面设计

### 4.1 首页 — 项目配置

```
┌──────────────────────────────────────────────────┐
│  solo-spring-scaffold                             │
│  ────────────────────                             │
│  Spring Boot 3.x 项目生成器                       │
│                                                    │
│  项目名称:  [my-app          ]                    │
│  包名:      [com.example.myapp]                   │
│  JDK:       ○ 17  ● 21  ○ 25                     │
│                                                    │
│  数据库:    ○ H2 (内存)  ● MySQL  ○ PostgreSQL    │
│  MySQL URL: [jdbc:mysql://localhost:3306/mydb]    │
│                                                    │
│                [下一步：选择模块 →]                 │
└──────────────────────────────────────────────────┘
```

### 4.2 模块选择页

```
┌──────────────────────────────────────────────────┐
│  选择模块                                          │
│                                                    │
│  ┌─────────────────┐  ┌─────────────────┐        │
│  │ 🔒 Security     │  │ 🐳 Docker       │        │
│  │ JWT 认证/鉴权    │  │ 容器化部署       │        │
│  │ ● 已选择        │  │ ● 已选择        │        │
│  └─────────────────┘  └─────────────────┘        │
│                                                    │
│  ┌─────────────────┐  ┌─────────────────┐        │
│  │ 🧪 Test         │  │ 📊 Actuator     │        │
│  │ 单元/集成测试    │  │ 监控 + Prometheus│        │
│  │ ○ 未选择        │  │ ● 已选择        │        │
│  └─────────────────┘  └─────────────────┘        │
│                                                    │
│  ┌─────────────────┐                              │
│  │ 🔴 Redis        │    PRO 标识                   │
│  │ 缓存 + Spring   │    ────────                   │
│  │   Cache         │    高级功能需订阅              │
│  │ 🔒 Pro          │                               │
│  └─────────────────┘                              │
│                                                    │
│                [下一步：预览 →]                     │
└──────────────────────────────────────────────────┘
```

### 4.3 代码预览页

```
┌──────────────────────────────────────────────────┐
│  项目预览                                          │
│                                                    │
│  ┌────────────┐  ┌─────────────────────────────┐ │
│  │ 📁 my-app/ │  │ SecurityConfig.java          │ │
│  │  📁 common │  │                              │ │
│  │   📁 securi│  │ @Configuration               │ │
│  │    Securit.│  │ @EnableWebSecurity            │ │
│  │    JwtToke│  │ @EnableMethodSecurity          │ │
│  │    JwtAuth│  │ @RequiredArgsConstructor       │ │
│  │    JwtEntr│  │ public class SecurityConfig { │ │
│  │    Securit│  │                                │ │
│  │   📁 api  │  │   @Bean                       │ │
│  │   📁 excep│  │   public SecurityFilterChain  │ │
│  │   📁 web  │  │     securityFilterChain(...)  │ │
│  │  📁 dao   │  │   {                           │ │
│  │  📁 service│  │     http.sessionManagement() │ │
│  │  📁 web   │  │     ...                       │ │
│  │   📁 auth │  │   }                           │ │
│  │    AuthCo.│  │ }                              │ │
│  │  📄 pom.xml│  │                              │ │
│  └────────────┘  └─────────────────────────────┘ │
│                                                    │
│  [⬇ 下载 ZIP]  [🐙 推送 GitHub]  [📋 复制 CLI]   │
└──────────────────────────────────────────────────┘
```

### 4.4 CLI 命令同步

在线平台生成的配置，自动生成对应的 CLI 命令：

```bash
# 用户在线选择: security + docker + actuator
# 平台显示等效命令:

python solo-spring-scaffold init my-app \
    --package com.example.myapp \
    --jdk 21 \
    --plugins security,docker,actuator
```

---

## 五、商业模式

### 5.1 分层定价

| 层级 | 包含插件 | 价格 | 目标用户 |
|------|---------|------|---------|
| **Free** | 基础骨架 + security + docker + test | ¥0 | 个人开发者 |
| **Pro** | + redis + actuator + 多 Domain + 审计日志 | ¥199/年 | 小团队 |
| **Enterprise** | + 微服务 + K8s Helm + 分布式锁 + 配置中心 + 定制 | ¥599/年 | 企业 IT |

### 5.2 收入来源

| 来源 | 占比预期 | 说明 |
|------|---------|------|
| Pro 订阅 | 40% | ¥199/年，目标 500 用户 = ¥10 万/年 |
| Enterprise 订阅 | 30% | ¥599/年，目标 100 用户 = ¥6 万/年 |
| 定制服务 | 20% | ¥5,000-20,000/次，按需 |
| 培训/咨询 | 10% | Spring Boot 架构培训 |

### 5.3 付费功能实现

**技术方案：** 插件标记 `tier: pro` 或 `tier: enterprise`

```yaml
# plugins/redis/plugin.yaml
name: redis
tier: pro              # 标记为 Pro 功能
# ...
```

**在线平台：** Pro 插件显示 🔒 标识，点击后引导订阅。

**CLI：** `--plugins redis` 需要验证 License Key。

```bash
# 免费用户
python solo-spring-scaffold init my-app --plugins security,docker

# Pro 用户（需 License Key）
export SOLO_LICENSE=xxx
python solo-spring-scaffold init my-app --plugins security,docker,redis,actuator
```

License 验证逻辑：
1. CLI 本地缓存认证令牌（~/.solo-spring-scaffold/license）
2. 离线时检查令牌有效期
3. 在线时验证服务端

---

## 六、技术实施计划

### Phase 1：MVP（2 周）

| 任务 | 工作量 | 产出 |
|------|--------|------|
| FastAPI 后端骨架 | 2 天 | /api/generate, /api/plugins |
| 复用 CLI 生成逻辑 | 1 天 | 统一引擎 |
| 简单前端（配置 + 下载） | 3 天 | 单页应用 |
| ZIP 生成 + 下载 | 1 天 | 下载端点 |
| 部署到 Vercel + Railway | 1 天 | 线上可访问 |

### Phase 2：体验优化（2 周）

| 任务 | 工作量 | 产出 |
|------|--------|------|
| 代码预览（Monaco Editor） | 3 天 | 在线浏览生成代码 |
| 架构图可视化 | 2 天 | 模块依赖关系图 |
| GitHub OAuth 推送 | 2 天 | 一键推送到仓库 |
| 插件兼容性验证 | 1 天 | 前端提示冲突 |
| pip install 分发 | 1 天 | PyPI 发布 |

### Phase 3：商业化（4 周）

| 任务 | 工作量 | 产出 |
|------|--------|------|
| Pro/Enterprise 插件 | 5 天 | 5+ 付费插件 |
| License 系统 | 3 天 | Key 验证 + 支付集成 |
| 文档网站 | 3 天 | Docusaurus 站点 |
| SEO + 内容营销 | 持续 | 博客 + 教程 |
| 行业模板 | 5 天 | 电商/SaaS 模板 |

---

## 七、竞品对比

| 维度 | Spring Initializr | JHipster | RuoYi | JeecgBoot | **我们** |
|------|-------------------|----------|-------|-----------|---------|
| 定位 | 空项目生成 | 全栈生成器 | 完整后台 | 低代码平台 | **可插拔骨架** |
| 自定义程度 | 低 | 中 | 低 | 低 | **高** |
| 上手成本 | 极低 | 高 | 中 | 中 | **低** |
| 生成质量 | 空壳 | 重 | 完整 | 完整 | **生产就绪** |
| 可插拔 | ❌ | 部分 | ❌ | ❌ | **✅ 核心** |
| 在线生成 | ✅ | ✅ | ❌ | ❌ | **✅** |
| 商业模式 | 免费开源 | 咨询 | 双版本 | 双版本 | **订阅 + 定制** |
| 目标用户 | 所有人 | 企业 | 外包 | 企业 | **外包 + 中小企业** |

**我们的护城河：** 可插拔 + 在线生成 + 极低上手成本。没有任何竞品同时做到这三点。

---

## 八、域名与品牌

| 项目 | 建议 |
|------|------|
| 域名 | `solo-scaffold.dev` 或 `spring-scaffold.com` |
| Slogan | "30 秒，生产级 Spring Boot 项目" |
| Logo | 🧱 积木 + 🌱 Spring 叶子的组合 |
| GitHub | github.com/solo-spring-scaffold |
