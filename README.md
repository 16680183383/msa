# MSA 微服务示例项目

本项目为一个基于 Spring Boot 的微服务架构示例，包含注册中心（Registry）、时间服务（Time-Service）、客户端（Client）、日志服务（Logging-Service）等模块，演示了服务注册与发现、心跳、服务间调用和日志采集等功能。

## 目录结构

```
msa/
├── client/           # 客户端服务，调用 time-service 并采集日志
├── Logging-Service/  # 日志采集服务，接收并存储日志
├── registry/         # 注册中心，服务注册/发现/同步
├── time-service/     # 时间服务，提供时间相关API
└── README.md
```

## 各模块简介

- **registry**  
  服务注册中心，支持多实例集群部署，提供服务注册、注销、心跳、发现等 REST API，并支持注册信息的集群同步。

- **time-service**  
  时间服务，注册到 registry，提供 `/api/getDateTime` 接口，支持多种时间格式返回。

- **client**  
  客户端服务，定时通过 registry 发现 time-service 并调用其 API，同时定时向 Logging-Service 发送日志。

- **Logging-Service**  
  日志采集服务，接收 client 发送的日志并存储，可通过 API 查询所有日志。

## 快速开始

### 1. 构建所有模块

在项目根目录下依次执行：

```sh
cd registry && ./mvnw clean package
cd ../time-service && ./mvnw clean package
cd ../Logging-Service && ./mvnw clean package
cd ../client && ./mvnw clean package
```

或使用如下命令批量构建：

```sh
for d in registry time-service Logging-Service client; do (cd $d && ./mvnw clean package); done
```

### 2. 启动服务

建议按如下顺序分别启动各服务（可用不同终端窗口）：

```sh
# 启动注册中心（可多实例，端口8180/8181）
cd registry
./mvnw spring-boot:run -Dspring-boot.run.profiles=default
# 或
./mvnw spring-boot:run -Dspring-boot.run.profiles=8181

# 启动时间服务（可多实例，端口8280/8281）
cd ../time-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=default
# 或
./mvnw spring-boot:run -Dspring-boot.run.profiles=8281

# 启动日志服务
cd ../Logging-Service
./mvnw spring-boot:run

# 启动客户端
cd ../client
./mvnw spring-boot:run
```

### 3. 主要接口说明

#### 注册中心（Registry）

- 服务注册：`POST /api/register`
- 服务注销：`POST /api/unregister`
- 心跳：`POST /api/heartbeat`
- 服务发现：`GET /api/discovery?name=serviceName`
- 查询所有服务：`GET /api/discovery`

#### 时间服务（Time-Service）

- 获取时间：`GET /api/getDateTime?style=full|date|time|unix`

#### 日志服务（Logging-Service）

- 发送日志：`POST /api/logs`
- 查询日志：`GET /api/logs`

#### 客户端（Client）

- 获取服务信息：`GET /api/getInfo`

### 4. 示例调用

获取当前时间（通过 client）：

```sh
curl http://localhost:8300/api/getInfo
```

查看日志采集服务中的日志：

```sh
curl http://localhost:8480/api/logs
```

### 5. 配置说明

各服务支持多实例部署，通过 `application-xxxx.properties` 文件指定端口和实例ID。  
注册中心支持集群同步，相关配置见 [registry/src/main/resources/application.properties](registry/src/main/resources/application.properties)。

### 6. 依赖环境

- JDK 21
- Maven 3.8+
- Spring Boot 3.5.x
