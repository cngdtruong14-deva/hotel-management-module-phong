# Bản dùng cho các lần ghép tiếp theo

**Source chính: ModulePhong-current/** — phiên bản đọc tại ModulePhong-current/VERSION.json.

**ZIP bàn giao: deliverables/ModulePhong-current.zip**.

Hợp đồng bàn giao: ModulePhong-current/CONTRACT.md. Kết quả kiểm thử: ModulePhong-current/verification/RESULTS.md. Thay đổi/lỗi đã sửa: ModulePhong-current/CHANGELOG.md.

archive chứa bản đầu vào và lịch sử của luồng chat này, không dùng làm nền tích hợp. Dự án hotel.* ở thư mục gốc đã được xóa theo yêu cầu ngày 2026-10-05; nhật ký nằm trong audit/deleted-other-chat-files.json. Bản ModulePhong ở đường dẫn Documents/Codex trước đây đã bị thay thế về mặt bàn giao bởi bản chính này; không tiếp tục sửa từ ZIP cũ.

Mỗi lần nhận module: đọc AGENTS.md và phiên bản chính, đối chiếu rồi sửa source chính, kiểm thử kết hợp, tăng version, xuất ZIP hiện hành. Không chọn v1/v2 theo tên file của thành viên.


Từ 2026.10.09.1: quản lý phòng tách khỏi tác nghiệp booking; hồ sơ phân chia và nhật ký ở docs/project, có bản sao trong ZIP. Cổng lưu trú đã kiểm thử, source thành viên thật chưa được nhận.


Hiện hành 2026.10.21.1: SQL Server room-simple-1, bộ CREATE TABLE đơn giản ở handoff/HotelManagement-SQL-local.zip. Bản SQL phức tạp đã chuyển archive/2026-10-11-sql-complex. Dùng database mới HotelManagementDB_LocalSimple; nghiệp vụ do Java Service kiểm tra.
