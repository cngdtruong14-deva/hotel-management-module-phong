package common;

import module_phong.dao.*;
import module_phong.service.*;
import module_khachhang.dao.*;
import module_khachhang.service.KhachHangService;
import module_lienket.dao.*;
import module_lienket.service.LienKetService;
import module_datphong.service.DatPhongService;
import java.nio.file.Path;

/** Khoi tao cac service dung chung nguon du lieu; moi giao dich mo ket noi rieng. */
public record HotelContext(
        PhongService phong,
        KhachHangService khachHang,
        DatPhongService datPhong,
        HoSoPhongService hoSoPhong,
        CongLuuTru congLuuTru,
        LienKetService lienKet,
        String mode) {
    public static HotelContext sql(Path config) throws Exception {
        var db = new DBConnection(config);
        SchemaContract.verify(db);
        var roomDAO = new SqlServerPhongDAO(db);
        var rooms = new PhongService(roomDAO);
        var customers = new KhachHangService(new SqlServerKhachHangDAO(db));
        var links = new LienKetService(new SqlServerLienKetDAO(db, roomDAO), rooms);
        return assemble(
                rooms,
                customers,
                links,
                new SqlServerHoSoPhongDAO(db),
                "SQL Server — database dùng chung");
    }

    public static HotelContext demo() {
        var roomDAO = new MemoryPhongDAO();
        var rooms = PhongService.demo(roomDAO);
        var customerDAO = new MemoryKhachHangDAO();
        var customers = new KhachHangService(customerDAO);
        var links = new LienKetService(new MemoryLienKetDAO(roomDAO, customerDAO), rooms);
        var portalDAO =
                new MemoryHoSoPhongDAO(
                        rooms,
                        customers,
                        id ->
                                links.taiDuLieu().datPhongs().stream()
                                        .filter(p -> p.maDatPhong().equals(id))
                                        .findFirst()
                                        .orElse(null));
        return assemble(rooms, customers, links, portalDAO, "DEMO — cùng bộ nhớ, mất khi thoát");
    }

    private static HotelContext assemble(
            PhongService rooms,
            KhachHangService customers,
            LienKetService links,
            HoSoPhongDAO portalDAO,
            String mode) {
        return new HotelContext(
                rooms,
                customers,
                new DatPhongService(links),
                new HoSoPhongService(portalDAO),
                new TiepNhanLuuTruService(portalDAO),
                links,
                mode);
    }
}
