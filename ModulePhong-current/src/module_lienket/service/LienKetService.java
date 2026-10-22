package module_lienket.service;

import module_lienket.dao.LienKetDAO;
import module_lienket.model.DuLieuLienKet;
import module_phong.service.PhongService;
import module_khachhang.model.KhachHang;
import module_dichvu.model.*;
import module_datphong.model.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static module_datphong.model.TrangThaiDatPhong.*;

/** Noi danh muc chung voi nghiep vu dat phong va su dung dich vu. */
public class LienKetService {
    private final LienKetDAO dao;
    private final PhongService phongService;
    private static final ZoneId HOTEL_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public LienKetService(LienKetDAO dao, PhongService phongService) {
        this.dao = dao;
        this.phongService = phongService;
    }

    private static void require(boolean ok, String msg) {
        if (!ok) throw new IllegalArgumentException(msg);
    }

    private static void code(String v) {
        require(
                v != null && v.matches("[A-Z0-9_-]{1,20}"),
                "Mã phải gồm 1–20 ký tự A–Z, 0–9, _ hoặc -.");
    }

    private static void text(String v, int max, String field) {
        require(
                v != null && !v.isBlank() && v.length() <= max,
                field + " bắt buộc, tối đa " + max + " ký tự.");
    }

    private static void optional(String v, int max, String field) {
        require(v != null && v.length() <= max, field + " tối đa " + max + " ký tự.");
    }

    private static void money(BigDecimal v) {
        require(
                v != null
                        && v.signum() > 0
                        && v.stripTrailingZeros().scale() <= 2
                        && v.compareTo(new BigDecimal("9999999999999999.99")) <= 0,
                "Đơn giá phải dương, tối đa 2 số thập phân.");
    }

    public DuLieuLienKet taiDuLieu() {
        return dao.giaoDich(LienKetDAO.Session::doc);
    }

    public KhachHang getKhachHang(String ma) {
        return taiDuLieu().khachHangs().stream()
                .filter(k -> k.maKhachHang().equals(ma))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng."));
    }

    public DichVu getDichVu(String ma) {
        return taiDuLieu().dichVus().stream()
                .filter(k -> k.maDichVu().equals(ma))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ."));
    }

    public List<DatPhong> getDatPhongTheoPhong(String maPhong) {
        return taiDuLieu().datPhongs().stream().filter(p -> p.maPhong().equals(maPhong)).toList();
    }

    public List<SuDungDichVu> getDichVuTheoDatPhong(String maDatPhong) {
        return taiDuLieu().suDungs().stream()
                .filter(p -> p.maDatPhong().equals(maDatPhong))
                .toList();
    }

    public BigDecimal getTienDichVu(String maDatPhong) {
        return getDichVuTheoDatPhong(maDatPhong).stream()
                .map(SuDungDichVu::thanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void luuDichVu(DichVu p, boolean sua) {
        code(p.maDichVu());
        text(p.tenDichVu(), 100, "Tên dịch vụ");
        money(p.donGia());
        text(p.donViTinh(), 30, "Đơn vị tính");
        dao.giaoDich(
                s -> {
                    s.luuDichVu(p, sua);
                    return null;
                });
    }

    public void taoDatPhong(String maDatPhong, String maKhachHang, String maPhong) {
        code(maDatPhong);
        dao.giaoDich(
                s -> {
                    var data = s.doc();
                    require(
                            data.khachHangs().stream()
                                    .anyMatch(
                                            k ->
                                                    k.maKhachHang().equals(maKhachHang)
                                                            && k.trangThai()
                                                                    == module_khachhang.model
                                                                            .TrangThaiKhachHang
                                                                            .HOAT_DONG),
                            "Khách hàng không tồn tại hoặc đã ngừng hoạt động.");
                    require(
                            data.datPhongs().stream()
                                    .noneMatch(p -> p.maDatPhong().equals(maDatPhong)),
                            "Mã đặt phòng đã tồn tại.");
                    require(
                            data.datPhongs().stream()
                                    .noneMatch(
                                            p ->
                                                    p.maPhong().equals(maPhong)
                                                            && (p.trangThai() == GIU_PHONG
                                                                    || p.trangThai() == DANG_O)),
                            "Phòng có lượt đặt/lưu trú đang hoạt động.");
                    var ps = s.phongSession();
                    phongService.giuPhong(ps, maPhong);
                    s.luuDatPhong(
                            new DatPhong(
                                    maDatPhong,
                                    maKhachHang,
                                    maPhong,
                                    phongService.getGiaHienHanh(ps, maPhong),
                                    LocalDateTime.now(HOTEL_ZONE),
                                    null,
                                    null,
                                    GIU_PHONG),
                            false);
                    return null;
                });
    }

    public void checkIn(String maDatPhong) {
        chuyen(maDatPhong, GIU_PHONG, DANG_O);
    }

    public void checkOut(String maDatPhong) {
        chuyen(maDatPhong, DANG_O, DA_TRA);
    }

    public void huyDatPhong(String maDatPhong) {
        chuyen(maDatPhong, GIU_PHONG, DA_HUY);
    }

    private void chuyen(String ma, TrangThaiDatPhong tu, TrangThaiDatPhong den) {
        dao.giaoDich(
                s -> {
                    DatPhong p = dat(s.doc(), ma);
                    require(p.trangThai() == tu, "Trạng thái lượt đặt/lưu trú không hợp lệ.");
                    if (den == DANG_O) phongService.checkIn(s.phongSession(), p.maPhong());
                    else if (den == DA_TRA) phongService.checkOut(s.phongSession(), p.maPhong());
                    else phongService.huyGiuPhong(s.phongSession(), p.maPhong());
                    LocalDateTime now = LocalDateTime.now(HOTEL_ZONE);
                    s.luuDatPhong(
                            new DatPhong(
                                    p.maDatPhong(),
                                    p.maKhachHang(),
                                    p.maPhong(),
                                    p.giaPhongChot(),
                                    p.ngayDat(),
                                    den == DANG_O ? now : p.ngayNhan(),
                                    den == DA_TRA ? now : p.ngayTra(),
                                    den),
                            true);
                    return null;
                });
    }

    private static DatPhong dat(DuLieuLienKet data, String ma) {
        return data.datPhongs().stream()
                .filter(p -> p.maDatPhong().equals(ma))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã đặt phòng."));
    }

    public void ghiNhanDichVu(String maDatPhong, String maDichVu, int soLuong) {
        require(soLuong > 0 && soLuong <= 10000, "Số lượng từ 1 đến 10000.");
        dao.giaoDich(
                s -> {
                    var data = s.doc();
                    DatPhong dp = dat(data, maDatPhong);
                    require(dp.trangThai() == DANG_O, "Chỉ ghi nhận dịch vụ cho khách đang ở.");
                    DichVu dv =
                            data.dichVus().stream()
                                    .filter(v -> v.maDichVu().equals(maDichVu) && !v.ngungSuDung())
                                    .findFirst()
                                    .orElseThrow(
                                            () ->
                                                    new IllegalArgumentException(
                                                            "Dịch vụ không tồn tại hoặc đã ngừng sử dụng."));
                    BigDecimal total = dv.donGia().multiply(BigDecimal.valueOf(soLuong));
                    require(
                            total.compareTo(new BigDecimal("9999999999999999.99")) <= 0,
                            "Thành tiền vượt giới hạn DECIMAL(18,2).");
                    s.themSuDung(
                            new SuDungDichVu(
                                    UUID.randomUUID().toString(),
                                    maDatPhong,
                                    maDichVu,
                                    soLuong,
                                    dv.donGia(),
                                    LocalDateTime.now(HOTEL_ZONE)));
                    return null;
                });
    }

    public void napDanhMucMau() {
        luuDichVu(
                new DichVu("DV001", "Minibar - nước uống", new BigDecimal("20000"), "Chai", false),
                false);
        luuDichVu(new DichVu("DV002", "Giặt ủi", new BigDecimal("30000"), "Kg", false), false);
        luuDichVu(new DichVu("DV003", "Ăn sáng", new BigDecimal("80000"), "Suất", false), false);
    }
}
