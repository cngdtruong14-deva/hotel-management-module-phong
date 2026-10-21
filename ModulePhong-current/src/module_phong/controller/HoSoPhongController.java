package module_phong.controller;
import module_phong.model.HoSoPhong.*;
import module_phong.model.Phong;
import module_phong.service.*;
import module_khachhang.service.KhachHangService;
import module_khachhang.model.KhachHang;
import java.util.*;
import java.util.function.*;
import javax.swing.*;

/** One worker serializes local UI operations; all DAO work stays off the EDT. */
public final class HoSoPhongController {
 public record Snapshot(DuLieu data,List<Phong> rooms,List<KhachHang> customers){}
 private final HoSoPhongService service;private final PhongService rooms;private final KhachHangService customers;
 private Consumer<Snapshot> onData=p->{};private Consumer<Boolean> onBusy=p->{};private Consumer<String> onError=p->{};private Consumer<String> onSuccess=p->{};
 private boolean busy,pending;
 public boolean dangXuLy(){return busy||pending;}
 public HoSoPhongController(HoSoPhongService service,PhongService rooms,KhachHangService customers){this.service=service;this.rooms=rooms;this.customers=customers;}
 public void onData(Consumer<Snapshot> f){onData=f;}public void onBusy(Consumer<Boolean> f){onBusy=f;}public void onError(Consumer<String> f){onError=f;}public void onSuccess(Consumer<String> f){onSuccess=f;}
 public void refresh(){if(!SwingUtilities.isEventDispatchThread()){SwingUtilities.invokeLater(this::refresh);return;}if(busy){pending=true;return;}run(s->{},"");}
 public void run(Consumer<HoSoPhongService> action,String success){
  if(!SwingUtilities.isEventDispatchThread())throw new IllegalStateException("UI calls must use EDT");if(busy)return;busy=true;onBusy.accept(true);
  new SwingWorker<Snapshot,Void>(){
   protected Snapshot doInBackground(){action.accept(service);return new Snapshot(service.doc(),rooms.taiDuLieu().phongs(),customers.getKhachHangs());}
   protected void done(){busy=false;onBusy.accept(false);try{onData.accept(get());if(!success.isBlank())onSuccess.accept(success);}catch(InterruptedException e){Thread.currentThread().interrupt();onError.accept("Tác vụ bị gián đoạn.");}catch(java.util.concurrent.ExecutionException e){onError.accept(Objects.toString(e.getCause().getMessage(),"Không tải được dữ liệu."));}if(pending){pending=false;refresh();}}
  }.execute();
 }
}
