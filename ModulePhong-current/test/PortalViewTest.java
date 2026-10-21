import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.concurrent.*;
import module_khachhang.dao.*;
import module_khachhang.service.*;
import module_khachhang.model.*;
import module_khachhang.controller.*;
import module_khachhang.view.*;
import module_phong.dao.*;
import module_phong.service.*;
import module_phong.controller.*;
import module_phong.view.*;
import module_phong.model.HoSoPhong.*;
import module_lienket.dao.*;
import module_lienket.service.*;

public class PortalViewTest {
 static void layout(Component c){if(c instanceof Container p){p.doLayout();for(var child:p.getComponents())layout(child);}}
 static JTabbedPane find(Component c,String title){if(c instanceof JTabbedPane t)for(int i=0;i<t.getTabCount();i++)if(t.getTitleAt(i).equals(title))return t;if(c instanceof Container p)for(Component ch:p.getComponents()){var result=find(ch,title);if(result!=null)return result;}return null;}
 static void assertNoBooking(Component c){if(c instanceof AbstractButton b){String text=b.getText();if(text!=null&&java.util.Set.of("Giữ phòng","Check-in dòng đã chọn","Check-out dòng đã chọn","Hủy giữ dòng đã chọn","Ghi nhận dịch vụ").contains(text))throw new AssertionError("Booking action leaked into room UI");}if(c instanceof Container p)for(var child:p.getComponents())assertNoBooking(child);}
 public static void main(String[] args)throws Exception {
  var pd=new MemoryPhongDAO();var ps=PhongService.demo(pd);var cd=new MemoryKhachHangDAO();var cs=new KhachHangService(cd);
  var customer=cs.themKhachHang(new DuLieuKhachHang("Nguyễn Minh Anh",null,null,null,null,null,null,null,null,null));
  // Test fixture only: mimic the external booking module to demonstrate inbound messages.
  var ls=new LienKetService(new MemoryLienKetDAO(pd,cd),ps);
  ls.taoDatPhong("DEMO_REVIEW",customer.maKhachHang(),"P103");ls.checkIn("DEMO_REVIEW");ls.checkOut("DEMO_REVIEW");
  ps.batDauCongViec("VESINH","P103","Dọn sau trả","Demo");
  var job=ps.taiDuLieu().congViecs().stream().filter(x->x.maPhong().equals("P103")&&x.dangThucHien()).findFirst().orElseThrow();ps.hoanTatCongViec(job.maCongViec());
  ls.taoDatPhong("DEMO_STAY",customer.maKhachHang(),"P103");ls.checkIn("DEMO_STAY");
  var dao=new MemoryHoSoPhongDAO(ps,cs,id->ls.taiDuLieu().datPhongs().stream().filter(x->x.maDatPhong().equals(id)).findFirst().orElse(null));
  var svc=new HoSoPhongService(dao);var inbound=new TiepNhanLuuTruService(dao);var time=java.time.LocalDateTime.now();
  svc.luuChiTiet(new ChiTiet("P103","Phòng Deluxe hướng vườn. Cửa sổ rộng, bàn làm việc và phòng tắm riêng."));
  svc.luuTienNghi(new TienNghi("P103","TV","Smart TV 43 inch",1,"TOT"));svc.luuTienNghi(new TienNghi("P103","DH","Điều hòa",1,"CAN_SUA"));
  svc.guiYeuCau("YC_DEMO",customer.maKhachHang(),"P103","Khách muốn tìm phòng tương tự, ưu tiên yên tĩnh.");
  inbound.nhanDanhGia(new DanhGia("DG_DEMO","DEMO_REVIEW","P103",customer.maKhachHang(),4,"Phòng sạch, ánh sáng tốt; mong cách âm tốt hơn.",time));
  inbound.nhanTrangThai(new LuuTru("DEMO_STAY","P103",customer.maKhachHang(),"DANG_O",1,time));
  inbound.nhanPhanAnh(new PhanAnh("PA_DEMO","DEMO_STAY","P103",customer.maKhachHang(),"Điều hòa phát tiếng ồn, đề nghị kiểm tra.","DANG_XU_LY",1,time));
  var latch=new CountDownLatch(1);JTabbedPane[] ui=new JTabbedPane[1];HoSoPhongController[] ctl=new HoSoPhongController[1];
  SwingUtilities.invokeAndWait(()->{
   common.HotelTheme.install();var pc=new PhongController(ps);ctl[0]=new HoSoPhongController(svc,ps,cs);
   ui[0]=new JTabbedPane();ui[0].addTab("Quản lý phòng",new PhongView(pc,ctl[0],"DEMO KIỂM THỬ — phản hồi mô phỏng module Lưu trú"));
   ui[0].addTab("Khách hàng",new KhachHangPanel(new KhachHangController(cs),"DEMO"));
   ctl[0].onSuccess(m->latch.countDown());ctl[0].onError(m->{throw new AssertionError(m);});
   ctl[0].run(s->{},"Loaded");pc.refresh();
  });
  if(!latch.await(10,TimeUnit.SECONDS))throw new AssertionError("Load timed out");
  SwingUtilities.invokeAndWait(()->{find(ui[0],"Thông tin & phản hồi").setSelectedIndex(5);selectRoom(ui[0]);});
  boolean[] busy={true};for(int i=0;i<100&&busy[0];i++){Thread.sleep(50);SwingUtilities.invokeAndWait(()->busy[0]=ctl[0].dangXuLy());}
  if(busy[0])throw new AssertionError("Refresh did not settle");
  SwingUtilities.invokeAndWait(()->{try{
   ui[0].setSize(1400,940);
   selectRoom(ui[0]);assertNoBooking(ui[0]);var tabs=find(ui[0],"Thông tin & tiện nghi");if(tabs==null||tabs.getTabCount()!=4)throw new AssertionError();
   for(int i=0;i<4;i++){tabs.setSelectedIndex(i);layout(ui[0]);var img=new BufferedImage(1400,940,BufferedImage.TYPE_INT_RGB);var g=img.createGraphics();ui[0].printAll(g);g.dispose();ImageIO.write(img,"png",new File("verification/portal-"+i+".png"));}
  }catch(Exception e){throw new RuntimeException(e);}});
  System.out.println("PASS UI Portal: 4 new tabs rendered, no booking/check-in/out/usage action in room workspace");
 }
 static void selectRoom(Component c){if(c instanceof JComboBox<?> box){for(int i=0;i<box.getItemCount();i++)if("P103".equals(box.getItemAt(i))){box.setSelectedIndex(i);break;}}if(c instanceof Container p)for(var child:p.getComponents())selectRoom(child);}
}
