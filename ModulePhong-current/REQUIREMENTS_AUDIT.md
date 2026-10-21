# Đối chiếu yêu cầu — 2026.10.11.1

Quyết định mới: giữ SQL Server, viết CREATE TABLE đơn giản theo mẫu bài học; bỏ quy tắc nghiệp vụ khỏi tầng database, đưa về Java. Quyết định này thay thế yêu cầu thực thi bằng stored procedures ở bản .3.

- Swing/MVC/JTabbedPane, source ModulePhong-current: giữ.
- Khách13, mã VARCHAR(20), giá chốt phiếu/dịch vụ: giữ để tương thích.
- Thông tin/thiết bị/review/yêu cầu/khách và phản ánh/vệ sinh/bảo trì: có bảng và service hiện tại.
- Đặt/nhận/trả/hủy: backend Java hiện có; UI Lưu trú thành viên đang là bộ khung.
- Đổi phòng: có LuotLuuTru/ChuyenPhong và ví dụ transaction; chưa có Java service thành viên hoàn chỉnh. Không còn trigger/procedure tự xử lý.
- Đã bỏ CHECK/filtered index/FK tổng hợp; PK/FK đơn giữ mã tồn tại. Kiểm tra trạng thái, đúng nguồn, tránh đặt trùng phải ở Java transaction; dữ liệu ghi SQL trực tiếp không được bảo vệ đầy đủ các quy tắc này.
- Local mới 00–03 tạo bảng và dữ liệu nền, DatPhong trống. 04 kiểm tra; 05 demo rollback.
- Không sửa/xóa database cũ. Bản SQL phức tạp lưu archive/2026-10-11-sql-complex. Bản current dùng room-simple-1.

Bằng chứng thực thi/build/JDBC ở verification/RESULTS.md. Chưa có đặt theo ngày dự kiến hoặc danh sách nhiều người/phiếu.


2026.10.11.2: Đã rà soát source/schema và cấu hình NetBeans Ant; kiểm thử context SQL chung đạt. Hai module dùng cùng database, khác nghiệp vụ/bảng sở hữu; không cần schema Phong/KhachHang riêng. Báo cáo NETBEANS_SQL_CHUNG.md ghi rõ phần đã nối và UI/service thành viên còn thiếu.
