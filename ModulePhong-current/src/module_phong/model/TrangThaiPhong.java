package module_phong.model;

public enum TrangThaiPhong {
    TRONG("Trống"),
    DA_DAT("Đã đặt"),
    DANG_SU_DUNG("Đang sử dụng"),
    DANG_DON("Đang dọn"),
    BAO_TRI("Bảo trì");
    private final String nhan;

    TrangThaiPhong(String nhan) {
        this.nhan = nhan;
    }

    public String toString() {
        return nhan;
    }
}
