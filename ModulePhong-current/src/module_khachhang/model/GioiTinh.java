package module_khachhang.model;

/** KHONG_CUNG_CAP được dùng khi khách chưa cung cấp thông tin. */
public enum GioiTinh {
    NAM("Nam"),
    NU("Nữ"),
    KHAC("Khác"),
    KHONG_CUNG_CAP("Không cung cấp");

    private final String nhan;

    GioiTinh(String nhan) {
        this.nhan = nhan;
    }

    public String nhan() {
        return nhan;
    }
}
