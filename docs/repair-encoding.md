# Sửa tiếng Việt của dữ liệu mẫu trên Windows

Các chuỗi dạng `Giß║Ñy` là byte UTF-8 bị giải mã bằng bảng mã OEM (cp437/cp850). Chuyển font CSS không sửa được giá trị này. Hướng dẫn dùng pipe PowerShell trước đây có thể gây lỗi: `-X utf8` điều khiển Python nhưng không điều khiển PowerShell giải mã đầu ra. `Out-File -Encoding utf8` chỉ mã hóa lại chuỗi đã sai.

## Dữ liệu mới

Trong thư mục dự án:

```powershell
python .\scripts\generate_data.py --output .\data\demo.sql
```

Mở file UTF-8 để xác nhận thấy `Giấy in`, `Cái` đúng. Chỉ import vào DB demo mới đủ migration và bootstrap; không chạy lại file INSERT trên DB đã nạp. Dùng client MySQL với `--default-character-set=utf8mb4`.

## Sửa dữ liệu đã nhập

Dừng các thao tác chỉnh sửa dữ liệu trong lúc sửa. Sao lưu bằng MySQL Workbench → Server → Data Export, chọn schema `stationery` và export cả cấu trúc/dữ liệu. Giữ bản sao lưu để phục hồi nếu kết quả không như mong đợi.

Tạo file xem trước (chưa UPDATE dữ liệu):

```powershell
python .\scripts\repair_seed_encoding.py --output .\data\encoding-preview.sql
docker compose cp .\data\encoding-preview.sql mysql:/tmp/encoding-preview.sql
docker compose exec mysql mysql --default-character-set=utf8mb4 -u root -p stationery
```

Nhập MYSQL_ROOT_PASSWORD trong `.env`. Tại dấu nhắc `mysql>`:

```sql
SOURCE /tmp/encoding-preview.sql;
```

Xem `matched_rows`, `current_value`, `proposed_value`. Giá trị mới phải đúng tiếng Việt. Nếu không khớp dòng nào trong khi DB vẫn lỗi, không sửa hàng loạt: dùng Workbench kiểm tra `SELECT id,sku,name,HEX(name) FROM products LIMIT 10;` để chẩn đoán bảng mã thực tế.

Khi đã kiểm tra preview, thoát MySQL (`EXIT;`) rồi tạo và chép file sửa:

```powershell
python .\scripts\repair_seed_encoding.py --apply --output .\data\encoding-repair.sql
docker compose cp .\data\encoding-repair.sql mysql:/tmp/encoding-repair.sql
docker compose exec mysql mysql --default-character-set=utf8mb4 -u root -p stationery
```

Trong MySQL:

```sql
SOURCE /tmp/encoding-repair.sql;
SELECT id,sku,name,category,unit FROM products ORDER BY id LIMIT 10;
```

Refresh trang sản phẩm. Nếu dùng Workbench, cũng có thể mở file SQL bằng UTF-8, chọn đúng schema `stationery` và chạy toàn bộ file trong cùng một session.

## Phạm vi sửa

- Khớp SKU/code/email đúng mẫu generator và từng giá trị sai theo byte (`HEX`), tránh so sánh collation không phân biệt dấu.
- Khôi phục name/category/unit sản phẩm, tên/phòng ban nhân viên, tên khách hàng, nhà cung cấp phiếu DEMO-IN.
- Chỉ cập nhật trường khớp một biến thể lỗi đã biết. Trường đã tùy chỉnh khác mẫu không khớp thì giữ nguyên. Một bản ghi do người dùng tự tạo trùng hoàn toàn quy ước và giá trị demo vẫn có thể khớp, vì DB chưa có cờ nguồn dữ liệu seed.
- Không đổi ID, SKU, giá, tồn kho, dòng chứng từ hoặc khóa ngoại. Chạy lại sau sửa không thay đổi thêm.
- Không thể khôi phục dữ liệu tùy chỉnh đã mất ký tự bằng công cụ này. Không dùng `ALTER TABLE ... CONVERT` hoặc chuyển mã toàn DB như một cách sửa mojibake chung.

Đã kiểm tra bằng MySQL 8 trên schema thử nghiệm riêng: khôi phục tiếng Việt bị lỗi cp437, giữ nguyên tên tùy chỉnh/SKU ngoài demo, chạy lần hai không đổi thêm. Có 4 unit test, gồm xuất file dưới console cp437 nhưng file vẫn UTF-8 đúng. Chưa truy cập DB Windows của bạn; cần xem preview để xác nhận dữ liệu thực tế khớp.
