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
