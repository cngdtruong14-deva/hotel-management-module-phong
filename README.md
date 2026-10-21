# Hotel Management — Module Phòng

Source Java nâng cao của nhóm: **2026.10.21.1**, schema SQL Server **room-simple-1**. Source chính duy nhất ở [ModulePhong-current](ModulePhong-current/).

## Bắt đầu

1. Cài JDK 17+ và NetBeans. Clone repository hoặc tải Code → Download ZIP.
2. Open Project tại `ModulePhong-current/`.
3. Xem thử giao diện bằng `run-shared-demo.cmd` (dữ liệu memory; không lưu khi đóng).
4. Để dùng SQL Server: chạy file `00` → `03` trong [sql](ModulePhong-current/sql/README.md) trên database mới. Database room-simple-1 đã tạo trước bản chống trùng giấy tờ: chạy riêng `06_chan_trung_giay_to.sql` sau khi kiểm tra `USE`.
5. Sao chép `config/db.example.properties` thành `config/db.properties`, tự cấu hình server/database/tài khoản local. File cấu hình cá nhân được Git bỏ qua.
6. Run/F6 mở ứng dụng dùng SQL. Xem [hướng dẫn NetBeans và JDBC](ModulePhong-current/NETBEANS_SQL_CHUNG.md).

PowerShell trong `ModulePhong-current`: `./build.ps1` để build; `./test.ps1` để kiểm thử. SQL Server JDBC driver và giấy phép đi kèm trong `lib/`.

## Ghép module

- [Hợp đồng API, model, panel và database](ModulePhong-current/CONTRACT.md)
- [Bàn giao Đặt phòng & Lưu trú](ModulePhong-current/BAN_GIAO_DAT_PHONG_LUU_TRU.md)
- [Phân chia trách nhiệm](docs/project/PHAN_CHIA_MODULE.md)
- [Nhật ký chỉnh sửa](docs/project/NHAT_KY_CHINH_SUA.md)
- [7 file SQL trên Google Docs](https://docs.google.com/document/d/1wivWsyp-QgpSCAFqNUgu7FbD8k6Ykh18gkqobbYiJE4)
- [ZIP source hiện hành](deliverables/ModulePhong-current.zip) · [SQL độc lập](handoff/HotelManagement-SQL-local.zip)

Quản lý phòng phụ trách danh mục, tiện nghi, gửi yêu cầu và xem review/trạng thái/phản ánh. Đặt phòng & Lưu trú phụ trách xác nhận/nhận/trả/hủy/đổi và gửi kết quả qua CongLuuTru. Consumer đầy đủ của thành viên chưa tích hợp. Module khách hàng sở hữu hồ sơ 13 trường; source khách hàng giữ nguyên trong lần xuất bản này.

Thành viên lấy `main` làm bản tham chiếu, mở nhánh riêng khi đóng góp và ghép thay đổi qua pull request. Các mã phòng/khách/phiếu dùng String theo CONTRACT, dùng chung cấu hình JDBC. Không commit mật khẩu hay db.properties.

Bản ZIP cũ và dữ liệu chẩn đoán giữ local, không đưa vào repository. Phiên bản và hash hiện hành nằm trong ModulePhong-current/VERSION.json và FILE_HASHES.json.
