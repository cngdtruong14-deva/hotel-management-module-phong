# Kiểm chứng 2026.10.11.1 / room-simple-1

SQL mới trên database riêng HotelManagement_Portal_Test_20261011_Simple2: tạo 17 bảng, không user procedure/trigger/CHECK, seed 6 phòng/3 khách và DatPhong trống. File05 đặt/nhận/đổi/trả chạy rồi rollback PASS.

Build Java17 và test.ps1: SharedContextTest, SyncTest, PortalTest, ModulePhongTest, PortalViewTest PASS. Sau điều chỉnh test theo fixture mới, PortalTest memory và JDBC chạy lại PASS: nguồn khách/phòng/phiếu được service kiểm tra, FK đơn chặn mã không tồn tại, rollback và dữ liệu đọc lại đúng. SQL không còn kiểm tra bộ FK tổng hợp; không đánh đồng hai mức bảo đảm.

Log tại ../audit/2026-10-11 và bản sao trong verification. Hai lần JDBC đầu thất bại do assertion đếm toàn bộ tiện nghi/đọc mô tả dòng đầu trong fixture có thêm phòng; đã sửa lọc P103 và chạy database sạch đạt. Các log cũ giữ trong archive, không phải bằng chứng của schema mới.

Không thay database chung; không có migration đổi schema cũ. Chưa kiểm thử tải đồng thời hoặc Java service đổi phòng của thành viên chưa gửi. File05 là ví dụ SQL, không thay kiểm tra nghiệp vụ đầy đủ.

## 2026.10.11.3 — Giữ nguyên module khách hàng

Khôi phục duy nhất index UX_KhachHang_GiayTo trên (LoaiGiayTo, QuocGiaCap, SoGiayTo), lọc SoGiayTo IS NOT NULL, đúng quy tắc SQL của module khách hàng gốc. Không đổi source Java, 13 cột, API, mã khách hay dữ liệu hiện có. Không thêm procedure/trigger/CHECK phức tạp.

Database mới chạy 00–03 như trước; file01 đã có index. Database room-simple-1 đã tạo: chạy riêng sql/06_chan_trung_giay_to.sql, kiểm tra tên USE trước. Script dừng và liệt kê nếu có giấy tờ trùng; không tự gộp/xóa/sửa khách. Có thể chạy lại an toàn. Không dùng file01 tạo bảng lại trên database có dữ liệu. Các kết nối ghi bảng cần SET ANSI_NULLS, QUOTED_IDENTIFIER, ANSI_PADDING, ANSI_WARNINGS, CONCAT_NULL_YIELDS_NULL, ARITHABORT ON và NUMERIC_ROUNDABORT OFF; DBConnection hiện có đã cấu hình.

Kiểm thử SQL Server/JDBC: lỗi trùng tái hiện trước sửa; sau sửa chặn INSERT/UPDATE trùng, cho phép sửa cùng hồ sơ, nhiều khách chưa có giấy tờ, cùng số hộ chiếu khác nước cấp; bật/tắt khách và hai INSERT đồng thời PASS. Migration chạy lại PASS; nâng cấp giữ nguyên mọi giá trị khách; dữ liệu trùng bị từ chối không tạo index. Chỉ chạy database kiểm thử riêng, chưa áp dụng database người dùng. Toàn bộ src/ giữ nguyên SHA256 so với đầu lần sửa. Google Docs đã được đồng bộ 7 file SQL ngày 11/10/2026.
