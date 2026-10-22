package module_phong.service;

import module_phong.dao.HoSoPhongDAO;
import module_datphong.model.DatPhong;
import module_phong.model.HoSoPhong.*;
import module_khachhang.model.TrangThaiKhachHang;
import java.time.*;
import java.util.*;

/** Xu ly ho so phong va gui yeu cau; nghiep vu nhan, tra phong thuoc module luu tru. */
public final class HoSoPhongService {
    private final HoSoPhongDAO dao;

    public HoSoPhongService(HoSoPhongDAO dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    static void require(boolean b, String m) {
        if (!b) throw new IllegalArgumentException(m);
    }

    static void code(String v) {
        require(
                v != null && v.matches("[A-Z0-9_-]{1,20}"),
                "Mã gồm 1–20 ký tự A–Z, 0–9, _ hoặc -.");
    }

    static void reference(String v) {
        require(
                v != null && !v.isBlank() && v.length() <= 20,
                "Mã tham chiếu phải tồn tại, tối đa 20 ký tự.");
    }

    static void text(String v, int max, boolean required) {
        require(
                v != null && v.length() <= max && (!required || !v.isBlank()),
                "Nội dung không hợp lệ (tối đa " + max + " ký tự).");
    }

    static LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).withNano(0);
    }

    public DuLieu doc() {
        return dao.giaoDich(session -> {
            DuLieu duLieu = session.doc();
            List<LuuTru> danhSachLuuTru = new ArrayList<>();

            // Lay trang thai hien tai tu phieu, khong dung trang thai phan hoi cu.
            for (LuuTru luuTru : duLieu.luuTrus()) {
                DatPhong phieuDat = session.datPhong(luuTru.maDatPhong());
                boolean dungPhong = phieuDat.maPhong().equals(luuTru.maPhong());
                boolean dungKhach = phieuDat.maKhachHang().equals(luuTru.maKhachHang());
                require(dungPhong && dungKhach, "Liên kết lưu trú thay đổi; cần đồng bộ lại.");

                LuuTru trangThaiMoi = new LuuTru(
                        luuTru.maDatPhong(), luuTru.maPhong(), luuTru.maKhachHang(),
                        phieuDat.trangThai().name(), luuTru.phienBan(), luuTru.capNhatLuc());
                danhSachLuuTru.add(trangThaiMoi);
            }

            // Tra ve ban sao chi doc, tranh giao dien sua truc tiep du lieu service.
            return new DuLieu(
                    duLieu.chiTiets(), duLieu.tienNghis(), duLieu.yeuCaus(),
                    List.copyOf(danhSachLuuTru), duLieu.danhGias(), duLieu.phanAnhs());
        });
    }

    public void luuChiTiet(ChiTiet p) {
        code(p.maPhong());
        text(p.moTa(), 2000, false);
        dao.giaoDich(
                s -> {
                    s.phong(p.maPhong());
                    s.luu(p);
                    return null;
                });
    }

    public void luuTienNghi(TienNghi p) {
        code(p.maPhong());
        code(p.maTienNghi());
        text(p.tenTienNghi(), 100, true);
        require(p.soLuong() >= 0 && p.soLuong() <= 10000, "Số lượng từ 0 đến 10000.");
        require(
                Set.of("TOT", "CAN_SUA", "NGUNG_DUNG").contains(p.tinhTrang()),
                "Tình trạng thiết bị không hợp lệ.");
        dao.giaoDich(
                s -> {
                    s.phong(p.maPhong());
                    s.luu(p);
                    return null;
                });
    }

    public void guiYeuCau(String id, String kh, String phong, String ghiChu) {
        code(id);
        reference(kh);
        reference(phong);
        text(ghiChu, 1000, false);
        dao.giaoDich(
                s -> {
                    // Thu tu khoa: khach hang, phong, roi den yeu cau va phan hoi.
                    var customer = s.khach(kh);
                    var room = s.phong(phong);
                    var old =
                            s.doc().yeuCaus().stream()
                                    .filter(x -> x.maYeuCau().equals(id))
                                    .findFirst()
                                    .orElse(null);
                    if (old != null) {
                        require(
                                old.maKhachHang().equals(kh)
                                        && old.maPhong().equals(phong)
                                        && old.ghiChu().equals(ghiChu),
                                "Mã yêu cầu đã có nội dung khác.");
                        return null;
                    }
                    require(
                            customer.trangThai() == TrangThaiKhachHang.HOAT_DONG,
                            "Khách đã ngừng hoạt động.");
                    require(!room.ngungSuDung(), "Phòng đã ngừng sử dụng.");
                    var time = now();
                    s.luu(new YeuCau(id, kh, phong, ghiChu, "CHO_XU_LY", null, "", 0, time, time));
                    return null;
                });
    }
}
