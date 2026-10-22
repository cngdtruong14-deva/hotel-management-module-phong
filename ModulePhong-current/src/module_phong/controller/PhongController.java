package module_phong.controller;

import module_phong.model.*;
import module_phong.service.PhongService;
import javax.swing.*;
import java.util.*;
import java.util.function.Consumer;

/** Giao dien doc du lieu da tai; service va DAO chay tren luong nen. */
public class PhongController {
    private final PhongService service;
    private DuLieuPhong data = DuLieuPhong.rong();
    private final List<Runnable> listeners = new ArrayList<>();
    private Consumer<String> errors = message -> {};
    private Consumer<Boolean> busyListener = value -> {};
    private boolean busy;
    private boolean refreshQueued;

    public PhongController(PhongService service) {
        this.service = service;
    }

    public void onError(Consumer<String> fn) {
        errors = fn;
    }

    public void onBusy(Consumer<Boolean> fn) {
        busyListener = fn;
    }

    public void onChange(Runnable fn) {
        listeners.add(fn);
    }

    public List<Phong> rooms() {
        return data.phongs().stream().filter(p -> !p.ngungSuDung()).toList();
    }

    public List<LoaiPhong> types() {
        return data.loais();
    }

    public List<Tang> floors() {
        return data.tangs();
    }

    public List<CongViecPhong> jobs() {
        return data.congViecs();
    }

    public Phong room(String ma) {
        return data.phongs().stream().filter(p -> p.maPhong().equals(ma)).findFirst().orElse(null);
    }

    public LoaiPhong type(String ma) {
        return types().stream().filter(p -> p.maLoaiPhong().equals(ma)).findFirst().orElse(null);
    }

    public Tang floor(String ma) {
        return floors().stream().filter(p -> p.maTang().equals(ma)).findFirst().orElse(null);
    }

    public boolean isBusy() {
        return busy;
    }

    public void refresh() {
        if (busy) {
            refreshQueued = true;
            return;
        }
        run(() -> {}, () -> {});
    }

    private void run(Runnable action, Runnable success) {
        if (!SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException("Controller must be called on EDT");
        if (busy) {
            errors.accept("Đang xử lý, vui lòng chờ.");
            return;
        }
        busy = true;
        busyListener.accept(true);
        new SwingWorker<DuLieuPhong, Void>() {
            protected DuLieuPhong doInBackground() {
                action.run();
                return service.taiDuLieu();
            }

            protected void done() {
                try {
                    data = get();
                    listeners.forEach(Runnable::run);
                    success.run();
                } catch (Exception e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    errors.accept(cause.getMessage());
                } finally {
                    busy = false;
                    busyListener.accept(false);
                    if (refreshQueued) {
                        refreshQueued = false;
                        refresh();
                    }
                }
            }
        }.execute();
    }

    public void saveRoom(Phong p, boolean sua, Runnable ok) {
        run(() -> service.luuPhong(p, sua), ok);
    }

    public void saveType(LoaiPhong p, boolean sua, Runnable ok) {
        run(() -> service.luuLoai(p, sua), ok);
    }

    public void saveFloor(Tang p, boolean sua, Runnable ok) {
        run(() -> service.luuTang(p, sua), ok);
    }

    public void deleteRoom(String ma, Runnable ok) {
        run(() -> service.ngungSuDung(ma), ok);
    }

    public void deleteType(String ma, Runnable ok) {
        run(() -> service.xoaLoai(ma), ok);
    }

    public void deleteFloor(String ma, Runnable ok) {
        run(() -> service.xoaTang(ma), ok);
    }

    public void startJob(String loai, String ma, String nd, String nguoi, Runnable ok) {
        run(() -> service.batDauCongViec(loai, ma, nd, nguoi), ok);
    }

    public void completeJob(String ma) {
        run(() -> service.hoanTatCongViec(ma), () -> {});
    }
}
