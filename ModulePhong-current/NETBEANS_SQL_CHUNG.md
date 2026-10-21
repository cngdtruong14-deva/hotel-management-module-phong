# Chạy các module cùng NetBeans và SQL Server — 2026.10.11.2

## Kết luận rà soát

Source hiện hành có thể dùng một database SQL Server chung. Sáu file SQL trong source và gói SQL độc lập giống từng byte. Hai module cùng dùng Phong/MaPhong, KhachHang/MaKhachHang, DatPhong/MaDatPhong, kiểu mã VARCHAR(20), khách13 và trạng thái thống nhất. Không cần tạo hai database hoặc hai bảng Phong/KhachHang.

Đây là kết luận cho module phòng + backend/bộ khung đặt phòng hiện có trong source. Chưa nhận source hoàn chỉnh của thành viên để xác nhận giao diện/nghiệp vụ bên đó khớp. Nếu thành viên vẫn dùng mẫu MySQL INT AUTO_INCREMENT, CCCD/SDT và DA_CHECK_IN thì chưa tương thích: cần đổi theo CONTRACT.md.

## SQL có cần khác theo module không?

Database là dữ liệu lưu trên SQL Server; file .sql là lệnh tạo bảng/dữ liệu, không phải database đang chạy. Chỉ chạy bộ tạo bảng một lần cho database mới. File01 chứa bảng chính dùng chung; file02 chứa bảng chức năng; file03 dữ liệu mẫu; file04 SELECT; file05 ví dụ rollback. Các module khác nhau ở service/DAO/SELECT/INSERT/UPDATE và panel, không sửa khác tên/kiểu của cùng bảng.

| Dữ liệu | Chủ sở hữu/chức năng | Bên còn lại sử dụng |
|---|---|---|
| Tang, LoaiPhong, Phong, ChiTietPhong, PhongTienNghi | Quản lý phòng | Lưu trú đọc phòng/giá, đổi trạng thái qua nghiệp vụ |
| KhachHang | Khách hàng | Cả hai đọc mã khách; không tạo bảng khách riêng |
| DatPhong, LuotLuuTru, ChuyenPhong | Đặt phòng & Lưu trú | Phòng đọc tình trạng và lịch sử liên quan |
| YeuCauDatPhong | Phòng gửi, Lưu trú xử lý | Dùng cùng dòng yêu cầu và mã |
| PhongLuuTruNhan, PhongDanhGiaNhan, PhongPhanAnhNhan | Nguồn lưu trú/feedback gửi về | UI phòng hiển thị |
| VeSinh, BaoTri | Quản lý phòng | Lưu trú kiểm tra phòng khả dụng |

Nếu mỗi máy dùng localhost thì mỗi máy có database riêng; chỉ chung cấu trúc, không chung dữ liệu. Muốn chạy nhóm cùng dữ liệu: mọi db.url phải trỏ cùng server:port và databaseName. Không chia sẻ một file .mdf qua thư mục mạng cho từng Java module tự mở.

## Mở project trong NetBeans

1. Giải nén source current, giữ nguyên src/, lib/, config/, sql/, build.xml và nbproject/project.xml.
2. File → Open Project, chọn thư mục ModulePhong-current (không chọn src). Project là Java with Ant dạng freeform; NetBeans phải bật hỗ trợ Java/Ant.
3. Dùng JDK17 trở lên; source biên dịch --release17. Nếu javac không tìm thấy, chọn JDK cho IDE hoặc sửa JAVA_HOME máy, không đổi mã nguồn.
4. Build Project tạo ModulePhong.jar. Run Project/F6 gọi demo.HotelSharedApp ở chế độ SQL, mở ba tab Phòng, Khách hàng, Đặt phòng & Lưu trú (bộ khung).
5. Muốn xem demo không cần SQL: menu project “Run shared DEMO”. Có thể dùng Files → build.xml → Run Target → run-demo nếu menu không hiện.
6. Kiểm tra SQL trước khi mở UI: “Check database connection” hoặc target check-db. Chỉ SELECT, không thêm/sửa dữ liệu, không in mật khẩu.

Không cần Maven hoặc tải JDBC mới: lib/ đã có mssql-jdbc-13.2.1.jre11.jar. Freeform cấu hình build/run/test; chưa cấu hình Debug/F5 hoặc chạy riêng từng file. Các panel viết tay vẫn sửa Java trong NetBeans; không tự sinh file .form từ code có sẵn.

## Cấu hình SQL một lần cho cả ứng dụng

Chạy sql/00..03 trong SSMS trên database mới HotelManagementDB_LocalSimple. Đã có database đúng schema thì không chạy lại. Sao chép config/db.example.properties thành config/db.properties; không ghi đè file cấu hình riêng đang có.

SQL authentication (SQL Server phải bật login tương ứng):

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=HotelManagementDB_LocalSimple;encrypt=true;trustServerCertificate=true
db.user=TEN_DANG_NHAP_SQL
db.password=MAT_KHAU_CUA_BAN
```

1433 chỉ là ví dụ, phải thay bằng cổng instance của máy. trustServerCertificate=true phù hợp máy local học tập; server nhóm có chứng chỉ hợp lệ có thể dùng false như example mặc định.

Windows authentication (Windows/JDK64 bit, DLL trong lib):

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=HotelManagementDB_LocalSimple;encrypt=true;trustServerCertificate=true;integratedSecurity=true
db.user=
db.password=
```

Ant đã truyền java.library.path=lib cho run/check-db. Các biến môi trường HOTEL_DB_URL/HOTEL_DB_USER/HOTEL_DB_PASSWORD ưu tiên file: nếu đổi file mà vẫn trỏ DB khác, kiểm tra những biến đó. Giữ config UTF-8 không BOM. Project không tự tạo user SQL hoặc sửa cấu hình network server.

Kết nối trong NetBeans Services → Databases chỉ để xem/chạy SQL từ IDE. Cấu hình đó không tự chuyển vào Java; ứng dụng vẫn dùng config/db.properties qua common.DBConnection.

## Kiến trúc ghép chung

```text
HotelSharedApp (JFrame + JTabbedPane)
    └── HotelContext.sql(config)
          ├── PhongService + HoSoPhongService → PhongView
          ├── KhachHangService → KhachHangPanel
          ├── DatPhongService → DatPhongPanel/Controller
          └── CongLuuTru → queue và phản hồi
                 tất cả dùng cùng DBConnection/ConnectionFactory
```

Khởi tạo HotelContext ngoài EDT vì có kiểm tra database; tạo panel trên EDT. Truyền cùng context vào module; không new DBConnection riêng với URL khác, không new MemoryDAO khi đang chạy SQL. ConnectionFactory dùng chung nhưng mỗi transaction mở Connection riêng; phiếu và trạng thái phòng phải dùng chung Connection bên trong transaction đó. Đóng Connection sau giao dịch, không chia sẻ một Connection tĩnh cho mọi SwingWorker.

Constructor ghép:

```java
HotelContext ctx = HotelContext.sql(Path.of("config/db.properties"));
// Trên EDT:
DatPhongPanel booking = new DatPhongPanel(new DatPhongController(ctx));
tabs.addTab("Đặt phòng & Lưu trú", booking);
```

Service booking hiện có tạo/nhận/trả/hủy nhưng không tự gửi callback. Sau commit, adapter của nhóm gọi CongLuuTru. Nếu callback lỗi, giữ mã phiếu/payload để thử lại, không tạo thêm booking. DatPhongPanel hiện đọc queue/phiếu; chưa có form xử lý booking hoàn chỉnh. Đổi phòng cần Java Service transaction theo file SQL05; chưa tích hợp từ thành viên. Những giới hạn này không phải do cần database riêng.

## Kiểm chứng đã làm

- project.xml được kiểm tra bằng XSD của NetBeans21 đã cài.
- Ant của NetBeans21 build Java17 và test memory PASS.
- Target check-db đọc schema room-simple-1 qua JDBC trên database test riêng PASS.
- SharedContextTest SQL: khách → yêu cầu → booking → nhận phòng/callback → trả phòng → context mới đọc lại dữ liệu PASS.
- SQL gói độc lập và source giống nhau; không sửa schema hay database người dùng trong lần rà soát.

Chưa thao tác mở UI NetBeans bằng chuột, chưa kiểm thử source thành viên chưa gửi. Chứng cứ ở verification/linked-ant.log, netbeans-project-validation.log và audit/2026-10-11. Không coi bộ khung là toàn bộ module Lưu trú hoàn chỉnh.
