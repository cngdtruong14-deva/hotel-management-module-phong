# 2026.10.11.2 — rà soát dùng chung SQL và chạy NetBeans

- Thêm build.xml, nbproject/project.xml dạng Ant freeform, dùng JDBC sẵn trong lib; Run Project mở HotelSharedApp SQL. Build.ps1/JAR thống nhất main class shared.
- Thêm CheckDatabase read-only và tùy chọn đường dẫn config ở shared launcher. Loại nbproject/private khỏi release.
- SharedContextTest hỗ trợ SQL với guard database test riêng, đọc lại qua context mới để xác minh dữ liệu bền vững liên module.
- Cấu hình XSD NetBeans21, Ant build/test/check-db và test SQL đều đạt. Không đổi schema room-simple-1, không sửa DB chung; UI đặt phòng vẫn là bộ khung chờ source thành viên.

# 2026.10.11.1 — SQL đơn giản cho môn Java nâng cao

- Theo yêu cầu người dùng: giữ SQL Server, chuyển 00–08/migration/procedures thành 00–05 CREATE TABLE/seed/SELECT/demo dễ đọc.
- Bỏ CHECK, filtered index, FK tổng hợp, procedures/triggers/views; giữ PK/FK đơn, NOT NULL/DEFAULT, sequence khách và cột tính ThanhTien cần cho DAO.
- Giữ khách13, mã String, giá chốt, 17 bảng để không bỏ chức năng đã thống nhất. Dữ liệu nền có 6 phòng/3 khách và DatPhong trống. Java chịu trách nhiệm nghiệp vụ; đổi phòng cần service thành viên, có ví dụ transaction.
- SchemaContract không còn yêu cầu procedures/triggers. PortalTest xác minh FK mã phòng tồn tại và nguồn sai bị service chặn; sửa assertions chọn đúng phòng thay vì đếm/đọc dòng đầu toàn bộ fixture.
- Cài SQL mới, demo rollback, Java suites và PortalTest JDBC PASS. Không sửa database cũ; archive toàn bộ SQL/handoff phức tạp.

# 2026.10.09.3 — schema đầy đủ vòng đời phòng/lưu trú

- SQL08 bổ sung LuotLuuTru, ChuyenPhong, MaLuotLuuTru trên DatPhong; giữ phiếu từng phòng và toàn bộ FK feedback/dịch vụ. Trigger hỗ trợ Java cũ và chặn sửa định danh phiếu.
- Chín procedures xử lý yêu cầu, đặt/nhận/trả/hủy/đổi phòng, review, phản ánh và vệ sinh/bảo trì; cập nhật trạng thái/projection trong transaction.
- Thêm views lịch sử/khách đang ở/dịch vụ theo lượt; bộ SQL local có migration06 và demo07 rollback.
- Kiểm thử cài mới 17 bảng, nâng cấp phiếu cũ, đổi phòng nhiều lần, giá/feedback giữ nguyên, chặn thao tác sai và rollback khi lỗi muộn PASS. Java regression và PortalTest JDBC schema3 PASS.
- Lỗi môi trường kiểm thử: properties BOM được loại bỏ; xác thực Windows qua IP thất bại, hostname localhost + IPv4 thành công. Không đổi server/tài khoản.
- Source chính room-portal-3; database chung chưa áp migration, UI thành viên chưa tích hợp.

# 2026.10.09.2 — bộ khung dùng chung cho nhóm Lưu trú

- Thêm HotelContext.sql/demo, HotelSharedApp và DatPhongPanel/Controller đọc hàng đợi/phiếu, hai launcher shared.
- Thêm hướng dẫn BAN_GIAO_DAT_PHONG_LUU_TRU.md về database/server chung, API, điểm mở rộng, retry và tiêu chí bàn giao.
- Không đổi schema; khung chưa tự tạo booking/xử lý callback. SharedContextTest kiểm tra nguồn khách/phòng, queue và vòng booking/callback/trả phòng.

# 2026.10.09.1 — tách quản lý phòng và đặt phòng/lưu trú

- Thay UI liên kết booking/dịch vụ bằng hồ sơ phòng, tiện nghi, review chỉ đọc, yêu cầu chuyển bộ phận và trạng thái/phản ánh nhận về.
- Thêm HoSoPhong model, DAO memory/SQL, HoSoPhongService, CongLuuTru/TiepNhanLuuTruService, controller/view và migration SQL07 sáu bảng.
- Chống yêu cầu lặp/xung đột, callback cũ, sai khách/phòng/phiếu; đối chiếu trạng thái DatPhong, review sau trả phòng; giữ mã tham chiếu cũ.
- Archive UI/controller/test liên kết cũ; giữ backend tương thích và dữ liệu lịch sử. Cập nhật schema contract room-portal-2.
- Kiểm thử memory/render và SQL mới/nâng cấp PASS; sửa assertion FK bị UNIQUE che lấp và thời điểm render khi worker chưa xong.
- Hồ sơ phân chia/nhật ký/API/schema được đóng gói cùng source. Chưa có consumer thành viên thật; chưa nâng cấp database chung.

# 2026.10.05.2 — dọn dự án khác và rà soát ràng buộc

- Xóa dự án hotel.* ở gốc theo yêu cầu, lưu inventory hash tại ../audit.
- Bổ sung REQUIREMENTS_AUDIT.md và bảng tên panel/biến trong CONTRACT. Không đổi nghiệp vụ Java.
- Chạy lại test độc lập sau khi bỏ toàn bộ dependency của dự án kia; SQL mới/nâng cấp trên database _Audit PASS.
- Lệnh xóa đầu bị auto-review chặn; sau khi cung cấp bằng chứng sở hữu và yêu cầu mới của người dùng, được chấp thuận.
- Lần ghi log test đầu dùng đường dẫn tương đối bị thay đổi bởi Push-Location; đã dùng đường dẫn tuyệt đối khi chạy lại.

# 2026.10.05.1 — tích hợp Phòng + Khách hàng v2

- Giải quyết lỗi constructor 6/13 trường và các lớp trùng bằng một model/service khách hàng duy nhất.
- Bỏ form CRUD khách cũ trong LienKetView; dùng KhachHangPanel của Đức Việt.
- Cập nhật DAO liên kết đọc schema 13 trường, xử lý nullable và Passport; không giới hạn email ở 100 ký tự nữa qua service phòng.
- Demo và SQL cùng chia sẻ khách hàng với phần phòng, cập nhật UI khi lưu khách và đổi tab.
- Chặn đặt mới cho khách ngừng hoạt động; lịch sử giữ nguyên. Thứ tự khóa khách → phòng thống nhất.
- Schema mới SQL 04, migration bảo toàn khóa SQL 06; khởi động SQL kiểm tra schema.
- Build sạch, test nghiệp vụ tích hợp, kho archive và hướng dẫn AGENTS.md xác định bản nguồn chính.

## Nhật ký lỗi kiểm tra

- Trước sửa: bản khách hàng v2 compile riêng OK; ghép với ModulePhong còn 5 lỗi compile (constructor và kiểu suy luận phát sinh), tên cột SQL SDT/CCCD chưa khớp.
- Sau sửa: build Java17 và test memory/SQL mới/SQL nâng cấp đều PASS.
- Lần chạy test đầu: PowerShell tách -Djava.awt.headless=true; đã sửa bằng chuỗi có dấu nháy. Không phải lỗi source.
- SQL chạy trong sandbox thiếu thông tin xác thực Windows; chạy kiểm thử với quyền cục bộ trên database thử nghiệm riêng thành công.

## 2026.10.11.3 — Giữ nguyên module khách hàng

Khôi phục duy nhất index UX_KhachHang_GiayTo trên (LoaiGiayTo, QuocGiaCap, SoGiayTo), lọc SoGiayTo IS NOT NULL, đúng quy tắc SQL của module khách hàng gốc. Không đổi source Java, 13 cột, API, mã khách hay dữ liệu hiện có. Không thêm procedure/trigger/CHECK phức tạp.

Database mới chạy 00–03 như trước; file01 đã có index. Database room-simple-1 đã tạo: chạy riêng sql/06_chan_trung_giay_to.sql, kiểm tra tên USE trước. Script dừng và liệt kê nếu có giấy tờ trùng; không tự gộp/xóa/sửa khách. Có thể chạy lại an toàn. Không dùng file01 tạo bảng lại trên database có dữ liệu. Các kết nối ghi bảng cần SET ANSI_NULLS, QUOTED_IDENTIFIER, ANSI_PADDING, ANSI_WARNINGS, CONCAT_NULL_YIELDS_NULL, ARITHABORT ON và NUMERIC_ROUNDABORT OFF; DBConnection hiện có đã cấu hình.

Kiểm thử SQL Server/JDBC: lỗi trùng tái hiện trước sửa; sau sửa chặn INSERT/UPDATE trùng, cho phép sửa cùng hồ sơ, nhiều khách chưa có giấy tờ, cùng số hộ chiếu khác nước cấp; bật/tắt khách và hai INSERT đồng thời PASS. Migration chạy lại PASS; nâng cấp giữ nguyên mọi giá trị khách; dữ liệu trùng bị từ chối không tạo index. Chỉ chạy database kiểm thử riêng, chưa áp dụng database người dùng. Toàn bộ src/ giữ nguyên SHA256 so với đầu lần sửa. Google Docs cũ chưa cập nhật bản vá này.

## 2026.10.21.1 — Bàn giao GitHub

Chuẩn bị repository chia sẻ source current, 7 file SQL và hướng dẫn NetBeans/JDBC. Cập nhật README root, lọc cấu hình local/log/lịch sử khỏi Git; giữ nguyên Java/SQL và source khách hàng. Google Docs đã đồng bộ đủ 7 file ở lần cập nhật trước. Kiểm thử và commit remote ghi tại docs/PLAN.md.
