import module_phong.dao.*;
import module_phong.model.*;
import module_phong.service.*;
import module_phong.controller.*;
import module_phong.view.*;
import common.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.concurrent.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import static module_phong.model.TrangThaiPhong.*;

public class ModulePhongTest {
    static void check(boolean value) { if(!value) throw new AssertionError(); }
    static void reject(Runnable r) { try { r.run(); } catch(IllegalArgumentException e) { return; } throw new AssertionError("Expected rejection"); }
    public static void exercise(PhongService s) {
        int size=s.taiDuLieu().phongs().size();
        s.luuTang(new Tang("TESTT","Tầng kiểm thử"),false);
        s.luuLoai(new LoaiPhong("TESTL","Kiểm thử",2,new BigDecimal("700000")),false);
        Phong p=new Phong("TESTP","999","TESTL","TESTT",null,TRONG,false);
        s.luuPhong(p,false);
        check(s.getGiaHienHanh("TESTP").compareTo(new BigDecimal("700000"))==0);
        reject(() -> s.luuPhong(p,false));
        reject(() -> s.luuPhong(new Phong("TESTX","999","TESTL","TESTT",null,TRONG,false),false));
        reject(() -> s.luuPhong(new Phong("TESTX","998","NONE","TESTT",null,TRONG,false),false));
        reject(() -> s.luuLoai(new LoaiPhong("BAD","Sai",0,BigDecimal.ONE),false));
        reject(() -> s.luuPhong(new Phong("TESTP","999","TESTL","TESTT",null,DA_DAT,false),true));
        reject(() -> s.xoaTang("TESTT")); reject(() -> s.xoaLoai("TESTL"));
        s.luuPhong(new Phong("TESTP","999","TESTL","TESTT",new BigDecimal("800000"),TRONG,false),true);
        check(s.getGiaHienHanh("TESTP").compareTo(new BigDecimal("800000"))==0);
        s.batDauCongViec("VESINH","TESTP","Dọn","Nhân viên A");
        check(s.getPhong("TESTP").trangThai()==DANG_DON);
        reject(() -> s.batDauCongViec("BAOTRI","TESTP","Sửa","Nhân viên B"));
        reject(() -> s.ngungSuDung("TESTP"));
        String job=s.taiDuLieu().congViecs().stream().filter(j -> j.maPhong().equals("TESTP") && j.dangThucHien()).findFirst().orElseThrow().maCongViec();
        s.hoanTatCongViec(job); check(s.getPhong("TESTP").trangThai()==TRONG);
        String completedJob=job; reject(() -> s.hoanTatCongViec(completedJob));
        s.batDauCongViec("BAOTRI","TESTP","Điều hòa","Nhân viên B");
        check(s.getPhong("TESTP").trangThai()==BAO_TRI);
        job=s.taiDuLieu().congViecs().stream().filter(j -> j.maPhong().equals("TESTP") && j.dangThucHien()).findFirst().orElseThrow().maCongViec();
        s.hoanTatCongViec(job); s.ngungSuDung("TESTP");
        check(s.getPhong("TESTP").ngungSuDung()); check(s.getPhongTrong().stream().noneMatch(r -> r.maPhong().equals("TESTP")));
        check(s.taiDuLieu().phongs().size()==size+1);
    }
    static void layout(Component c) {
        if(c instanceof Container p) { p.doLayout(); for(Component child:p.getComponents()) layout(child); }
    }
    public static void main(String[] args) throws Exception {
        PhongService test=PhongService.demo(); exercise(test);
        PhongService service=PhongService.demo();
        PhongController[] controller=new PhongController[1]; PhongView[] view=new PhongView[1];
        CountDownLatch latch=new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            HotelTheme.install(); controller[0]=new PhongController(service); view[0]=new PhongView(controller[0]);
            controller[0].onError(message -> { throw new AssertionError(message); });
            controller[0].onChange(latch::countDown); controller[0].refresh();
        });
        check(latch.await(10,TimeUnit.SECONDS));
        SwingUtilities.invokeAndWait(() -> {
            try {
                check(controller[0].rooms().size()==12);
                view[0].setSize(1200,780); layout(view[0]);
                JTabbedPane tabs=null;
                for(Component c:view[0].getComponents()) if(c instanceof JTabbedPane t) tabs=t;
                check(tabs!=null && tabs.getTabCount()==5);
                for(int i=0;i<5;i++) {
                    tabs.setSelectedIndex(i); layout(view[0]);
                    BufferedImage image=new BufferedImage(1200,780,BufferedImage.TYPE_INT_RGB);
                    Graphics2D g=image.createGraphics(); view[0].printAll(g); g.dispose();
                    ImageIO.write(image,"png",new File(args[0]+"/preview-"+i+".png"));
                }
            } catch(Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("PASS: memory CRUD, references, prices, states, hygiene, maintenance, soft delete, async controller, 5 rendered tabs");
    }
}
