# ModulePhong

Java 17+, Swing, SQL Server. Mở thư mục này bằng NetBeans.

- Demo memory: `run-shared-demo.cmd`.
- SQL: tạo database theo [sql/README.md](sql/README.md), sao chép `config/db.example.properties` sang `config/db.properties`, cấu hình server/database/tài khoản rồi chạy `run-shared-sqlserver.cmd` hoặc F6.
- Build PowerShell: `./build.ps1`.
- Test PowerShell: `./test.ps1`.

File cấu hình cá nhân không được đưa vào Git. JDBC driver và giấy phép ở `lib/`. Thông tin cấu trúc module ở [README chính](../README.md).

## NetBeans và giao diện Swing

Project hiển thị là **Hotel Management**; mở thư mục `ModulePhong-current` để giữ cấu trúc tích hợp. Điểm khởi chạy chung là `app.HotelSharedApp`; ứng dụng phòng riêng là `app.ModulePhongApp`; kiểm tra kết nối là `app.CheckDatabase`. Các lệnh cũ dùng `demo.*` cần đổi sang `app.*`.

Run/F6 chạy chế độ SQL với `config/db.properties`. Chạy memory bằng `run-shared-demo.cmd` hoặc menu **Run shared DEMO**. Memory không lưu dữ liệu sau khi đóng ứng dụng.

Giao diện Swing hiện viết trực tiếp bằng Java, không có file `.form` cho GUI Builder. Chỉnh giao diện trong các package `view`; nếu thành viên dùng kéo thả NetBeans thì bàn giao cả `.java` và `.form`, ghép JPanel qua JTabbedPane. Không sửa thủ công vùng code NetBeans tự sinh trong form của thành viên.

## Quy ước đọc và ghép mã

Mã Java thụt lề 4 dấu cách, tách từng bước xử lý và dùng ghi chú tiếng Việt không dấu. Chuỗi hiển thị và dữ liệu tiếng Việt giữ UTF-8. Phần khách hàng giữ source của thành viên.

Luồng xử lý: View → Controller → Service → DAO → SQL Server. Controller dùng SwingWorker khi tải/ghi dữ liệu. Các module dùng cùng DBConnection và giao tiếp lưu trú qua CongLuuTru; giữ đúng tên hàm, bảng, cột và mã String hiện có khi ghép. Bản chỉnh trình bày SQL không cần chạy lại database đã có schema room-simple-1 và index chống trùng giấy tờ.
