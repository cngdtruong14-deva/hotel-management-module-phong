# SQL Server đơn giản — 2026.10.11.1

Bản mới theo yêu cầu ngày 11/10: dễ đọc, mỗi bảng một CREATE TABLE, chỉ PK/FK, NOT NULL và DEFAULT cơ bản. Không stored procedure, trigger, CHECK, view hoặc index lọc. Nghiệp vụ viết trong Java Service. Đây là bản hiện hành thay bộ SQL phức tạp room-portal-3; không trộn hai bộ.

## 1. Tạo database

Mở SSMS, kết nối SQL Server local rồi chạy các file theo thứ tự:

| File | Nội dung |
|---|---|
| 00_tao_database.sql | Tạo HotelManagementDB_LocalSimple |
| 01_bang_chinh.sql | Tang, LoaiPhong, Phong, KhachHang, LuotLuuTru, DatPhong, ChuyenPhong |
| 02_bang_chuc_nang.sql | Tiện nghi, yêu cầu, review, phản ánh, vệ sinh/bảo trì và dịch vụ |
| 03_du_lieu_mau.sql | Loại phòng, 6 phòng, 3 khách, tiện nghi và dịch vụ mẫu; chưa tạo booking |
| 04_kiem_tra.sql | SELECT cho từng màn hình |
| 05_vi_du_dat_va_doi_phong.sql | Ví dụ tùy chọn về đặt/nhận/đổi/trả phòng, rollback cuối file |

Chạy 00–03 một lần trên database mới; 04 đọc lại tùy ý. 05 dùng dữ liệu mẫu và không lưu thay đổi sau khi chạy hết file. Dừng nếu SQL báo lỗi. Database cũ được giữ nguyên; đây là bộ tạo mới, không phải migration. Trong Java đặt databaseName=HotelManagementDB_LocalSimple ở config/db.properties, cùng server/tài khoản phù hợp máy của bạn.

## 2. Bắt đầu từ các bảng chính

```text
LoaiPhong ──< Phong >── Tang
                │
                └──< DatPhong >── KhachHang
                         │
                    MaLuotLuuTru
                         │
                    LuotLuuTru
```

Bên đặt phòng có thể đọc phần LoaiPhong, Phong, KhachHang, DatPhong trước. Tang giữ vì UI đã có quản lý tầng. LuotLuuTru/ChuyenPhong dành cho chức năng đổi phòng. File02 giữ các chức năng khác đã thống nhất, không phải tất cả đều cần dùng ngay khi viết form đặt phòng.

## 3. Những khác biệt cần giữ so với mẫu MySQL

- Dùng SQL Server: NVARCHAR cho tiếng Việt, DATETIME2/SYSDATETIME cho thời gian, GO giữa batch.
- Mã vẫn VARCHAR(20): P101, STD, KH00000001, DP001... Giao diện/model Java đã dùng String; chuyển sang INT tự tăng sẽ không khớp.
- KhachHang vẫn 13 thuộc tính; SoDienThoai và LoaiGiayTo/SoGiayTo/QuocGiaCap thay cho SDT/CCCD đơn lẻ. Giữ sequence SeqKhachHang vì DAO khách đang dùng để sinh mã.
- Phong có MaTang thay vì Tang INT; GiaPhong nullable để tùy giá từng phòng. Nếu NULL thì lấy LoaiPhong.GiaTheoNgay.
- DatPhong có GiaPhongChot: chép giá hiệu lực lúc tạo phiếu. Nếu chỉ đọc giá danh mục thì tăng giá ngày mai sẽ làm phiếu cũ đổi giá.
- NgayNhan/NgayTra là thời điểm thực tế, để NULL khi chưa nhận/trả; mẫu dùng ngày dự kiến là nghiệp vụ khác chưa triển khai.
- Giữ PhienBan trong các bảng nhận để service hiện có xử lý phản hồi cũ. ThanhTien là cột tính đơn giản SoLuong * DonGia để DAO dịch vụ tiếp tục đọc được.

## 4. Trạng thái thống nhất trong Java

| Đối tượng | Giá trị |
|---|---|
| Phòng | TRONG, DA_DAT, DANG_SU_DUNG, DANG_DON, BAO_TRI |
| Phiếu | GIU_PHONG, DANG_O, DA_TRA, DA_HUY |
| Khách | HOAT_DONG, NGUNG_HOAT_DONG |
| Yêu cầu | CHO_XU_LY, TIEP_NHAN, TU_CHOI, HOAN_TAT |
| Phản ánh | MOI, DANG_XU_LY, DA_XU_LY |
| Tiện nghi | TOT, CAN_SUA, NGUNG_DUNG |

SQL mới không CHECK những chuỗi này; Java phải kiểm tra và dùng đúng giá trị. Đặc biệt phòng dùng DANG_SU_DUNG, phiếu dùng DANG_O.

## 5. Phân việc giữa SQL và Java

SQL lưu dữ liệu và giữ các mã tham chiếu tồn tại. Java Service kiểm tra giá/số lượng/điểm đánh giá hợp lệ, giấy tờ khách, khách hoạt động, phòng trống, một lượt hoạt động/phòng, đúng khách/phòng/phiếu, thứ tự nhận/trả và nội dung phản hồi. FK đơn không chứng minh ba mã khách/phòng/phiếu thuộc cùng một lượt; service phải đối chiếu.

Gửi yêu cầu chỉ INSERT YeuCauDatPhong. Sau khi bộ phận lưu trú tạo phiếu, cập nhật kết quả yêu cầu và dữ liệu nhận. Nhận/trả/hủy/đổi phòng cập nhật phiếu và trạng thái phòng trong cùng JDBC transaction. Cần khóa/kiểm tra lại trạng thái bên trong transaction để tránh hai người cùng nhận một phòng; không chỉ kiểm tra trên UI.

Đổi phòng: tạo nhóm LuotLuuTru nếu phiếu cũ chưa có, điền MaLuotLuuTru vào phiếu cũ; đóng phiếu cũ, tạo phiếu mới cùng nhóm và ghi ChuyenPhong. Review/phản ánh/dịch vụ giữ theo phiếu của phòng phát sinh. Không có trigger tự tạo nhóm nữa. MaLuotLuuTru có thể NULL với nghiệp vụ đặt/nhận/trả cũ; Java thành viên phải điền khi triển khai đổi phòng. File05 minh họa SQL, không thay service hoàn chỉnh.

UI phòng vẫn đọc review/phản ánh và gửi yêu cầu; nút đặt/nhận/trả/hủy/đổi thuộc UI Lưu trú của thành viên. Bộ SQL vẫn có bảng cho toàn bộ chức năng đã thống nhất, nhưng không tự thực thi nghiệp vụ. Phần đổi phòng trong Java thành viên chưa tích hợp; không còn gọi sp_DoiPhong hay procedures cũ.

## 6. Mẫu bảng DatPhong

```sql
CREATE TABLE DatPhong (
    MaDatPhong VARCHAR(20) PRIMARY KEY,
    MaKhachHang VARCHAR(20) NOT NULL REFERENCES KhachHang(MaKhachHang),
    MaPhong VARCHAR(20) NOT NULL REFERENCES Phong(MaPhong),
    GiaPhongChot DECIMAL(18,2) NOT NULL,
    NgayDat DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    NgayNhan DATETIME2(0) NULL,
    NgayTra DATETIME2(0) NULL,
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'GIU_PHONG',
    MaLuotLuuTru VARCHAR(20) NULL REFERENCES LuotLuuTru(MaLuotLuuTru)
);
```

Đã có câu lệnh này trong file01, không chạy thêm lần nữa. Không dùng đồng thời mã/trạng thái của mẫu MySQL và bộ hiện hành.
