# Kiểm chứng sửa SQL

## 2026.10.11.3 — Giữ nguyên module khách hàng

Khôi phục duy nhất index UX_KhachHang_GiayTo trên (LoaiGiayTo, QuocGiaCap, SoGiayTo), lọc SoGiayTo IS NOT NULL, đúng quy tắc SQL của module khách hàng gốc. Không đổi source Java, 13 cột, API, mã khách hay dữ liệu hiện có. Không thêm procedure/trigger/CHECK phức tạp.

Database mới chạy 00–03 như trước; file01 đã có index. Database room-simple-1 đã tạo: chạy riêng sql/06_chan_trung_giay_to.sql, kiểm tra tên USE trước. Script dừng và liệt kê nếu có giấy tờ trùng; không tự gộp/xóa/sửa khách. Có thể chạy lại an toàn. Không dùng file01 tạo bảng lại trên database có dữ liệu. Các kết nối ghi bảng cần SET ANSI_NULLS, QUOTED_IDENTIFIER, ANSI_PADDING, ANSI_WARNINGS, CONCAT_NULL_YIELDS_NULL, ARITHABORT ON và NUMERIC_ROUNDABORT OFF; DBConnection hiện có đã cấu hình.

Kiểm thử SQL Server/JDBC: lỗi trùng tái hiện trước sửa; sau sửa chặn INSERT/UPDATE trùng, cho phép sửa cùng hồ sơ, nhiều khách chưa có giấy tờ, cùng số hộ chiếu khác nước cấp; bật/tắt khách và hai INSERT đồng thời PASS. Migration chạy lại PASS; nâng cấp giữ nguyên mọi giá trị khách; dữ liệu trùng bị từ chối không tạo index. Chỉ chạy database kiểm thử riêng, chưa áp dụng database người dùng. Toàn bộ src/ giữ nguyên SHA256 so với đầu lần sửa. Google Docs cũ chưa cập nhật bản vá này.

PASS: SHA256 của toàn bộ 58 file src không đổi.
