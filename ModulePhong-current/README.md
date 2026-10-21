# ModulePhong — 2026.10.21.1 / room-simple-1

Source chính cho các lần ghép module tiếp theo. Java17+, Swing/MVC, SQL Server. Người dùng đã chọn đơn giản hóa SQL: bỏ procedures/triggers/CHECK nghiệp vụ; xử lý bằng Java Service.

## SQL local

Bộ độc lập ở ../handoff/HotelManagement-SQL-local.zip; các SQL giống hệt source trong sql/. Chạy 00_tao_database.sql → 01_bang_chinh.sql → 02_bang_chuc_nang.sql → 03_du_lieu_mau.sql trên database mới HotelManagementDB_LocalSimple. 04 SELECT; 05 ví dụ tùy chọn có rollback. Đọc sql/README.md.

Cấu hình config/db.properties từ file example, đổi databaseName thành HotelManagementDB_LocalSimple rồi chạy run-sqlserver.cmd. Không có migration tự động từ schema cũ; database cũ không bị sửa/xóa. Bộ SQL phức tạp lưu archive/2026-10-11-sql-complex, không trộn với bản này.

## Chạy Java

build.ps1 rồi run-demo.cmd. run-shared-demo.cmd / run-shared-sqlserver.cmd mở thêm panel khung Đặt phòng & Lưu trú. Các panel vẫn ghép bằng JTabbedPane. Quản lý phòng sở hữu danh mục, tiện nghi, yêu cầu, đọc review/phản ánh; UI thành viên xử lý booking/nhận/trả/hủy/đổi.

Khung hiện tại giữ khách13 và model/phần đọc ghi phòng/booking/feedback. Schema đã có lịch sử đổi phòng, nhưng service Java đổi phòng của thành viên còn cần triển khai theo sql/README.md; không còn thủ tục SQL làm thay. Database mẫu chưa có phiếu để bắt đầu viết giao diện.

Đọc CONTRACT.md, BAN_GIAO_DAT_PHONG_LUU_TRU.md, REQUIREMENTS_AUDIT.md, docs/project và verification/RESULTS.md. test.ps1 kiểm thử memory/build/render; JDBC được chạy trên database riêng. Source/ZIP cũ chỉ ở archive; bản giao hiện hành ở ../deliverables/ModulePhong-current.zip. release.ps1 và -VerifyOnly tạo/xác minh hash.


## NetBeans và database chung

Đã có build.xml/nbproject để Open Project trực tiếp. Run/F6 chạy demo.HotelSharedApp với SQL; cấu hình config/db.properties trước. Target check-db chỉ đọc để kiểm tra kết nối. Hướng dẫn và báo cáo rà soát: NETBEANS_SQL_CHUNG.md. SQL vẫn room-simple-1; bản vá 2026.10.11.3 có thêm file06, đã cập nhật Google Docs ngày 11/10/2026.

## 2026.10.11.3 — Giữ nguyên module khách hàng

Khôi phục duy nhất index UX_KhachHang_GiayTo trên (LoaiGiayTo, QuocGiaCap, SoGiayTo), lọc SoGiayTo IS NOT NULL, đúng quy tắc SQL của module khách hàng gốc. Không đổi source Java, 13 cột, API, mã khách hay dữ liệu hiện có. Không thêm procedure/trigger/CHECK phức tạp.

Database mới chạy 00–03 như trước; file01 đã có index. Database room-simple-1 đã tạo: chạy riêng sql/06_chan_trung_giay_to.sql, kiểm tra tên USE trước. Script dừng và liệt kê nếu có giấy tờ trùng; không tự gộp/xóa/sửa khách. Có thể chạy lại an toàn. Không dùng file01 tạo bảng lại trên database có dữ liệu. Các kết nối ghi bảng cần SET ANSI_NULLS, QUOTED_IDENTIFIER, ANSI_PADDING, ANSI_WARNINGS, CONCAT_NULL_YIELDS_NULL, ARITHABORT ON và NUMERIC_ROUNDABORT OFF; DBConnection hiện có đã cấu hình.

Kiểm thử SQL Server/JDBC: lỗi trùng tái hiện trước sửa; sau sửa chặn INSERT/UPDATE trùng, cho phép sửa cùng hồ sơ, nhiều khách chưa có giấy tờ, cùng số hộ chiếu khác nước cấp; bật/tắt khách và hai INSERT đồng thời PASS. Migration chạy lại PASS; nâng cấp giữ nguyên mọi giá trị khách; dữ liệu trùng bị từ chối không tạo index. Chỉ chạy database kiểm thử riêng, chưa áp dụng database người dùng. Toàn bộ src/ giữ nguyên SHA256 so với đầu lần sửa. Google Docs đã được đọc lại xác nhận đủ 7 file SQL ngày 11/10/2026.

## Xuất bản GitHub — 2026.10.21.1

Hướng dẫn clone/chạy/ghép module ở README root repository. Không thay source Java hay SQL khi xuất bản. File cấu hình local, log kiểm thử và archive/work/audit không được theo dõi bằng Git. Schema vẫn room-simple-1; Google Docs đồng bộ nội dung SQL 2026.10.11.3.
