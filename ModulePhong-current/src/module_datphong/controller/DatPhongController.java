package module_datphong.controller;

import common.HotelContext;
import module_phong.model.HoSoPhong.YeuCau;
import module_datphong.model.DatPhong;
import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

/** Controller khung doc yeu cau; nhom dat phong bo sung xu ly bang luong nen. */
public final class DatPhongController {
    public record Snapshot(List<YeuCau> yeuCau, List<DatPhong> phieu) {}

    private final HotelContext context;
    private boolean busy;

    public DatPhongController(HotelContext context) {
        this.context = context;
    }

    public void taiLai(
            Consumer<Snapshot> success, Consumer<String> error, Consumer<Boolean> loading) {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Call on EDT");
        if (busy) return;
        busy = true;
        loading.accept(true);
        new SwingWorker<Snapshot, Void>() {
            protected Snapshot doInBackground() {
                return new Snapshot(
                        context.congLuuTru().yeuCauChoXuLy(),
                        context.lienKet().taiDuLieu().datPhongs());
            }

            protected void done() {
                try {
                    success.accept(get());
                } catch (Exception ex) {
                    error.accept(
                            ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage());
                } finally {
                    busy = false;
                    loading.accept(false);
                }
            }
        }.execute();
    }
}
