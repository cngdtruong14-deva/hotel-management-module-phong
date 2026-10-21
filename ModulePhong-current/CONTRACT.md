# Hợp đồng tích hợp — 2026.10.21.1 / room-simple-1

SQL Server được giữ theo lựa chọn người dùng. Từ bản này SQL có bảng/PK/FK/NOT NULL/DEFAULT; không procedures, triggers hoặc CHECK; giữ một filtered UNIQUE index giấy tờ của khách hàng. Nghiệp vụ được kiểm tra ở Java Service. Các hướng dẫn gọi sp_* của phiên bản cũ hết hiệu lực.

Database local HotelManagementDB_LocalSimple, dbo; máy chủ chung có thể dùng HotelManagementDB nếu khởi tạo cùng schema. ConnectionFactory/DBConnection vẫn dùng db.url/db.user/db.password và HOTEL_DB_*.

## Owner và cấu trúc dữ liệu

Khách hàng sở hữu KhachHang 13 trường. Quản lý phòng sở hữu phòng/loại/tầng/giá/mô tả/tiện nghi/vệ sinh/bảo trì; gửi yêu cầu, đọc review/phản ánh. Đặt phòng & Lưu trú sở hữu phiếu/nhận/trả/hủy/đổi và trả kết quả. Dịch vụ sở hữu danh mục/tiêu dùng. Không thêm nút booking vào UI phòng.

Mã String/VARCHAR(20), không đổi sang INT như mẫu MySQL. KhachHang → DatPhong ← Phong; DatPhong → SuDungDichVu ← DichVu. GiaPhongChot/DonGia dòng dịch vụ giữ giá lịch sử. NgayNhan/NgayTra là thời điểm thực tế nullable. Tên cột/kiểu đầy đủ tại sql/01_bang_chinh.sql và 02_bang_chuc_nang.sql.

Các bảng phản hồi vẫn có MaDatPhong/MaPhong/MaKhachHang nhưng mỗi FK chỉ kiểm tra mã tồn tại. Service Java phải kiểm tra ba mã cùng nguồn, điểm1–5, trạng thái/phiên bản, review sau trả phòng. Đã bỏ FK tổng hợp và CHECK SQL theo yêu cầu đơn giản hóa; direct SQL có thể bỏ qua quy tắc Java.

LuotLuuTru/ChuyenPhong và MaLuotLuuTru trên DatPhong giữ lịch sử đổi phòng. Không tự tạo nhóm bằng trigger; phía Java tạo nhóm khi cần. Mỗi phiếu là phân đoạn một phòng, khi chuyển giữ phiếu cũ/feedback/giá, tạo phiếu mới cùng lượt. Chi tiết và ví dụ JDBC transaction cần thực hiện ở sql/README.md, file05 minh họa SQL. Service đổi phòng thành viên chưa được tích hợp.

## Model / API khách hàng

KhachHang là record 13 trường theo thứ tự source. Tạo/sửa qua DuLieuKhachHang và KhachHangService, không tạo record 6 trường. sdt() là alias nullable; cccd() chỉ trả CCCD, Passport trả null. Dùng soGiayTo()/loaiGiayTo()/quocGiaCap() để hiển thị giấy tờ đầy đủ.

- themKhachHang(DuLieuKhachHang): trả KhachHang và mã tự sinh.
- suaKhachHang(String ma, DuLieuKhachHang): giữ mã/ngày tạo/trạng thái.
- getKhachHang(String ma): đọc cả khách đã ngừng hoạt động.
- getKhachHangs()/getKhachHangHoatDong(): chỉ khách hoạt động, dùng cho đặt mới.
- timKhachHang(new BoLocKhachHang(null,null,null,null)): mọi trạng thái, dùng lịch sử.
- voHieuHoa(ma), kichHoatLai(ma): không xóa hồ sơ.
- getKhachHangChoDatPhong(Connection,ma): kiểm tra trong transaction caller; không tự commit.

API luuKhachHang(KhachHang,boolean) và constructor KhachHangService(LienKetService) đã bỏ. Phần liên kết không sở hữu CRUD khách nữa. DAO khách sở hữu dữ liệu duy nhất; MemoryLienKetDAO nhận cùng MemoryKhachHangDAO đang dùng bởi service khách.

## Panel, controller và biến

Ghép `new PhongView(phongController,hoSoController,mode)` vào JTabbedPane. Constructor một tham số PhongView(PhongController) chỉ tạo các tab phòng cũ. KhachHangPanel(KhachHangController,String mode) vẫn là JPanel, gọi taiLai() khi vào tab. HoSoPhongController(HoSoPhongService,PhongService,KhachHangService) dùng SwingWorker; tạo và thao tác controller/UI trên EDT, JDBC chạy nền. onSaved(hoSoController::refresh) nối thay đổi khách với danh sách chọn khách.

| Lớp | Field private | Vai trò |
|---|---|---|
| PhongView | tabPhong, pnlPhong, pnlLoaiPhong, pnlTang, pnlSoDoPhong | Tab quản lý phòng |
| PhongView | cboLocTang, cboLocTrangThai | Bộ lọc |
| HoSoPhongView | cboPhong, cboKhach | Phòng được xem, khách gửi yêu cầu |
| HoSoPhongView | txtMoTa, txtMaTienNghi, txtTenTienNghi, spnSoLuong, cboTinhTrang | Mô tả/thiết bị |
| HoSoPhongView | txtMaYeuCau, txtGhiChu | Yêu cầu chuyển lưu trú |
| HoSoPhongView | amenities, reviews, requests, stays, issues | DefaultTableModel chỉ đọc; thay đổi qua form/service |
| KhachHangPanel | tblKhachHang | Hồ sơ khách |

Module khác gọi service/public methods, không truy cập field UI. Phong(maPhong,soPhong,maLoaiPhong,maTang,giaPhong,trangThai,ngungSuDung); DichVu(maDichVu,tenDichVu,donGia,donViTinh,ngungSuDung).

## API quản lý phòng

`HoSoPhongService`: doc(), luuChiTiet(ChiTiet), luuTienNghi(TienNghi), guiYeuCau(String maYeuCau,String maKhachHang,String maPhong,String ghiChu).

Mã mới yêu cầu/tiện nghi/review/phản ánh: [A-Z0-9_-]{1,20}. UI sinh mã yêu cầu YC + 18 ký tự UUID. Gửi lặp cùng mã và payload không tạo bản sao; khác payload bị từ chối. Yêu cầu mới cần khách hoạt động, phòng chưa ngừng sử dụng; không giữ phòng, không tạo DatPhong, không đảm bảo còn trống. Chưa có ngày nhận/trả trong request; phía booking phải thu thập/kiểm tra trước khi xác nhận.

## Cổng nhận và transaction

CongLuuTru/TiepNhanLuuTruService vẫn có yeuCauChoXuLy(), traKetQua(id,state,maDP,lyDo,version), nhanTrangThai(LuuTru), nhanDanhGia(DanhGia), nhanPhanAnh(PhanAnh). DTO chính xác trong src/module_phong/model/HoSoPhong.java. PhienBan được giữ để tương thích Java; payload cùng phiên bản phải giống nhau, cũ bị bỏ qua theo kiểm tra service. Review một/phiếu đã DA_TRA; phản ánh từ DANG_O/DA_TRA. Cổng dành cho adapter nội bộ, caller xác thực khách và người thao tác.

Yêu cầu CHO_XU_LY/TIEP_NHAN/TU_CHOI/HOAN_TAT; chỉ HOAN_TAT gắn phiếu thật. Phiếu GIU_PHONG/DANG_O/DA_TRA/DA_HUY; phòng dùng DANG_SU_DUNG khi đang ở. Gửi yêu cầu không giữ phòng. UI refresh khi vào tab hoặc Tải lại. Trạng thái hiện tại đối chiếu DatPhong; lịch sử chỉ hiện sau khi nguồn gửi phản hồi.

JDBC nghiệp vụ khóa khách trước phòng/phiếu, cùng Connection cho thay đổi phiếu và phòng; rollback khi lỗi. Callback Java là transaction riêng sau booking commit; cần lưu kết quả để retry đúng mã/payload. Không có xử lý tự động hay SQL procedures trong bản này. Không tuyên bố consumer thành viên đã hoàn thiện.

## Quy trình nhận module

Lấy VERSION của source current làm nền, lưu đầu vào ở archive; so sánh schema/model/API/owner; sửa và kiểm thử source current; cập nhật phiên bản, hồ sơ, hash và ZIP. Không chép đè common/model bằng phiên bản khác. Chỉ một ZIP dự án current trong deliverables; bộ SQL độc lập ở handoff.


## NetBeans

Open Project tại ModulePhong-current; Run/F6 gọi HotelSharedApp SQL, dùng cùng HotelContext và config/db.properties. CheckDatabase/target check-db chỉ đọc để kiểm chứng schema/kết nối. SharedContextTest đã chạy qua JDBC, tạo khách/request/phiếu và callback rồi mở context mới đọc lại. SQL giữa source/gói độc lập khớp byte. NetBeans Services database connection không thay thế cấu hình ứng dụng. Xem NETBEANS_SQL_CHUNG.md; source UI thành viên vẫn chưa nhận.

## 2026.10.11.3 — Giữ nguyên module khách hàng

Khôi phục duy nhất index UX_KhachHang_GiayTo trên (LoaiGiayTo, QuocGiaCap, SoGiayTo), lọc SoGiayTo IS NOT NULL, đúng quy tắc SQL của module khách hàng gốc. Không đổi source Java, 13 cột, API, mã khách hay dữ liệu hiện có. Không thêm procedure/trigger/CHECK phức tạp.

Database mới chạy 00–03 như trước; file01 đã có index. Database room-simple-1 đã tạo: chạy riêng sql/06_chan_trung_giay_to.sql, kiểm tra tên USE trước. Script dừng và liệt kê nếu có giấy tờ trùng; không tự gộp/xóa/sửa khách. Có thể chạy lại an toàn. Không dùng file01 tạo bảng lại trên database có dữ liệu. Các kết nối ghi bảng cần SET ANSI_NULLS, QUOTED_IDENTIFIER, ANSI_PADDING, ANSI_WARNINGS, CONCAT_NULL_YIELDS_NULL, ARITHABORT ON và NUMERIC_ROUNDABORT OFF; DBConnection hiện có đã cấu hình.

Kiểm thử SQL Server/JDBC: lỗi trùng tái hiện trước sửa; sau sửa chặn INSERT/UPDATE trùng, cho phép sửa cùng hồ sơ, nhiều khách chưa có giấy tờ, cùng số hộ chiếu khác nước cấp; bật/tắt khách và hai INSERT đồng thời PASS. Migration chạy lại PASS; nâng cấp giữ nguyên mọi giá trị khách; dữ liệu trùng bị từ chối không tạo index. Chỉ chạy database kiểm thử riêng, chưa áp dụng database người dùng. Toàn bộ src/ giữ nguyên SHA256 so với đầu lần sửa. Google Docs đã được đọc lại xác nhận đủ 7 file SQL ngày 11/10/2026.

## Xuất bản GitHub — 2026.10.21.1

Hướng dẫn clone/chạy/ghép module ở README root repository. Không thay source Java hay SQL khi xuất bản. File cấu hình local, log kiểm thử và archive/work/audit không được theo dõi bằng Git. Schema vẫn room-simple-1; Google Docs đồng bộ nội dung SQL 2026.10.11.3.
