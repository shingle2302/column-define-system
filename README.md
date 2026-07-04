# Column Define System — 列自定义管理系统

企业级列定义管理系统，提供「列元数据定义 + 运行时列配置」双重能力。管理员通过管理后台配置列模板（预设），终端用户可在此基础上自定义列的显示/隐藏/宽度/固定位置，通过合并算法实现模板与用户偏好的增量合并。

## 项目架构

```
column-define-system/
├── packages/
│   ├── core/          @cds/core     — TypeScript 核心：类型定义 + composables + Vue 组件
│   └── ui/            @cds/ui       — Vue 3 UI 层：re-export core 组件 + 列配置器存根
├── demo/              @cds/demo     — Vite 开发 playground（CRM 场景演示）
├── admin/             @cds/admin    — 管理后台 SPA（Vue 3 + Vue Router + Ant Design Vue）
├── backend/           Spring Boot   — Java 后端（Spring Boot 3.2 + MyBatis-Plus + H2 + JWT）
├── docs/plans/                      — 系统设计文档
└── ColumnDefine.postman_collection.json — API 测试集合（31 个接口）
```

## 技术栈

| 层 | 技术 |
|----|------|
| 前端核心 | TypeScript, Vue 3 (Composition API) |
| 管理后台 | Vue 3, Vue Router, Ant Design Vue 4 |
| 开发演示 | Vite 5, pnpm workspace |
| 后端 | Spring Boot 3.2, MyBatis-Plus 3.5, H2, JWT |
| 构建工具 | pnpm, Maven |

## 快速开始

### 前置要求

- Node.js >= 18
- pnpm >= 8
- Java 17+
- Maven 3.8+

### 安装依赖

```bash
# 安装前端依赖
pnpm install

# 编译后端
cd backend && mvn clean compile
```

### 启动开发环境

```bash
# 方式一：仅启动前端 demo（需先启动后端）
pnpm dev

# 方式二：同时启动前后端
pnpm dev:all

# 方式三：仅启动后端
pnpm dev:server
```

- Demo 前端：`http://localhost:5173`（端口可能自动递增）
- Admin 后台：`http://localhost:5174`
- 后端 API：`http://localhost:8080`
- H2 控制台：`http://localhost:8080/h2-console`

### 数据库说明

后端默认使用 **H2 内存数据库**（`jdbc:h2:mem:column_define`），数据仅在进程运行期间存在，重启后丢失并自动从 `schema.sql` + `data.sql` 重新初始化。

如需数据持久化，可将 `application.yml` 中的数据源 URL 改为文件模式：

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/column_define;MODE=MySQL
```

H2 控制台默认开放（`/h2-console`），连接信息：

| 项 | 值 |
|----|-----|
| JDBC URL | `jdbc:h2:mem:column_define` |
| 用户名 | `sa` |
| 密码 | （空） |

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | alice / bob | alice123 / bob123 |
| 观察者 | viewer | viewer123 |

## 管理后台

管理后台提供完整的列定义配置能力，支持三种角色：

| 角色 | 权限 |
|------|------|
| **admin** | 全部功能：系统/业务/预设/列定义/用户覆盖/角色预设/用户/角色管理 |
| **user** | 预设/列定义/用户覆盖/角色预设管理 |
| **viewer** | 只读查看预设/用户覆盖/角色预设 |

### 功能概览与截图

#### 登录页

支持配置后端服务器地址，多角色账号登录。

![Admin 登录](docs/screenshots/admin-login.png)

#### 仪表盘

展示接入系统数、业务模块数、预设方案数、列定义数等核心指标。

![仪表盘](docs/screenshots/admin-dashboard.png)

#### 系统管理

管理接入的业务系统（如 CRM、HR），每个系统下可配置独立的业务模块和列定义。

![系统管理](docs/screenshots/admin-systems.png)

支持新增/编辑系统名称、编码和状态。

![新增系统](docs/screenshots/admin-systems-add.png)

#### 用户管理

管理系统用户及其角色分配，支持管理员、普通用户、观察者三种角色。

![用户管理](docs/screenshots/admin-users.png)

![新增用户](docs/screenshots/admin-users-add.png)

#### 角色管理

管理系统中可用的角色定义（admin / user / viewer），控制不同角色的菜单和操作权限。

![角色管理](docs/screenshots/admin-roles.png)

![新增角色](docs/screenshots/admin-roles-add.png)

#### 业务管理

管理每个系统下的业务模块，业务模块是预设方案的归属单元。

![业务管理](docs/screenshots/admin-businesses.png)

![新增业务](docs/screenshots/admin-businesses-add.png)

#### 预设管理

管理列预设方案。预设方案是一组列的模板，定义了列的默认宽度、可见性、固定位置、对齐方式等属性。每个预设可关联到指定的系统和业务。

![预设管理](docs/screenshots/admin-presets.png)

![新增预设](docs/screenshots/admin-presets-add.png)

#### 列定义管理

管理预设方案下的列分组和列定义。选择预设后，可对每个分组的列进行增删改：设置字段名、类型、宽度范围、是否可见、是否锁定、是否可排序/筛选等。

![列定义管理](docs/screenshots/admin-columns.png)

点击「加列」弹出新增列定义表单，配置字段名、类型、宽度、对齐、锁定等属性。

![新增列定义](docs/screenshots/admin-columns-add.png)

#### 角色预设管理

按角色维度管理列配置覆盖。例如可单独为 admin 角色或 viewer 角色设置不同的列显隐/宽度/顺序，优先级低于用户级覆盖。

![角色预设管理](docs/screenshots/admin-role-presets.png)

![新增角色覆盖](docs/screenshots/admin-role-presets-add.png)

#### 用户覆盖管理

管理单个用户对预设列的个性化覆盖。用户可以在预设模板基础上调整列的排序、宽度、可见性和固定位置，系统自动合并生成最终列配置。

![用户覆盖管理](docs/screenshots/admin-overrides.png)

![新增用户覆盖](docs/screenshots/admin-overrides-add.png)

## 核心组件

### @cds/core

核心库，零 UI 依赖。提供所有类型定义和 composables。

```ts
import { usePresetFetcher, useColumnConfig, ColumnTable, ColumnPresetPicker } from '@cds/core'
import type { ColumnPreset, ColumnDefItem, ApiClient } from '@cds/core'
```

### @cds/ui

UI 组件库，依赖 `@cds/core`。Re-export 核心组件，提供 `ColumnConfigurator` 存根。

### CrmTable（列数据表格组件）

完整的列数据表格组件，内置预设选择器、列自定义弹窗和表格渲染。一行代码即可集成：

```vue
<CrmTable
  server-url="http://localhost:8080"
  :token="authToken"
  user="alice"
  password="alice123"
  system-id="sys001"
  business-id="biz_crm_customer"
  :data="tableData"
>
  <template #header="{ column }">...</template>
  <template #cell="{ column, value }">...</template>
</CrmTable>
```

> **安全提示**：`user` / `password` 用于 `ColumnConfigurator` 内部独立获取认证 token 以保存用户覆盖。生产环境建议在组件外部完成登录，仅传入 `token`，避免在前端组件间传递明文密码。

### ColumnConfigurator（列配置面板）

独立的列配置弹窗，集中管理列的显示/隐藏、宽度、固定位置等所有设置。

## Demo 演示

CRM 场景演示项目，展示客户/线索/合同三个业务模块的列自定义能力，包含完整的登录认证流程。

### 核心概念

#### ColumnPreset（列预设）

完整的列定义方案，包含多个 ColumnGroup，每个 Group 包含若干 ColumnDefItem。

```
ColumnPreset
├── id, name, description, version
└── groups: ColumnGroup[]
    ├── id, label, collapsed
    └── children: ColumnDefItem[]
        ├── id, label, field, type
        ├── width, minWidth, maxWidth
        ├── visible, pinned, locked
        ├── sortable, filterable, resizable
        └── align, order
```

#### 合并算法

系统通过三级优先级链生成最终列配置，优先级从高到低为：

```
用户覆盖（User Override） > 角色预设（Role Preset） > 模板预设（Preset）
```

合并规则（增量合并，仅覆盖有差异的字段）：

- 用户主动设置 `visible=false` 的列 → 隐藏；未覆盖的列 → 沿用模板预设默认值
- 用户自定义宽度 → 覆盖预设宽度
- 用户调整顺序 → 覆盖预设顺序
- 用户固定列 → 覆盖预设固定位置
- 角色预设中的覆盖 → 作为中间层，当用户未覆盖时生效
- 其余字段 → 沿用优先级更高的层级，最终回退到模板预设默认值

### 截图

#### 登录页

![登录页](docs/screenshots/login-page.png)

#### CRM 客户管理

![客户管理](docs/screenshots/crm-customer.png)

#### CRM 线索管理

![线索管理](docs/screenshots/crm-lead.png)

#### CRM 合同管理

![合同管理](docs/screenshots/crm-contract.png)

#### 列自定义配置弹窗

![列自定义](docs/screenshots/column-configurator.png)

#### 关于组件

![关于组件](docs/screenshots/about-page.png)

## Postman 接口测试

导入 `ColumnDefine.postman_collection.json` 到 Postman，包含 31 个接口：

- 认证：`POST /api/auth/login`
- 公共查询：系统/业务/预设列表
- 用户覆盖：获取/保存覆盖配置
- 管理中心：用户/角色/系统/业务/列定义/列组/预设/角色预设的 CRUD

## 构建

```bash
# 构建所有前端包
pnpm build

# 单独构建后端
cd backend && mvn package
```

## 环境配置

后端配置文件位于 `backend/src/main/resources/application.yml`，关键配置项如下：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | `8080` | 后端服务端口 |
| `spring.datasource.url` | `jdbc:h2:mem:column_define` | 数据源连接 URL（改为 `jdbc:h2:file:...` 可持久化） |
| `spring.h2.console.enabled` | `true` | 是否开放 H2 控制台（生产环境建议关闭） |
| `jwt.secret` | `cds-jwt-secret-2026` | JWT 签名密钥（**生产环境务必更换为强随机字符串**） |
| `jwt.expiration` | `604800000` | Token 有效期（毫秒），默认 7 天 |
| `mybatis-plus.configuration.log-impl` | `StdOutImpl` | SQL 日志输出（生产环境建议改为 `NoLoggingImpl` 或删除） |

前端 API 基地址通过组件的 `server-url` prop 或管理后台登录页的服务器地址输入框配置。

## 部署指南

### 后端部署

```bash
# 1. 构建 JAR
cd backend && mvn clean package -DskipTests

# 2. 运行
java -jar target/column-define-backend-1.0.0.jar \
  --server.port=8080 \
  --spring.datasource.url=jdbc:h2:file:/data/column_define;MODE=MySQL \
  --jwt.secret=your-production-secret \
  --spring.h2.console.enabled=false

# 3. 后台运行（可选）
nohup java -jar target/column-define-backend-1.0.0.jar \
  --spring.profiles.active=prod > app.log 2>&1 &
```

> **生产环境检查清单**：
> - [ ] 更换 JWT 密钥为强随机字符串
> - [ ] 关闭 H2 控制台（`spring.h2.console.enabled=false`）
> - [ ] 切换数据源为文件模式或外部数据库（MySQL/PostgreSQL）
> - [ ] 关闭 SQL 日志输出
> - [ ] 配置 CORS 允许的域名（`CorsConfig.java`）

### 前端部署

```bash
# 1. 构建前端产物
pnpm build

# 2. 产物位于各包的 dist/ 目录，使用 Nginx 托管静态资源
```

Nginx 配置示例：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # Demo 前端
    location / {
        root /var/www/cds/demo/dist;
        try_files $uri $uri/ /index.html;
    }

    # Admin 后台
    location /admin {
        alias /var/www/cds/admin/dist;
        try_files $uri $uri/ /admin/index.html;
    }

    # 反向代理后端 API
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 项目结构说明

| 目录 | 说明 |
|------|------|
| `packages/core/src/index.ts` | 核心类型定义（ColumnGroup, ColumnDefItem, ColumnPreset） |
| `packages/core/src/usePresetFetcher.ts` | 预设远程拉取封装 |
| `packages/core/src/useColumnDef.ts` | 列定义查询与转换 |
| `packages/core/src/useColumnOverride.ts` | 用户覆盖数据管理 |
| `packages/core/src/uiStubs.ts` | 轻量级 UI 组件（ColumnTable, ColumnPresetPicker, useColumnConfig） |
| `packages/core/src/ColumnDataTable.vue` | 完整的列数据表格组件 |
| `packages/core/src/ColumnConfigurator.vue` | 列配置面板组件 |
| `admin/src/views/BusinessManage.vue` | 业务管理页 |
| `admin/src/views/ColumnManage.vue` | 列定义管理页（核心页面） |
| `admin/src/views/PresetManage.vue` | 预设方案管理页 |
| `admin/src/views/RolePreset.vue` | 角色预设分配页 |
| `admin/src/views/UserOverride.vue` | 用户覆盖管理页 |
| `backend/src/main/java/.../controller/` | REST API 控制器 |
| `backend/src/main/java/.../service/AdminService.java` | 核心业务逻辑（26KB） |
| `backend/src/main/java/.../entity/` | JPA 实体类 |
| `backend/src/main/resources/db/schema.sql` | 数据库表结构 |
| `backend/src/main/resources/db/data.sql` | 初始化种子数据 |

## License

[Apache License 2.0](LICENSE)