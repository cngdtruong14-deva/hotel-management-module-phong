package module_khachhang.model;

/** Hồ sơ ngừng hoạt động vẫn được giữ để tra cứu lịch sử. */
public enum TrangThaiKhachHang {
    HOAT_DONG("Hoạt động"),
    NGUNG_HOAT_DONG("Ngừng hoạt động");

    private final String nhan;

    TrangThaiKhachHang(String nhan) {
        this.nhan = nhan;
    }

    public String nhan() {
        return nhan;
    }
}
