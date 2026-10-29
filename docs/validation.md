# Kết quả kiểm chứng trong workspace

- Backend đóng gói thành công bằng Maven 3.9.9; bytecode major 55 (Java 11). Chạy 5 test tích hợp trên Java 11.0.32.1, MySQL 8.0.41 Testcontainers: 5 passed, 0 failed, 0 skipped.
- Bao gồm duyệt hai tầng/cấm tự duyệt, xuất lặp không trừ thêm, rollback thiếu tồn, hai đơn cạnh tranh không bán vượt tồn, HTTP authentication/authorization.
- Generator Python: 2 passed. React/Vite production build thành công, khoảng 52 KB gzip JavaScript.
- JAR chạy thực tế trong container eclipse-temurin:11-jre-jammy, kết nối MySQL và Redis 7.4.2. HTTP smoke passed: health, auth, product keyset, dashboard hai lượt (Redis cache), lịch sử khách hàng, anonymous 401.
- Import file mặc định `demo.sql` thành công trên MySQL với FK và CHECK bật. Số dòng: products 100000; customers 20000; employees 100; inventory_balances 300000; stock_movements 300000; sales_orders 100000; order_lines 100000; stock_receipts 10000; receipt_lines 10000; audit_logs 100000.
- Đối soát balance với SUM(movement): 0 lệch. Tổng đơn so với SUM(quantity * unit_price): 0 lệch.
- Source frontend đã định dạng bằng Prettier. Chưa chạy kiểm thử trình duyệt E2E, load test k6 hoặc kiểm chứng HA/production. Dockerfile multi-stage và toàn bộ đường `docker compose up --build` chưa được chạy end-to-end; phiên kiểm chứng dùng Maven trên host, Java 11 runtime container và MySQL/Redis Compose.

File dữ liệu lớn được sinh ở `/workspace/stationery-data/demo.sql`, không đưa vào Git. Có thể tái tạo bằng generator trong repository. Mật khẩu cục bộ được sinh ngẫu nhiên trong `.env` (ignored), không ghi vào báo cáo. Các test dùng mật khẩu test riêng, không dùng thông tin xác thực thật.
