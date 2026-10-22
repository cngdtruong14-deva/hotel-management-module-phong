package module_phong.service;

import module_phong.dao.HoSoPhongDAO;
import module_phong.model.HoSoPhong.*;
import module_datphong.model.DatPhong;
import java.util.*;
import static module_phong.service.HoSoPhongService.*;

/** Nhan phan hoi tu luu tru va doi chieu ma khach, phong, phieu voi DatPhong. */
public final class TiepNhanLuuTruService implements CongLuuTru {
    private final HoSoPhongDAO dao;

    public TiepNhanLuuTruService(HoSoPhongDAO dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    private DatPhong verified(HoSoPhongDAO.Session s, String dp, String room, String kh) {
        reference(dp);
        reference(room);
        reference(kh);
        s.khach(kh);
        s.phong(room);
        var p = s.datPhong(dp);
        require(
                p.maPhong().equals(room) && p.maKhachHang().equals(kh),
                "Khách/phòng không khớp lượt lưu trú nguồn.");
        return p;
    }

    public List<YeuCau> yeuCauChoXuLy() {
        return dao.giaoDich(session -> {
            List<YeuCau> danhSachCho = new ArrayList<>();
            for (YeuCau yeuCau : session.doc().yeuCaus()) {
                String trangThai = yeuCau.trangThai();
                if (Set.of("CHO_XU_LY", "TIEP_NHAN").contains(trangThai)) {
                    danhSachCho.add(yeuCau);
                }
            }
            // Giu thu tu trong DAO va khong cho ben goi sua danh sach ket qua.
            return List.copyOf(danhSachCho);
        });
    }

    public void traKetQua(String id, String state, String dp, String reason, long version) {
        code(id);
        text(reason, 1000, false);
        require(
                Set.of("CHO_XU_LY", "TIEP_NHAN", "TU_CHOI", "HOAN_TAT").contains(state),
                "Trạng thái yêu cầu không hợp lệ.");
        require(version >= 0, "Phiên bản không hợp lệ.");
        dao.giaoDich(
                s -> {
                    var old =
                            s.doc().yeuCaus().stream()
                                    .filter(p -> p.maYeuCau().equals(id))
                                    .findFirst()
                                    .orElseThrow(
                                            () ->
                                                    new IllegalArgumentException(
                                                            "Không tìm thấy yêu cầu."));
                    if (version < old.phienBan()) return null;
                    if (version == old.phienBan()) {
                        require(
                                old.trangThai().equals(state)
                                        && Objects.equals(old.maDatPhong(), dp)
                                        && old.lyDo().equals(reason),
                                "Phản hồi cùng phiên bản nhưng khác nội dung.");
                        return null;
                    }
                    require(
                            !Set.of("TU_CHOI", "HOAN_TAT").contains(old.trangThai()),
                            "Yêu cầu đã kết thúc.");
                    require(!state.equals("CHO_XU_LY"), "Không quay lại chờ xử lý.");
                    if (state.equals("HOAN_TAT")) {
                        var p = verified(s, dp, old.maPhong(), old.maKhachHang());
                        require(!p.trangThai().name().equals("DA_HUY"), "Phiếu nguồn đã hủy.");
                    } else require(dp == null, "Chỉ kết quả hoàn tất gắn phiếu đặt.");
                    if (state.equals("TU_CHOI")) text(reason, 1000, true);
                    s.luu(
                            new YeuCau(
                                    id,
                                    old.maKhachHang(),
                                    old.maPhong(),
                                    old.ghiChu(),
                                    state,
                                    dp,
                                    reason,
                                    version,
                                    old.taoLuc(),
                                    now()));
                    return null;
                });
    }

    public void nhanTrangThai(LuuTru p) {
        require(
                p != null && p.capNhatLuc() != null && p.phienBan() > 0,
                "Thiếu thời gian/phiên bản.");
        dao.giaoDich(
                s -> {
                    var old =
                            s.doc().luuTrus().stream()
                                    .filter(x -> x.maDatPhong().equals(p.maDatPhong()))
                                    .findFirst()
                                    .orElse(null);
                    if (old != null) {
                        require(
                                old.maPhong().equals(p.maPhong())
                                        && old.maKhachHang().equals(p.maKhachHang()),
                                "Không được đổi liên kết thông báo.");
                        if (p.phienBan() < old.phienBan()) return null;
                        if (p.phienBan() == old.phienBan()) {
                            require(old.equals(p), "Trùng phiên bản khác nội dung.");
                            return null;
                        }
                    }
                    var actual = verified(s, p.maDatPhong(), p.maPhong(), p.maKhachHang());
                    require(
                            actual.trangThai().name().equals(p.trangThai()),
                            "Trạng thái không khớp phiếu nguồn.");
                    s.luu(p);
                    return null;
                });
    }

    public void nhanDanhGia(DanhGia p) {
        code(p.maDanhGia());
        text(p.noiDung(), 2000, true);
        require(
                p.diem() >= 1 && p.diem() <= 5 && p.taoLuc() != null,
                "Điểm từ 1 đến 5; cần ngày đánh giá.");
        dao.giaoDich(
                s -> {
                    var actual = verified(s, p.maDatPhong(), p.maPhong(), p.maKhachHang());
                    require(
                            actual.trangThai().name().equals("DA_TRA"),
                            "Chỉ nhận review sau lượt ở đã trả phòng.");
                    var old =
                            s.doc().danhGias().stream()
                                    .filter(x -> x.maDanhGia().equals(p.maDanhGia()))
                                    .findFirst()
                                    .orElse(null);
                    if (old != null) {
                        require(old.equals(p), "Không ghi đè review khách.");
                        return null;
                    }
                    require(
                            s.doc().danhGias().stream()
                                    .noneMatch(x -> x.maDatPhong().equals(p.maDatPhong())),
                            "Lượt ở đã có review.");
                    s.luu(p);
                    return null;
                });
    }

    public void nhanPhanAnh(PhanAnh p) {
        code(p.maPhanAnh());
        text(p.noiDung(), 2000, true);
        require(p.phienBan() > 0 && p.capNhatLuc() != null, "Thiếu phiên bản/thời gian.");
        require(
                Set.of("MOI", "DANG_XU_LY", "DA_XU_LY").contains(p.trangThai()),
                "Trạng thái phản ánh không hợp lệ.");
        dao.giaoDich(
                s -> {
                    var actual = verified(s, p.maDatPhong(), p.maPhong(), p.maKhachHang());
                    require(
                            Set.of("DANG_O", "DA_TRA").contains(actual.trangThai().name()),
                            "Phản ánh cần lượt ở thực tế.");
                    var old =
                            s.doc().phanAnhs().stream()
                                    .filter(x -> x.maPhanAnh().equals(p.maPhanAnh()))
                                    .findFirst()
                                    .orElse(null);
                    if (old != null) {
                        require(
                                old.maDatPhong().equals(p.maDatPhong())
                                        && old.maKhachHang().equals(p.maKhachHang())
                                        && old.maPhong().equals(p.maPhong()),
                                "Không đổi liên kết phản ánh.");
                        if (p.phienBan() < old.phienBan()) return null;
                        if (p.phienBan() == old.phienBan()) {
                            require(old.equals(p), "Trùng phiên bản khác nội dung.");
                            return null;
                        }
                        require(
                                !old.trangThai().equals("DA_XU_LY")
                                        || p.trangThai().equals("DA_XU_LY"),
                                "Phản ánh đã đóng.");
                    }
                    s.luu(p);
                    return null;
                });
    }
}
