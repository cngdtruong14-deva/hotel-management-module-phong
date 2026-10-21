package module_khachhang.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import module_khachhang.dao.*;
import module_khachhang.model.*;

/** API nghiệp vụ dùng chung. Không gọi trực tiếp trên Swing EDT. */
public final class KhachHangService {
    private static final Set<String> MA_QUOC_GIA = Set.of(Locale.getISOCountries());
    private final KhachHangDAO dao;
    private final Clock clock;
    public KhachHangService(KhachHangDAO dao) {
        this(dao, Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
    }
    public KhachHangService(KhachHangDAO dao, Clock clock) {
        this.dao = Objects.requireNonNull(dao);
        this.clock = Objects.requireNonNull(clock);
    }
    public KhachHang themKhachHang(DuLieuKhachHang d) {
        return dao.them(chuanHoa(d), LocalDateTime.now(clock).withNano(0));
    }
    public void suaKhachHang(String ma, DuLieuKhachHang d) { dao.sua(ma, chuanHoa(d)); }
    public KhachHang getKhachHang(String ma) { return dao.get(ma); }
    public List<KhachHang> timKhachHang(BoLocKhachHang filter) {
        Objects.requireNonNull(filter);
        return dao.danhSach().stream().filter(filter::khop).toList();
    }
    public List<KhachHang> getKhachHangHoatDong() {
        return timKhachHang(new BoLocKhachHang(null, null, null, TrangThaiKhachHang.HOAT_DONG));
    }
    /** Danh sách cho bộ chọn khách của module Phòng/Đặt phòng. */
    public List<KhachHang> getKhachHangs() {
        return getKhachHangHoatDong();
    }
    public void voHieuHoa(String ma) { dao.doiTrangThai(ma, TrangThaiKhachHang.NGUNG_HOAT_DONG); }
    public void kichHoatLai(String ma) { dao.doiTrangThai(ma, TrangThaiKhachHang.HOAT_DONG); }

    /** Connection phải nằm trong transaction của caller; không commit/rollback tại đây. */
    public KhachHang getKhachHangChoDatPhong(Connection c, String ma) throws SQLException {
        if (c.getAutoCommit()) throw new IllegalArgumentException("Booking cần transaction với autoCommit=false.");
        var p = SqlServerKhachHangDAO.docChoDatPhong(c, ma);
        if (p.trangThai() != TrangThaiKhachHang.HOAT_DONG)
            throw new IllegalArgumentException("Khách hàng đã ngừng hoạt động.");
        return p;
    }
    public List<String> canhBaoTrung(String ma, DuLieuKhachHang input) {
        var d = chuanHoa(input);
        List<String> ketQua = new ArrayList<>();
        for (KhachHang khach : dao.danhSach()) {
            if (khach.maKhachHang().equals(ma)) continue;
            boolean trungDienThoai = d.soDienThoai() != null && d.soDienThoai().equals(khach.soDienThoai());
            boolean trungHoTenNgaySinh = d.ngaySinh() != null && d.ngaySinh().equals(khach.ngaySinh())
                    && d.hoTen().equalsIgnoreCase(khach.hoTen());
            if (trungDienThoai || trungHoTenNgaySinh) {
                ketQua.add(khach.maKhachHang() + " — " + khach.hoTen());
            }
        }
        return List.copyOf(ketQua);
    }
    private static String text(String s, String label, int max) {
        if (s == null || s.isBlank()) return null;
        s = s.trim();
        if (s.length() > max) throw new IllegalArgumentException(label + " tối đa " + max + " ký tự.");
        return s;
    }
    private static String country(String s, String label) {
        s = text(s, label, 2);
        if (s == null) return null;
        s = s.toUpperCase(Locale.ROOT);
        if (!MA_QUOC_GIA.contains(s))
            throw new IllegalArgumentException(label + " phải là mã quốc gia 2 chữ, ví dụ VN, US, GB.");
        return s;
    }
    public DuLieuKhachHang chuanHoa(DuLieuKhachHang d) {
        if (d == null) throw new IllegalArgumentException("Thiếu dữ liệu khách hàng.");
        String name = text(d.hoTen(), "Họ tên", 100);
        if (name == null) throw new IllegalArgumentException("Họ tên không được để trống.");
        String doc = text(d.soGiayTo(), "Số giấy tờ", 30);
        if (doc != null) doc = doc.toUpperCase(Locale.ROOT);
        String issuer = country(d.quocGiaCap(), "Nước cấp giấy tờ");
        boolean noDoc = d.loaiGiayTo() == null && doc == null && issuer == null;
        if (!noDoc && (d.loaiGiayTo() == null || doc == null || issuer == null))
            throw new IllegalArgumentException("Chọn loại giấy tờ và nhập đủ số giấy tờ, nước cấp; hoặc để trống cả ba.");
        if (d.loaiGiayTo() == LoaiGiayTo.CCCD && (!"VN".equals(issuer) || !doc.matches("[0-9]{12}")))
            throw new IllegalArgumentException("CCCD gồm 12 chữ số, nước cấp VN.");
        if (d.loaiGiayTo() == LoaiGiayTo.PASSPORT && !doc.matches("[A-Z0-9]{1,30}"))
            throw new IllegalArgumentException("Passport chỉ gồm chữ và số, tối đa 30 ký tự.");
        String phone = d.soDienThoai();
        if (phone != null) phone = phone.replaceAll("[\\s().-]", "");
        phone = text(phone, "Số điện thoại", 20);
        if (phone != null && !phone.matches("\\+?[0-9]{8,15}"))
            throw new IllegalArgumentException("Điện thoại gồm 8–15 chữ số, có thể bắt đầu bằng +.");
        String email = text(d.email(), "Email", 254);
        if (email != null && !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new IllegalArgumentException("Email không đúng định dạng.");
        if (d.ngaySinh() != null && (d.ngaySinh().getYear() < 1 || d.ngaySinh().isAfter(LocalDate.now(clock))))
            throw new IllegalArgumentException("Ngày sinh phải hợp lệ và không ở tương lai.");
        return new DuLieuKhachHang(name, d.loaiGiayTo(), doc, issuer, phone, email,
                d.ngaySinh(), d.gioiTinh() == null ? GioiTinh.KHONG_CUNG_CAP : d.gioiTinh(),
                country(d.quocTich(), "Quốc tịch"), text(d.diaChi(), "Địa chỉ", 255));
    }
}
