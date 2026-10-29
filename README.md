# Paperflow — Quản lý bán hàng văn phòng phẩm

Bộ khung ứng dụng thực thi nghiệp vụ ban đầu: tài khoản/phân quyền, khách hàng, nhân viên, SKU, kho, phiếu nhập duyệt 2 tầng, đặt hàng, xuất hàng, lịch sử mua và audit. Java **11**, Spring Boot **2.7.18**, React **18**, MySQL **8**, Redis và Docker Compose.

## Chạy bằng Docker

Yêu cầu Docker Engine + Compose v2, Python 3 để sinh dữ liệu; khuyến nghị ít nhất 4 CPU, 8 GB RAM và 10 GB đĩa cho bộ demo lớn. Compose chỉ mở cổng web trên loopback; MySQL/Redis không mở cổng host.

```bash
cp .env.example .env
# Sửa bốn mật khẩu trong .env, không commit .env.
docker compose up -d --build
docker compose logs -f backend
```

Đợi backend báo Started Application; mở cổng web **8088** trên máy chạy Docker. Tài khoản `admin`, mật khẩu từ `BOOTSTRAP_ADMIN_PASSWORD` (12+ ký tự, tối đa 72 byte UTF-8). Bootstrap chỉ tạo admin khi DB chưa có tài khoản. Đổi biến sau đó không đổi mật khẩu trong DB.

## Dữ liệu mẫu lớn

Chỉ chạy **một lần** sau migration và bootstrap, trên DB demo mới chưa có dữ liệu nghiệp vụ. Không import vào DB đang sử dụng. Dữ liệu đặt hàng ở trạng thái PLACED, chưa trừ tồn; tồn đầu kỳ có movement đối ứng. File lớn được sinh ngoài Git.

```bash
mkdir -p data
python scripts/generate_data.py --output data/demo.sql
docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql --default-character-set=utf8mb4 -ustationery stationery' < data/demo.sql
```

Mặc định 100.000 sản phẩm, 20.000 khách, 100.000 đơn, 3 kho và các bảng liên quan: khoảng **1,04 triệu dòng**. Có thể tăng:

```bash
python scripts/generate_data.py --products 1000000 --customers 100000 --orders 1000000 --output data/large.sql
```

Generator dùng bộ nhớ theo batch và seed cố định. Import là một transaction lớn để tránh DB demo nửa chừng; với hàng triệu dòng cần đủ undo/redo/disk và thời gian. Lệnh mysql mặc định dừng nếu gặp lỗi, không dùng `--force`. Đối soát sau nạp:

```sql
SELECT COUNT(*) FROM products;
SELECT warehouse_id, COUNT(*) sku_count, SUM(quantity) quantity FROM inventory_balances GROUP BY warehouse_id;
SELECT b.id FROM inventory_balances b
LEFT JOIN (SELECT warehouse_id,product_id,SUM(quantity) quantity FROM stock_movements GROUP BY warehouse_id,product_id) m
ON m.warehouse_id=b.warehouse_id AND m.product_id=b.product_id
WHERE b.quantity<>COALESCE(m.quantity,0);
```

## Thử quy trình

1. Admin tạo sản phẩm/khách hàng hoặc nạp demo. ID kho: 1, 2, 3.
2. Trong Tài khoản, tạo 3 người dùng: người lập (vai trò kho ID 3), quản lý cấp 1 (ID 4), cấp 2 (ID 5). Mật khẩu 12+ ký tự. Admin ID vai trò 1, bán hàng 2.
3. Người lập tạo phiếu nhập. Đăng nhập quản lý 1 để duyệt, rồi quản lý 2 để duyệt; kiểm tra tồn kho tăng đúng số lượng.
4. Tạo đơn bán (ID khách, ID sản phẩm), giá được lấy từ danh mục. Mở chi tiết và Xuất hàng. Thiếu tồn trả lỗi và không xuất dở dang.
5. Lọc Đơn hàng theo ID khách để xem lịch sử mua. Xem Lịch sử thao tác để kiểm tra audit.

Giao diện có đăng nhập, dashboard, danh sách phân trang, lọc SKU, tạo/sửa danh mục, tạo nhiều dòng chứng từ, duyệt/từ chối, xuất đơn, khóa tài khoản và gán vai trò. API còn hỗ trợ sửa danh mục và tạo/sửa vai trò tùy chỉnh; UI trình biên tập quyền tùy chỉnh chưa có.

## Cấu trúc và API

- `backend/src/main/java/vn/stationery/{entity,dto,repository,mapper,service,controller,config}`
- `backend/src/main/resources/db/migration`: DDL, index, FK và RBAC seed.
- `frontend/src`: React, gọi API thật, không gán số liệu demo trên UI.
- [Thiết kế DB và giới hạn](docs/architecture.md).

API dưới `/api`: `/auth/csrf`, `/auth/me`, `/products`, `/customers`, `/employees`, `/warehouses`, `/inventory?warehouseId=1`, `/receipts`, `/orders?customerId=1`, `/accounts`, `/roles`, `/permissions`, `/audit`, `/dashboard`.

POST `/receipts` hoặc `/orders`:

```json
{"warehouseId":1,"customerId":1,"supplier":"Nhà cung cấp A","lines":[{"productId":1,"quantity":10,"unitPrice":5000}]}
```

POST `/receipts/{id}/decision`: `{"decision":"APPROVE","note":"Đã kiểm tra"}` hoặc REJECT. POST `/orders/{id}/fulfill` không cần body. GET chi tiết `/orders/{id}` và `/receipts/{id}`. Danh mục dùng GET/POST/PUT `/{id}`. GET `/products?after=0&size=25&sku=VPP` trả slice; dùng id cuối làm after trang sau. API khác dùng page/size, tối đa 100.

POST `/roles`: `{"code":"AUDITOR","name":"Kiểm toán","permissionIds":[16]}`. PUT `/roles/{id}` cùng body; ADMIN không sửa được. PUT `/accounts/{id}/roles`: `[2,3]`. GET `/accounts/{id}/roles`, `/roles/{id}/permissions` để lấy ID đang gán. GET `/permissions` lấy mã quyền hiện tại.

API dùng HTTP Basic và CSRF. Với client riêng: GET `/auth/csrf`, giữ cookie, gửi token trong header được trả về (`X-XSRF-TOKEN`) khi POST/PUT; gửi Basic auth qua HTTPS. Frontend thực hiện tự động.

## Phát triển và kiểm thử

Backend: JDK 11, Maven 3.9+, MySQL/Redis. Đặt `DB_URL`, `DB_USER`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PASSWORD`, `BOOTSTRAP_ADMIN_PASSWORD`, rồi chạy trong `backend`:

```bash
mvn spring-boot:run
# Cần Docker để Testcontainers tạo MySQL riêng:
mvn test
mvn package
```

Frontend (Node 22) trong `frontend`: `npm ci`, `npm run dev` hoặc `npm run build`. Vite proxy `/api` tới backend 8080.

Generator: `python3 -m unittest discover -s scripts -p 'test_*.py' -v`.

Kiểm thử tích hợp gồm migration + JPA validation, duyệt hai tầng, cấm tự duyệt, xuất lặp không trừ thêm, rollback thiếu tồn, cạnh tranh hai đơn cùng SKU và phân quyền endpoint. Không bỏ qua kiểm thử khi Docker thiếu; Maven sẽ báo lỗi.

Đây là phiên bản khởi đầu, chưa cam kết tải lớn/production. Xem giới hạn nghiệp vụ và bảo mật trong tài liệu thiết kế. Không dùng `docker compose down -v` trên dữ liệu cần giữ vì sẽ xóa volume DB.

## Đo tải trước khi triển khai

Có kịch bản đọc sản phẩm `scripts/load-test.js` cho k6 (20 → 100 virtual users). Đặt `BASE_URL`, `API_USER`, `API_PASSWORD` qua môi trường bảo mật; không đưa mật khẩu vào lịch sử lệnh. Đây là workload đọc, chưa đo thông lượng bán/nhập thực tế. Ghi lại cấu hình máy, p95, error rate, CPU, DB slow query và pool saturation trước khi tăng instance. Không coi số lượng seed là bằng chứng chịu được truy cập lớn.

[Kết quả kiểm chứng trong workspace](docs/validation.md). Script smoke HTTP đọc biến `API_URL`, `API_USER`, `API_PASSWORD` và giả định bộ dữ liệu mặc định đã nạp. Mật khẩu local nằm trong `.env` (ignored); không chia sẻ file này.


## Tiếng Việt bị lỗi sau khi nạp dữ liệu trên Windows

Không dùng `python ... | Out-File` hoặc `>` trong Windows PowerShell để xuất SQL tiếng Việt: dữ liệu có thể bị chuyển mã trước khi ghi ra file. Dùng `python scripts/generate_data.py --output data/demo.sql` để Python ghi UTF-8 trực tiếp, không BOM. File SQL tự khai báo `SET NAMES utf8mb4`.

Nếu đã nạp dữ liệu bị lỗi, **không nạp lại demo.sql** vì sẽ trùng ID. Làm theo [hướng dẫn sửa mã hóa dữ liệu mẫu](docs/repair-encoding.md). Công cụ chỉ khớp các biến thể mã hóa đã biết của dữ liệu demo; không đổi toàn bộ charset cột hoặc xóa DB.
