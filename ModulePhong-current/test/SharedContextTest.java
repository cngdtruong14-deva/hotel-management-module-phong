import common.HotelContext;
import module_khachhang.model.DuLieuKhachHang;
import module_phong.model.HoSoPhong.LuuTru;
import java.time.LocalDateTime;

public class SharedContextTest {
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            var db = new common.DBConnection(java.nio.file.Path.of(args[0]));
            try (var c = db.open(); var st = c.createStatement(); var rs = st.executeQuery("SELECT DB_NAME()")) {
                rs.next();
                if (!rs.getString(1).startsWith("HotelManagement_Portal_Test_")) throw new IllegalStateException("Isolated test database only");
            }
        }
        var ctx = args.length == 0 ? HotelContext.demo() : HotelContext.sql(java.nio.file.Path.of(args[0]));
        var kh = ctx.khachHang().themKhachHang(new DuLieuKhachHang("Khách kiểm thử khung",null,null,null,null,null,null,null,null,null));
        ctx.hoSoPhong().guiYeuCau("YC_SHARED",kh.maKhachHang(),"P103","");
        if(ctx.congLuuTru().yeuCauChoXuLy().size()!=1) throw new AssertionError("Shared inbox");
        ctx.datPhong().taoDatPhong("DP_SHARED",kh.maKhachHang(),"P103");
        ctx.congLuuTru().traKetQua("YC_SHARED","HOAN_TAT","DP_SHARED","",1);
        ctx.datPhong().checkIn("DP_SHARED");
        ctx.congLuuTru().nhanTrangThai(new LuuTru("DP_SHARED","P103",kh.maKhachHang(),"DANG_O",1,LocalDateTime.now()));
        if(!ctx.hoSoPhong().doc().luuTrus().get(0).trangThai().equals("DANG_O")) throw new AssertionError("Shared stay");
        ctx.datPhong().checkOut("DP_SHARED");
        if(!ctx.hoSoPhong().doc().luuTrus().get(0).trangThai().equals("DA_TRA")) throw new AssertionError("Stale stay");
        if(!ctx.congLuuTru().yeuCauChoXuLy().isEmpty()) throw new AssertionError("Queue completion");
        if (args.length > 0) {
            var reopened = HotelContext.sql(java.nio.file.Path.of(args[0]));
            if (!reopened.hoSoPhong().doc().luuTrus().stream().anyMatch(x -> x.maDatPhong().equals("DP_SHARED") && x.trangThai().equals("DA_TRA"))) throw new AssertionError("Persisted cross-module data");
            if (!reopened.khachHang().getKhachHang(kh.maKhachHang()).hoTen().equals(kh.hoTen())) throw new AssertionError("Customer owner mismatch");
        }
        System.out.println("PASS SharedContext: one customer/room source, queue, booking, callback and checkout across modules");
    }
}
