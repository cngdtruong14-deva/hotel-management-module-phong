package module_khachhang.controller;

import java.util.Objects;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;
import module_khachhang.model.*;
import module_khachhang.service.KhachHangService;
import module_khachhang.view.*;

/** Điều phối thao tác View và chạy Service trên worker. */
public final class KhachHangController {
    private final KhachHangService service;
    private KhachHangPanel view;
    private boolean busy, reloadPending;
    private Runnable onSaved = () -> {};
    public void onSaved(Runnable listener) { onSaved = Objects.requireNonNull(listener); }
    public KhachHangController(KhachHangService service) { this.service = Objects.requireNonNull(service); }
    public void ganView(KhachHangPanel view) {
        if (this.view != null) throw new IllegalStateException("Controller đã có View.");
        this.view = Objects.requireNonNull(view);
    }
    public void taiLai() {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::taiLai); return; }
        if (busy) { reloadPending = true; return; }
        var filter = view.boLoc();
        execute(() -> service.timKhachHang(filter), view::hienThi, view::baoLoi);
    }
    public void luu(KhachHangFormDialog form, String ma, DuLieuKhachHang data) {
        if (busy) return;
        form.dangLuu(true);
        execute(() -> service.canhBaoTrung(ma, data),
                warnings -> luuSauKiemTra(form, ma, data, warnings),
                error -> baoLoiLuu(form, error));
    }

    private void luuSauKiemTra(KhachHangFormDialog form, String ma, DuLieuKhachHang data, List<String> warnings) {
        if (!warnings.isEmpty() && !form.xacNhanTrung(warnings)) {
            form.dangLuu(false);
            return;
        }
        if (!form.isDisplayable()) return;
        execute(() -> {
            if (ma == null) service.themKhachHang(data);
            else service.suaKhachHang(ma, data);
            return true;
        }, saved -> {
            form.dispose();
            onSaved.run();
            taiLai();
        }, error -> baoLoiLuu(form, error));
    }

    private void baoLoiLuu(KhachHangFormDialog form, String message) {
        form.dangLuu(false);
        form.baoLoi(message);
    }
    public void doiTrangThai(KhachHang p) {
        if (busy || !view.xacNhanTrangThai(p)) return;
        execute(() -> {
            if (p.trangThai() == TrangThaiKhachHang.HOAT_DONG) service.voHieuHoa(p.maKhachHang());
            else service.kichHoatLai(p.maKhachHang());
            return true;
        }, saved -> { onSaved.run(); taiLai(); }, view::baoLoi);
    }
    private <T> void execute(Callable<T> work, Consumer<T> success, Consumer<String> failure) {
        busy = true;
        view.dangXuLy(true);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return work.call(); }
            @Override protected void done() {
                busy = false;
                view.dangXuLy(false);
                try { success.accept(get()); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); failure.accept("Tác vụ bị gián đoạn."); }
                catch (ExecutionException e) {
                    var cause = e.getCause();
                    failure.accept(cause.getMessage() == null ? "Không thể hoàn thành thao tác." : cause.getMessage());
                }
                if (reloadPending && !busy) { reloadPending = false; taiLai(); }
            }
        }.execute();
    }
}
