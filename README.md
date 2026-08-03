# javalibs — Thư viện dùng chung cho hệ sinh thái Spring Boot microservices

> ⚠️ **Dòng 2.0.x chạy trên Spring Boot 4.1.** Dòng 1.0.x (Spring Boot 3.5.3) nằm ở repo
> `javalibs` và đang ở chế độ bảo trì — chỉ sửa lỗi, không thêm tính năng.
> Breaking change khi nâng: [CHANGELOG-2.0.md](CHANGELOG-2.0.md).

Bộ thư viện nội bộ chuẩn hóa cách xây dựng microservices với **Spring Boot 4.1.x / Java 21**, được phân rã thành các module độc lập để mỗi service chỉ "nhúng" đúng những gì nó cần — giảm dung lượng container và tối ưu thời gian khởi động.

> 📚 **Tài liệu đầy đủ: [docs/index.md](docs/index.md)** — [Getting Started](docs/getting-started.md) · [Kiến trúc](docs/architecture.md) · [Tham chiếu cấu hình](docs/configuration-reference.md) · [Cookbook](docs/cookbook.md) · [Vận hành](docs/operations.md) · [Đóng góp](docs/contributing.md) · Tham chiếu chi tiết từng module tại [docs/modules/](docs/modules/)

## Kiến trúc tổng thể

Mỗi module phức tạp được phân rã theo chuỗi 4 tầng chuẩn:

```
[core]  ──►  [spring]  ──►  [spring-boot-autoconfigure]  ──►  [spring-boot-starter]
 Java thuần     Tích hợp        Cấu hình tự động có điều kiện      Điểm chạm duy nhất
 (0 Spring)     framework       (@ConditionalOn*)                  của client (chỉ pom)
```

- **core**: logic thuần Java, không phụ thuộc Spring → unit test tính bằng mili-giây.
- **spring**: nhúng logic core vào hệ sinh thái Spring (filter, resolver, publisher...).
- **autoconfigure**: các `@AutoConfiguration` đăng ký qua `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, mọi dependency bên thứ ba đều `<optional>true</optional>`, mọi bean đều `@ConditionalOnMissingBean` để service ghi đè được.
- **starter**: không chứa code, chỉ gom dependency — developer chỉ cần khai báo 1 artifact.

## Danh sách module

| Module | Vai trò | Client thường dùng |
|---|---|---|
| `javalibs-root` | Parent POM: properties, pluginManagement, chốt version third-party | (chỉ nội bộ javalibs) |
| `javalibs-dependencies` | **BOM** chốt version mọi artifact javalibs | Import vào `dependencyManagement` |
| `javalibs-internal` | Tiện ích thuần Java: Strings, Dates, Checks, Ids | Tự động kéo theo (transitive) |
| `javalibs-spring` | Mở rộng Spring core: ApplicationContextProvider, chẩn đoán bean chậm, Profiles | Khai báo trực tiếp khi cần |
| `javalibs-ddd-core` / `-spring` | AggregateRoot, Entity, ValueObject, DomainEvent, BusinessRule, DomainEventPublisher | `javalibs-ddd-spring` |
| `javalibs-cqrs-*` | Command/Query/Handler + CommandBus/QueryBus in-memory | `javalibs-cqrs-spring-boot-starter` |
| `javalibs-web-*` | GlobalExceptionHandler, chuẩn JSON lỗi, PageResponse, request logging, CORS | `javalibs-web-spring-boot-starter` |
| `javalibs-security-*` | Xác thực JWT stateless, UserContext, `@RequireRole`, `@CurrentUser`, phát hành token (`javalibs-security-issuer`: login/refresh/logout, rotation + reuse detection) | `javalibs-security-spring-boot-starter` (+ `javalibs-security-issuer` để phát hành token) |
| `javalibs-security-test` | `JwtTestFactory` sinh token giả lập cho test | scope `test` |
| `javalibs-authz-*` | Phân quyền per-resource kiểu YouTrack: Permission → Role → Grant → Scope, `@RequirePermission`, `PermissionChecker`, cache Caffeine, schema JPA mặc định | `javalibs-authz-spring-boot-starter` (+ `javalibs-authz-jpa` cho persistence mặc định) |
| `javalibs-datahub-*` | EventEnvelope + Kafka publisher, REST client có retry mặc định | `javalibs-datahub-spring-boot-starter` |
| `javalibs-search-*` | HTTP params → JPA Specifications (dynamic filtering) | `javalibs-search-spring-boot-starter` |
| `javalibs-observability-*` | Correlation ID, MDC propagation, common metric tags, tracing | `javalibs-observability-spring-boot-starter` |
| `javalibs-cache-*` | Redis/Caffeine cache: key convention, JSON serialize, TTL theo cache | `javalibs-cache-spring-boot-starter` |
| `javalibs-resilience-*` | Resilience4j: Circuit Breaker, Retry, Rate Limiter với default platform | `javalibs-resilience-spring-boot-starter` |
| `javalibs-openapi-*` | Swagger/OpenAPI chuẩn hóa: bearer scheme, mã lỗi toàn cục trong docs | `javalibs-openapi-spring-boot-starter` |
| `javalibs-persistence-*` | Flyway conventions: clean-disabled, naming validation | `javalibs-persistence-spring-boot-starter` |
| `javalibs-test` | BaseIntegrationTest + Testcontainers PostgreSQL, TestData, Jsons | scope `test` |

## Bắt đầu nhanh (trong một microservice)

**1. Import BOM** — một lần duy nhất, sau đó mọi artifact javalibs không cần version:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.javalibs</groupId>
      <artifactId>javalibs-dependencies</artifactId>
      <version>2.0.0-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

**2. Khai báo starter cần dùng:**

```xml
<dependencies>
  <dependency>
    <groupId>io.javalibs</groupId>
    <artifactId>javalibs-web-spring-boot-starter</artifactId>
  </dependency>
  <dependency>
    <groupId>io.javalibs</groupId>
    <artifactId>javalibs-security-spring-boot-starter</artifactId>
  </dependency>
  <dependency>
    <groupId>io.javalibs</groupId>
    <artifactId>javalibs-test</artifactId>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>io.javalibs</groupId>
    <artifactId>javalibs-security-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

**3. Cấu hình trong `application.yml`** (mọi thuộc tính nằm dưới namespace `javalibs.*`, xem README từng module):

```yaml
javalibs:
  security:
    jwt:
      secret: ${JWT_SECRET}
  web:
    cors:
      enabled: true
      allowed-origins: ["https://app.example.com"]
  observability:
    metrics:
      common-tags:
        team: payments
```

Spring Boot sẽ tự quét file `AutoConfiguration.imports` của từng starter và kích hoạt cấu hình tương ứng — không cần `@Import` hay `@ComponentScan` thêm.

## Các năng lực production-grade

- **Transactional Outbox + Idempotent Consumer** (`javalibs-datahub`): loại bỏ dual-write DB↔Kafka, dedup message giao trùng — bật qua `javalibs.datahub.outbox.enabled` / `javalibs.datahub.idempotency.enabled`, DDL tham chiếu tại `META-INF/datahub/outbox-schema-postgres.sql`.
- **Token revocation** (`javalibs.security.blacklist.mode=redis`) + **chế độ OAuth2 Resource Server** (`javalibs.security.mode=oauth2-resource-server` với Keycloak/OIDC) — cùng một API `UserContext`/`@RequireRole` cho cả hai chế độ.
- **Circuit Breaker chống cascading failure**: bật `javalibs.resilience.rest.enabled=true` để bảo vệ mọi RestClient.
- **Business error codes**: định nghĩa catalog `BusinessErrorCode.of("ERR-USER-001", 404)` (javalibs-web-core) — code ổn định cho frontend, tự xuất hiện trong OpenAPI docs.

## Build toàn bộ

```bash
mvn clean install          # build + test tất cả module
mvn clean install -Prelease  # kèm sources + javadoc jar (khi publish)
```

Yêu cầu: JDK 21+, Maven 3.6.3+ (enforcer sẽ chặn nếu thiếu).

## Publish lên GitHub Packages

> Repo GitHub và pipeline publish thật sự được tạo ở Task 13 (đang chờ người chủ dự án duyệt,
> chưa chạy). Nội dung dưới đây là đích cấu hình cho dòng 2.0.x — thay `<OWNER>` bằng
> tổ chức/tài khoản GitHub thật khi Task 13 hoàn tất.

Khi Task 13 chạy, `distributionManagement` trong root POM và BOM sẽ đổi từ cặp
`javalibs-releases`/`javalibs-snapshots` (Nexus, dùng ở dòng 1.0.x — vẫn là cấu hình hiện tại
trong `pom.xml` cho tới khi đó) sang một repository Maven duy nhất của GitHub Packages phục vụ
cả release lẫn snapshot:

```xml
<distributionManagement>
  <repository>
    <id>github</id>
    <name>GitHub Packages — javalibs</name>
    <url>https://maven.pkg.github.com/<OWNER>/javalibs-4.1</url>
  </repository>
</distributionManagement>
```

Deploy:

```bash
mvn -B -DskipTests deploy -Prelease
```

Credentials: GitHub Packages xác thực bằng personal access token (scope `write:packages`,
`read:packages`) hoặc `GITHUB_TOKEN` mặc định trong GitHub Actions — khai trong `settings.xml`:

```xml
<servers>
  <server>
    <id>github</id>
    <username>${env.GITHUB_ACTOR}</username>
    <password>${env.GITHUB_TOKEN}</password>
  </server>
</servers>
```

Service muốn **tiêu thụ** artifact từ GitHub Packages (thay vì chỉ publish) phải khai cùng
`<repository>` id `github` ở `<repositories>` của POM và cùng cặp credentials — GitHub Packages
yêu cầu xác thực kể cả khi chỉ đọc package công khai trong cùng tổ chức. Workflow mẫu:
`.github/workflows/ci.yml` — build + test mọi PR, deploy SNAPSHOT từ `master`, release từ tag
`v*`.

## Quy tắc thiết kế (khi thêm module mới)

1. **core không phụ thuộc Spring** — nếu cần Spring, code đó thuộc tầng `spring`.
2. Trong `autoconfigure`, mọi thư viện bên thứ ba phải `<optional>true</optional>`; bean luôn có `@ConditionalOnMissingBean` và cờ bật/tắt `javalibs.<module>.enabled`.
3. `starter` tuyệt đối không chứa code Java — chỉ `pom.xml` + `README.md`.
4. Version artifact mới phải được thêm vào `javalibs-dependencies` (BOM).
5. Không dùng Lombok trong thư viện; ưu tiên `record` và immutability.
