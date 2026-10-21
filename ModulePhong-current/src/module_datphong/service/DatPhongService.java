package module_datphong.service;
import module_datphong.model.DatPhong;
import module_lienket.service.LienKetService;
import java.util.List;
public class DatPhongService {
    private final LienKetService lienKet;
    public DatPhongService(LienKetService lienKet) { this.lienKet=lienKet; }
    public void taoDatPhong(String maDatPhong,String maKhachHang,String maPhong) { lienKet.taoDatPhong(maDatPhong,maKhachHang,maPhong); }
    public void checkIn(String maDatPhong) { lienKet.checkIn(maDatPhong); }
    public void checkOut(String maDatPhong) { lienKet.checkOut(maDatPhong); }
    public void huyDatPhong(String maDatPhong) { lienKet.huyDatPhong(maDatPhong); }
    public List<DatPhong> getDatPhongTheoPhong(String maPhong) { return lienKet.getDatPhongTheoPhong(maPhong); }
}
