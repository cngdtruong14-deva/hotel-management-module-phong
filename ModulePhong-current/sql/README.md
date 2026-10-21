# SQL Server dùng chung

Database mặc định: `HotelManagementDB_LocalSimple`. Schema: `room-simple-1`. Các module kết nối cùng database.

## Database mới

Chạy lần lượt bằng SSMS:

1. `00_tao_database.sql`: tạo database nếu chưa có.
2. `01_bang_chinh.sql`: tầng, loại phòng, phòng, khách hàng, lượt lưu trú, đặt phòng, chuyển phòng.
3. `02_bang_chuc_nang.sql`: chi tiết, tiện nghi, yêu cầu, phản hồi, vệ sinh, bảo trì và dịch vụ.
4. `03_du_lieu_mau.sql`: dữ liệu thử.

`04_kiem_tra.sql` xem dữ liệu. `05_vi_du_dat_va_doi_phong.sql` là ví dụ có ROLLBACK cuối file.

## Database room-simple-1 đã tạo

Nếu chưa có index chống trùng giấy tờ khách, chạy riêng `06_chan_trung_giay_to.sql` sau khi kiểm tra tên database ở `USE`. Script dừng và liệt kê hồ sơ trùng; không tự sửa/xóa hồ sơ. Không chạy lại CREATE TABLE trên database đang có bảng.

## Quy ước tích hợp

Mã khách/phòng/phiếu dùng VARCHAR(20), tương ứng String trong Java. KhachHang gồm 13 cột và sinh mã qua SeqKhachHang. Giấy tờ duy nhất theo loại, nước cấp và số; khách chưa có giấy tờ vẫn được phép.

Phiếu: GIU_PHONG, DANG_O, DA_TRA, DA_HUY. Phòng đang ở: DANG_SU_DUNG. Java Service kiểm tra nghiệp vụ và transaction; khóa khách trước phòng, dùng cùng JDBC Connection. GiaPhongChot và DonGia giữ giá lịch sử. LuotLuuTru/ChuyenPhong hỗ trợ ghi lịch sử đổi phòng; thành viên triển khai nghiệp vụ bằng Java.

Dùng DBConnection trong `src/common` và mẫu `config/db.example.properties`. Đổi databaseName khi dùng tên database khác. Xem [ERD](ERD.md).
