package app;

import common.*;
import module_phong.controller.*;
import module_phong.view.PhongView;
import module_khachhang.controller.KhachHangController;
import module_khachhang.view.KhachHangPanel;
import module_datphong.controller.DatPhongController;
import module_datphong.view.DatPhongPanel;
import javax.swing.*;
import java.nio.file.Path;

/** Ung dung ghep ba panel, cung dung service tu mot HotelContext. */
public final class HotelSharedApp {
    public static void main(String[] args) throws Exception {
        var ctx =
                args.length > 0 && args[0].equals("--sql")
                        ? HotelContext.sql(
                                Path.of(args.length > 1 ? args[1] : "config/db.properties"))
                        : HotelContext.demo();
        SwingUtilities.invokeLater(
                () -> {
                    HotelTheme.install();
                    var rooms = new PhongController(ctx.phong());
                    var portal =
                            new HoSoPhongController(ctx.hoSoPhong(), ctx.phong(), ctx.khachHang());
                    var customers = new KhachHangController(ctx.khachHang());
                    customers.onSaved(portal::refresh);
                    var customerPanel = new KhachHangPanel(customers, ctx.mode());
                    var bookingPanel = new DatPhongPanel(new DatPhongController(ctx));
                    var tabs = new JTabbedPane();
                    tabs.addTab("Quản lý phòng", new PhongView(rooms, portal, ctx.mode()));
                    tabs.addTab("Khách hàng", customerPanel);
                    tabs.addTab("Đặt phòng & Lưu trú — bộ khung", bookingPanel);
                    tabs.addChangeListener(
                            e -> {
                                if (tabs.getSelectedIndex() == 0) {
                                    rooms.refresh();
                                    portal.refresh();
                                } else if (tabs.getSelectedIndex() == 1) customerPanel.taiLai();
                                else bookingPanel.taiLai();
                            });
                    var frame = new JFrame("Khung chung Hotel Management — " + ctx.mode());
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.add(tabs);
                    frame.setSize(1280, 840);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                    rooms.refresh();
                });
    }
}
