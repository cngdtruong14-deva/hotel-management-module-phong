# Nhật ký chỉnh sửa dự án

Source chính: ModulePhong-current. Nhật ký ghi rõ quyết định, sửa đổi đã thực hiện và phần chưa triển khai; không coi đề xuất là tính năng đã có.

## 2026-10-05 — bản 2026.10.05.1 (theo CHANGELOG hiện có)
- Ghép module khách hàng v2 với ModulePhong; dùng chung model 13 trường, DAO/schema và bộ common.
- Kiểm thử memory/SQL và migration; tạo cơ chế archive/version/hash.

## 2026-10-05 — bản 2026.10.05.2 (theo CHANGELOG hiện có)
- Xóa dự án hotel.* của chat khác theo yêu cầu; lưu inventory hash.
- Rà soát ràng buộc, kiểm thử lại và xuất ZIP hiện hành.

## 2026-10-09 — giai đoạn đề xuất (lịch sử, đã được triển khai bên dưới) — yêu cầu tách quản lý phòng khỏi đặt phòng/lưu trú
- Người dùng xác định lại phạm vi: thông tin phòng, review, gửi yêu cầu và nhận trạng thái khách/phản ánh theo phòng.
- Đã đọc VERSION/README/CONTRACT bản hiện hành và ghi thiết kế trong PHAN_CHIA_MODULE.md.
- Đã tạo hồ sơ phân chia module và file nhật ký này.
- Chưa sửa source, chưa migration SQL, chưa đổi phiên bản/ZIP; thiết kế chờ duyệt. Chưa có source module lưu trú chính thức nên adapter thật chưa thể kiểm chứng.
- Việc giữ/check-in/check-out trong UI ModulePhong cũ sẽ được bỏ khỏi UI khi triển khai thiết kế đã duyệt; dữ liệu lịch sử được bảo toàn.


## 2026-10-09 — đã duyệt và triển khai 2026.10.09.1

- Người dùng duyệt thiết kế, yêu cầu sửa source và tiếp tục. Triển khai đúng source ModulePhong-current.
- Gỡ LienKetView/Controller và test giao diện cũ khỏi source; lưu archive/2026-10-09-ui-booking-retired. Giữ backend tương thích và dữ liệu phiếu/dịch vụ.
- Thêm mô tả/tiện nghi, review chỉ đọc, hàng đợi yêu cầu và bảng lưu trú/phản ánh; DAO memory+SQL, cổng CongLuuTru và callback kiểm tra nguồn/version.
- Thêm SQL07 sáu bảng, FK tổng hợp phiếu/phòng/khách; schema startup yêu cầu room-portal-2. Không áp migration vào database chung.
- Sửa lỗi chặn mã khách/phiếu chữ thường ở request/callback sau review, thêm kiểm thử hồi quy fail → pass.
- Test FK ban đầu bị UNIQUE review che lấp; đổi phép kiểm tra sang cập nhật sai khóa trong transaction rollback. Render đợi worker xong để không chụp trạng thái đang tải.
- SyncTest, PortalTest, ModulePhongTest, PortalViewTest PASS. SQL Fresh2/Upgrade PASS, migration giữ khách/phiếu/giá dịch vụ lịch sử 40000. Log gốc ở audit/2026-10-09, log thành công đóng gói trong verification.
- Cập nhật README/CONTRACT/ERD/REQUIREMENTS_AUDIT/RESULTS/VERSION, đóng gói hồ sơ này; phát hành một ZIP current, archive bản trước, xác minh manifest và từng entry ZIP.
- Chưa có source thành viên Đặt phòng & Lưu trú: API/hàng đợi sẵn sàng, consumer thật chưa tích hợp. Fixture kiểm thử không thay thế kiểm chứng adapter thật.

## 2026-10-09 — 2026.10.09.2: khung chung bàn giao Lưu trú

Người dùng yêu cầu cùng database/bộ khung. Bổ sung HotelContext, main ghép ba module, panel/controller Lưu trú đọc queue/phiếu và hướng dẫn triển khai. Schema không đổi; nghiệp vụ UI/consumer hoàn chỉnh do thành viên bổ sung. Build và kiểm thử khung được ghi tại verification. Chưa thay database chung.


## 2026-10-09 — 2026.10.09.3: hoàn chỉnh schema theo ảnh

Theo yêu cầu người dùng, bổ sung SQL08: nhóm lượt, lịch sử đổi phòng và chín thủ tục nghiệp vụ nguyên tử. Chọn phiếu theo phân đoạn để không làm mất/đổi phòng của feedback, giữ tương thích mã/model khách/phòng. Bộ SQL độc lập nâng cấp bằng file06, có ví dụ07 rollback. Nâng cấp/cài mới/đổi phòng lặp/rollback lỗi muộn/Java/JDBC đều PASS. Log tại audit/2026-10-09 và verification. SQL chung chưa áp migration; UI thành viên chưa tích hợp. Các hạn chế đổi phòng ghi ở mục lịch sử trước đã được thay thế bằng thiết kế này.


## 2026-10-11 — 2026.10.11.1: đơn giản hóa SQL theo mẫu học tập

Người dùng thấy schema quá phức tạp và xác nhận giữ SQL Server. Archive bộ room-portal-3, thay bằng room-simple-1: bảng/PK/FK đơn/default, không procedure/trigger/CHECK/filter index. Giữ thuộc tính Java cần, chưa tạo booking trong seed. Sửa startup/test cho hợp đồng mới. Kiểm thử cài mới/demo rollback/build/memory/render/JDBC PASS. Hai lần JDBC đầu assertion phụ thuộc số lượng/thứ tự fixture cũ; sửa chọn đúng P103 rồi chạy database sạch Simple2 đạt. Không sửa database hiện hữu. Nghiệp vụ đổi phòng chuyển về trách nhiệm Java của thành viên; schema và ví dụ sẵn, không báo đã có UI/service hoàn chỉnh.


## 2026-10-11 — 2026.10.11.2: rà soát SQL chung và NetBeans

Đối chiếu schema/source hiện hành, không có source thành viên mới. SQL source/handoff giống byte; chung DBConnection qua HotelContext. Thêm NetBeans Ant project, kiểm tra kết nối chỉ đọc và test context SQL có đọc lại. Kiểm chứng XSD NetBeans21, Ant và JDBC PASS; chưa mở GUI IDE bằng chuột. Không sửa schema hoặc DB chung. UI booking/đổi phòng hoàn chỉnh vẫn chờ thành viên.

## 2026.10.11.3 — Giữ nguyên module khách hàng

Khôi phục duy nhất index UX_KhachHang_GiayTo trên (LoaiGiayTo, QuocGiaCap, SoGiayTo), lọc SoGiayTo IS NOT NULL, đúng quy tắc SQL của module khách hàng gốc. Không đổi source Java, 13 cột, API, mã khách hay dữ liệu hiện có. Không thêm procedure/trigger/CHECK phức tạp.

Database mới chạy 00–03 như trước; file01 đã có index. Database room-simple-1 đã tạo: chạy riêng sql/06_chan_trung_giay_to.sql, kiểm tra tên USE trước. Script dừng và liệt kê nếu có giấy tờ trùng; không tự gộp/xóa/sửa khách. Có thể chạy lại an toàn. Không dùng file01 tạo bảng lại trên database có dữ liệu. Các kết nối ghi bảng cần SET ANSI_NULLS, QUOTED_IDENTIFIER, ANSI_PADDING, ANSI_WARNINGS, CONCAT_NULL_YIELDS_NULL, ARITHABORT ON và NUMERIC_ROUNDABORT OFF; DBConnection hiện có đã cấu hình.

Kiểm thử SQL Server/JDBC: lỗi trùng tái hiện trước sửa; sau sửa chặn INSERT/UPDATE trùng, cho phép sửa cùng hồ sơ, nhiều khách chưa có giấy tờ, cùng số hộ chiếu khác nước cấp; bật/tắt khách và hai INSERT đồng thời PASS. Migration chạy lại PASS; nâng cấp giữ nguyên mọi giá trị khách; dữ liệu trùng bị từ chối không tạo index. Chỉ chạy database kiểm thử riêng, chưa áp dụng database người dùng. Toàn bộ src/ giữ nguyên SHA256 so với đầu lần sửa. Google Docs cũ chưa cập nhật bản vá này.

## 2026.10.21.1 — Bàn giao GitHub

Chuẩn bị repository chia sẻ source current, 7 file SQL và hướng dẫn NetBeans/JDBC. Cập nhật README root, lọc cấu hình local/log/lịch sử khỏi Git; giữ nguyên Java/SQL và source khách hàng. Google Docs đã đồng bộ đủ 7 file ở lần cập nhật trước. Kiểm thử và commit remote ghi tại docs/PLAN.md.
