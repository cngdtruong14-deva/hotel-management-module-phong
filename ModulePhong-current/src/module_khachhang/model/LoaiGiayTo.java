package module_khachhang.model;

/** Lưu name() vào database; nhãn có dấu chỉ dùng trên giao diện. */
public enum LoaiGiayTo {
    CCCD("CCCD"),
    PASSPORT("Hộ chiếu");

    private final String nhan;

    LoaiGiayTo(String nhan) {
        this.nhan = nhan;
    }

    public String nhan() {
        return nhan;
    }
}
