# Hồ sơ phân chia module — 2026.10.11.1

Quản lý phòng: phòng/tầng/loại/giá, mô tả/tiện nghi, vệ sinh/bảo trì, gửi yêu cầu, xem review/trạng thái/phản ánh. Đặt phòng & Lưu trú: xử lý yêu cầu, phiếu/nhận/trả/hủy/đổi phòng và trả kết quả. Khách hàng: hồ sơ13. Dịch vụ: danh mục/tiêu dùng. UI phòng không nhận lại các nút tác nghiệp booking.

Ngày 11/10 người dùng chọn giữ SQL Server nhưng đơn giản hóa theo mẫu học tập. Schema room-simple-1 chỉ lưu bảng/PK/FK/DEFAULT; không procedures/triggers/CHECK nghiệp vụ. Java Service kiểm tra các ràng buộc nghiệp vụ trong transaction. Hồ sơ .3 dùng SQL08 được lưu archive và không còn là hướng dẫn hiện hành.

Đổi phòng vẫn có LuotLuuTru/ChuyenPhong và phiếu phân đoạn; Java phải tạo/cập nhật trong transaction, không có trigger tự điền. Source thành viên chưa nhận nên chưa có service đổi phòng Java hoàn chỉnh. Bộ SQL có ví dụ để triển khai; không tự xử lý nghiệp vụ. Khách13/mã String/giá chốt giữ để tương thích UI hiện tại. Database cũ không đổi.

2026.10.11.3: giữ nguyên source khách hàng; SQL khôi phục UNIQUE giấy tờ gốc qua file06. Không thay quyền sở hữu module.
