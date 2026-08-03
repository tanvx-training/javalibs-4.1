# javalibs 2.0.x — Breaking changes so với 1.0.x

Dòng 2.0.x chạy trên **Spring Boot 4.1** (1.0.x ở 3.5.3). Java baseline giữ nguyên **21**.
groupId `io.javalibs` và mọi artifactId giữ nguyên — chỉ dòng version đổi.

## Bắt buộc sửa khi nâng

_(bồi dần theo từng bậc migration)_

- **Spring Boot baseline: 3.5.3 → 4.1.x.** Service dùng javalibs 2.0.x phải chạy trên
  Spring Boot 4.1. Dòng 3.5 đã hết vòng đời OSS ngày 30/06/2026.
- **Java baseline giữ nguyên 21** — không cần nâng JDK khi lên 2.0.x.
- **`spring-boot-starter-web` → `spring-boot-starter-webmvc`.** `javalibs-web-spring-boot-starter`
  đã kéo starter mới; service nào tự khai `spring-boot-starter-web` phải tự đổi tên.
- **Starter OAuth2 đổi tên:** `spring-boot-starter-oauth2-resource-server` →
  `spring-boot-starter-security-oauth2-resource-server` (tương tự cho `-client`,
  `-authorization-server`). Service dùng mode `oauth2-resource-server` phải đổi.
- **Spring Security 7.** API nội bộ của javalibs-security đã cập nhật; `UserContext`,
  `@RequireRole`, `@CurrentUser` giữ nguyên chữ ký nên service không phải sửa code.
- **Testcontainers 1.x → 2.x.** Artifact `org.testcontainers:postgresql` →
  `org.testcontainers:testcontainers-postgresql`; package
  `org.testcontainers.containers.PostgreSQLContainer` →
  `org.testcontainers.postgresql.PostgreSQLContainer`; `junit-jupiter` không còn cần.
  Service tự khai container riêng phải đổi theo. Thêm nữa: lớp `PostgreSQLContainer` ở
  TC 2.x không còn generic tự-tham-chiếu (`PostgreSQLContainer<Self>`) — service khai kiểu
  tường minh dạng `PostgreSQLContainer<?> pg = ...` phải bỏ tham số generic
  (`PostgreSQLContainer pg = ...`).
- **`BaseIntegrationTest` nay mang `@AutoConfigureTestRestTemplate`** — Boot 4 không còn
  tự cấp `TestRestTemplate` cho `@SpringBootTest`. Test kế thừa không phải sửa gì;
  test tự khai `@SpringBootTest` mà dùng `TestRestTemplate` thì phải tự thêm annotation.
- **`TestRestTemplate` đổi package:** `org.springframework.boot.test.web.client.TestRestTemplate`
  → `org.springframework.boot.resttestclient.TestRestTemplate` (artifact
  `org.springframework.boot:spring-boot-resttestclient`). Service tự import lớp này (thay vì chỉ
  dùng qua `@Autowired` không khai kiểu tường minh — vẫn phải import đúng lớp) phải đổi.
- **Flyway autoconfigure dời package (bậc 7):** `org.springframework.boot.autoconfigure.flyway.*`
  → `org.springframework.boot.flyway.autoconfigure.*` (tên lớp `FlywayAutoConfiguration`,
  `FlywayConfigurationCustomizer` giữ nguyên). Artifact chứa các lớp này cũng đổi:
  `spring-boot-autoconfigure` → `org.springframework.boot:spring-boot-flyway` (khai
  `<optional>true</optional>` giống `flyway-core`). Service tự khai `@AutoConfiguration(beforeName
  = "...")`/`afterName` trỏ vào lớp Flyway của Boot, hoặc tự import
  `FlywayConfigurationCustomizer`, phải đổi cả chuỗi FQCN lẫn import.
- **`HibernateJpaAutoConfiguration` dời package:**
  `org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration` →
  `org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration` (artifact
  `org.springframework.boot:spring-boot-hibernate`, khai optional).
- **`@EntityScan` dời package:** `org.springframework.boot.autoconfigure.domain.EntityScan` →
  `org.springframework.boot.persistence.autoconfigure.EntityScan` (artifact
  `org.springframework.boot:spring-boot-persistence`, khai optional trong autoconfigure —
  thường đã có sẵn transitively qua `spring-boot-starter-data-jpa` ở runtime/test).
- **`@DataJpaTest`/`TestEntityManager` dời package và tách starter riêng:**
  `org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest` →
  `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`;
  `org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager` →
  `org.springframework.boot.jpa.test.autoconfigure.TestEntityManager`. `TestEntityManager`
  không còn kéo theo bởi `spring-boot-starter-data-jpa` (scope test) — service dùng
  `@DataJpaTest`/`TestEntityManager` phải đổi dependency test-scope sang
  `org.springframework.boot:spring-boot-starter-data-jpa-test` (kéo theo cả
  `spring-boot-starter-data-jpa` + `spring-boot-starter-test` + hỗ trợ slice test JPA).
- **Jackson 2 → 3 (bậc 8).** Class API chuyển sang `tools.jackson.*` (annotation vẫn ở
  `com.fasterxml.jackson.annotation.*`). Service tự inject `ObjectMapper` phải đổi import.
  Artifact `com.fasterxml.jackson.core:jackson-databind` → `tools.jackson.core:jackson-databind`;
  `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` không còn tồn tại — hỗ trợ
  `java.time` nay có sẵn trong `jackson-databind`, không cần đăng ký module. Ngoại lệ:
  `jjwt-jackson` (JJWT) chưa lên Jackson 3 nên `javalibs-security-issuer` vẫn kéo
  `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` (Jackson 2) làm phụ thuộc runtime
  cho riêng JJWT — không liên quan tới `ObjectMapper` mà service tự inject.
- **`DataSourceAutoConfiguration`/`DataSourceTransactionManagerAutoConfiguration` dời package:**
  `org.springframework.boot.autoconfigure.jdbc.*` → `org.springframework.boot.jdbc.autoconfigure.*`
  (artifact `org.springframework.boot:spring-boot-jdbc`, khai optional).
- **`KafkaAutoConfiguration` dời package:** `org.springframework.boot.autoconfigure.kafka.*` →
  `org.springframework.boot.kafka.autoconfigure.*` (artifact `org.springframework.boot:spring-boot-kafka`,
  khai optional).
- **`CacheAutoConfiguration` dời package:** `org.springframework.boot.autoconfigure.cache.*` →
  `org.springframework.boot.cache.autoconfigure.*` (artifact `org.springframework.boot:spring-boot-cache`,
  khai optional).
- **`RedisCacheConfigurations.jsonSerializer()` đổi kiểu trả về:**
  `GenericJackson2JsonRedisSerializer` → `GenericJacksonJsonRedisSerializer` (Spring Data Redis
  đổi tên lớp serializer theo phiên bản Jackson). Tên method và danh sách tham số (rỗng) giữ
  nguyên. Service gọi qua `var` hoặc chaining không bị ảnh hưởng; service khai kiểu tường minh
  `GenericJackson2JsonRedisSerializer` phải đổi.
- **resilience4j 2.3.0 → 2.4.0 (bậc 9), artifact `resilience4j-spring-boot3` →
  `resilience4j-spring-boot4`.** Artifact mới bị bỏ sót khỏi `resilience4j-bom` 2.4.0
  (resilience4j#2427; đã kiểm bằng cách tải trực tiếp `resilience4j-bom-2.4.0.pom` từ Maven
  Central và grep — BOM chỉ liệt kê `resilience4j-spring-boot3`, không có `-spring-boot4`) nên
  javalibs pin version tường minh trong `<dependencyManagement>` của `pom.xml` gốc — service
  import BOM `javalibs-dependencies` không phải làm gì thêm. Team dùng style annotation-driven
  (`@CircuitBreaker`/`@Retry`) phải tự đổi dependency sang `resilience4j-spring-boot4`.
  **Lưu ý package đổi khác quy luật thông thường:** artifact mới **không** dùng package
  `io.github.resilience4j.springboot4.*` như tên artifact gợi ý — package thực tế là
  `io.github.resilience4j.springboot.*` (bỏ hẳn số phiên bản Boot). Service tự khai
  `@AutoConfiguration(beforeName = "...")`/`afterName` trỏ vào lớp
  `CircuitBreakerAutoConfiguration`/`RetryAutoConfiguration`/`RateLimiterAutoConfiguration` của
  resilience4j phải đổi cả ba chuỗi FQCN.
- **`RestClientCustomizer` dời package:**
  `org.springframework.boot.web.client.RestClientCustomizer` →
  `org.springframework.boot.restclient.RestClientCustomizer` (artifact
  `org.springframework.boot:spring-boot-restclient`, khai optional). Service tự implement
  `RestClientCustomizer` (ví dụ để tùy biến `RestClient.Builder` do Boot cấp) phải đổi import.

## Thay đổi hành vi

_(mọi test bị sửa vì hành vi đổi — loại (b) — phải có mục ở đây)_

## Để lại cho sau

- **`RedisCacheConfigurations` dùng `activateDefaultTyping` với validator chấp nhận mọi
  subtype** (`LaissezFaireSubTypeValidator`). Đây là mẫu có rủi ro deserialization. Giữ
  nguyên ở 2.0.0 vì sửa nó là đổi hành vi; xử lý ở một thay đổi riêng có chủ đích.

## Mốc định dạng cache Redis ở dòng 1.0.x

Chụp trên Boot 3.5.3 / Jackson 2 bằng `RedisSerializerFormatTest`. Task 10 đối chiếu với
mốc này để biết Jackson 3 có làm đổi định dạng hay không:

```json
{"@class":"DumpFormat$Sample","name":"tour","day":["java.time.LocalDate","2026-08-02"]}
```
