# Hotel Management — Java Swing

Source ở [ModulePhong-current](ModulePhong-current/). Yêu cầu JDK 17+, NetBeans và SQL Server nếu dùng chế độ lưu dữ liệu.

## Chạy dự án

1. Clone repository hoặc chọn Code → Download ZIP.
2. Trong NetBeans, Open Project tại `ModulePhong-current/`.
3. Chạy `run-shared-demo.cmd` để xem giao diện với dữ liệu memory.
4. Để chạy SQL Server, làm theo [hướng dẫn SQL](ModulePhong-current/sql/README.md), sao chép `config/db.example.properties` thành `config/db.properties` và điền thông tin kết nối trên máy.
5. Run/F6 hoặc `run-shared-sqlserver.cmd` để dùng database.

Build bằng `./build.ps1`, kiểm thử bằng `./test.ps1` trong thư mục `ModulePhong-current`.

## Cấu trúc

- `src/module_phong`: quản lý phòng, tiện nghi, yêu cầu và xem phản hồi lưu trú.
- `src/module_khachhang`: hồ sơ khách hàng.
- `src/module_datphong`: khung đặt phòng và lưu trú để tích hợp.
- `src/module_dichvu`, `src/module_lienket`: dịch vụ và liên kết dữ liệu.
- `src/common`: cấu hình JDBC và context dùng chung.
- `sql`: schema SQL Server và dữ liệu mẫu.
- `test`: kiểm thử; `lib`: JDBC driver và giấy phép.

Các module dùng chung database và DBConnection. Giao diện ghép bằng JTabbedPane; CongLuuTru là cổng nhận kết quả lưu trú. Giao diện đặt phòng đầy đủ của thành viên cần tích hợp thêm. Không commit cấu hình cá nhân `db.properties`.

## NetBeans và giao diện Swing

Project hiển thị là **Hotel Management**; mở thư mục `ModulePhong-current` để giữ cấu trúc tích hợp. Điểm khởi chạy chung là `app.HotelSharedApp`; ứng dụng phòng riêng là `app.ModulePhongApp`; kiểm tra kết nối là `app.CheckDatabase`. Các lệnh cũ dùng `demo.*` cần đổi sang `app.*`.

Run/F6 chạy chế độ SQL với `config/db.properties`. Chạy memory bằng `run-shared-demo.cmd` hoặc menu **Run shared DEMO**. Memory không lưu dữ liệu sau khi đóng ứng dụng.

Giao diện Swing hiện viết trực tiếp bằng Java, không có file `.form` cho GUI Builder. Chỉnh giao diện trong các package `view`; nếu thành viên dùng kéo thả NetBeans thì bàn giao cả `.java` và `.form`, ghép JPanel qua JTabbedPane. Không sửa thủ công vùng code NetBeans tự sinh trong form của thành viên.
