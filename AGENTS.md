# AGENTS.md
> 参与此项目的AI编码代理的指导原则。

## 项目概述
**grid** 是一个基于maven构建的交易管理平台。它允许用户保存交易记录，计算涨跌幅，提供各种交易报表查询

### 技术架构
- **语言：** Java 17
- **框架：** SpringBoot 3.5.11，Mybatis-Plus3.0.5
- **数据库：** postgresql@18
- **缓存：** Redis
### 项目结构

```markdown
grid/
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── jerryz/
        │           └── grid/
        │               ├── api/          # API接口定义
        │               ├── config/       # 配置类（MybatisPlus、Web、过滤器等）
        │               ├── controller/   # 控制器层（处理HTTP请求）
        │               │   ├── assetCode/        # 资产代码相关接口
        │               │   └── positionRecord/   # 持仓记录相关接口
        │               ├── em/           # 枚举类定义
        │               ├── handle/       # 全局异常处理
        │               ├── mapper/       # 数据访问层（MyBatis Mapper）
        │               │   └── xml/      # Mapper XML文件
        │               ├── pojo/         # 实体类/传输对象
        │               │   ├── po/       # 持久化对象（数据库实体）
        │               │   ├── ro/       # 请求/响应对象
        │               │   └── vo/       # 视图对象
        │               ├── schedule/     # 定时任务
        │               ├── service/      # 业务逻辑层
        │               │   ├── impl/     # 业务实现类
        │               │   └── strategy/ # 策略模式（交易处理策略）
        │               └── util/         # 工具类
        └── resources/  # 配置文件、静态资源

```

## maven命令
```bash
# 1.清理命令
# 清理 target 目录
mvn clean

# 强制清理（包括下载的依赖）
mvn clean -U

# 编译主代码
mvn compile

# 编译测试代码
mvn test-compile

# 只编译不运行测试
mvn compile -DskipTests


# 2.编译命令
# 编译主代码
mvn compile

# 编译测试代码
mvn test-compile

# 只编译不运行测试
mvn compile -DskipTests

# 3.测试命令
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=UserServiceTest

# 4.打包命令
# 打包（默认 jar）
mvn package

# 打包但不运行测试
mvn package -DskipTests

# 5.安装到本地仓库命令
# 安装到本地 maven 仓库
mvn install

# 安装但不运行测试
mvn install -DskipTests

# 强制重新安装
mvn install -U

```

## 代码规范指南

### Spring依赖注入

- 若类存在依赖注入，必须使用构造器注入，禁止使用字段注入，使用Lombok的@RequiredArgsConstructor简化构造函数生成

### service

- **service**包下新增的接口必须以I开头，Service结尾 示例如下
```java
  public interface IAssetCodeRecordService{
    
  }
```

- **service**包下方法返回值必须是 **com.jerryz.grid.pojo.ro.Result**（响应数据） 或 **com.jerryz.grid.pojo.ro.PageResult**（分页响应数据）示例如下
```java

Result<xxx> list();

PageResult<xxx> pageList();

```

- 一个接口的类方法/函数不能超过5个，若当前以到达上限，则新增接口类
- Result/PageResult中的泛型禁止使用  **com.jerryz.grid.pojo.po**包下的实体类，只允许使用和新增在 **com.jerryz.grid.pojo.ro**和**com.jerryz.grid.pojo.ro**包下的响应类

### mapper

- 命名必须要与 **com.jerryz.grid.pojo.po**下的实体类对应，例如pojo包含Test.java,Mapper命名规则必须是TestMapper.java
- 必须继承**com.baomidou.mybatisplus.core.mapper.BaseMapper**&lt;T&gt; T必须为对应的实体类
- SQL表结构为蛇形命名，例如**xxx_xxx**

### controller

- 方法注释必须说明接口用途
- POST请求无明确说明，默认请求数据类型为JSON，因此必须加上@RequestBody

## 边界

### 一直需要做的

- 必须在关键逻辑位，写上相对应的注释
- 完成新的代码编写以及旧代码变更/重构，必须编写相应的单元测试
- 在重构旧代码之后必须保证之前功能点可用且正常
- 必须遵守代码规范指南以及文件目录结构
- 若新增表必须创建sql文件在sql目录

### 需要询问

- 添加新增的pom依赖

### 禁止执行

- git推送远程操作
- 删除现有java文件