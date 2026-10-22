package app;

import common.*;
import module_phong.dao.SqlServerPhongDAO;
import module_phong.service.PhongService;
import module_phong.controller.PhongController;
import module_phong.view.PhongView;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import module_phong.dao.MemoryPhongDAO;
import module_phong.dao.*;
import module_phong.service.HoSoPhongService;
import module_phong.controller.HoSoPhongController;

/** Diem chay rieng module phong de kiem tra giao dien va du lieu. */
public class ModulePhongApp {
    public static void main(String[] args) throws Exception {
        boolean sql = args.length > 0 && args[0].equals("--sql");
        PhongService service;
        HoSoPhongService hoSo;
        module_khachhang.service.KhachHangService customers;
        if (sql) {
            DBConnection db = new DBConnection(Path.of("config/db.properties"));
            common.SchemaContract.verify(db);
            customers =
                    new module_khachhang.service.KhachHangService(
                            new module_khachhang.dao.SqlServerKhachHangDAO(db));
            SqlServerPhongDAO dao = new SqlServerPhongDAO(db);
            service = new PhongService(dao);
            hoSo = new HoSoPhongService(new SqlServerHoSoPhongDAO(db));
        } else {
            MemoryPhongDAO dao = new MemoryPhongDAO();
            service = PhongService.demo(dao);
            var customerDAO = new module_khachhang.dao.MemoryKhachHangDAO();
            customers = new module_khachhang.service.KhachHangService(customerDAO);
            customers.themKhachHang(
                    new module_khachhang.model.DuLieuKhachHang(
                            "Nguyễn Văn A", null, null, null, null, null, null, null, null, null));
            hoSo = new HoSoPhongService(new MemoryHoSoPhongDAO(service, customers, id -> null));
            hoSo.luuChiTiet(
                    new module_phong.model.HoSoPhong.ChiTiet(
                            "P103", "Phòng Deluxe hướng vườn, phù hợp 2 khách. Dữ liệu demo."));
            hoSo.luuTienNghi(
                    new module_phong.model.HoSoPhong.TienNghi("P103", "TV", "Smart TV", 1, "TOT"));
            hoSo.luuTienNghi(
                    new module_phong.model.HoSoPhong.TienNghi("P103", "DH", "Điều hòa", 1, "TOT"));
        }
        SwingUtilities.invokeLater(
                () -> {
                    HotelTheme.install();
                    PhongController controller = new PhongController(service);
                    HoSoPhongController hoSoController =
                            new HoSoPhongController(hoSo, service, customers);
                    JFrame frame =
                            new JFrame(
                                    "Module phòng • "
                                            + (sql ? "SQL Server" : "DEMO — dữ liệu trong bộ nhớ"));
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    JTabbedPane tabs = new JTabbedPane();
                    var customerController =
                            new module_khachhang.controller.KhachHangController(customers);
                    customerController.onSaved(hoSoController::refresh);
                    var customerPanel =
                            new module_khachhang.view.KhachHangPanel(
                                    customerController, sql ? "SQL Server" : "DEMO");
                    tabs.addTab(
                            "Quản lý phòng",
                            new PhongView(
                                    controller,
                                    hoSoController,
                                    sql
                                            ? "SQL Server — trao đổi qua hàng đợi"
                                            : "DEMO — chưa kết nối module Lưu trú"));
                    tabs.addTab("Khách hàng", customerPanel);
                    tabs.addChangeListener(
                            e -> {
                                if (tabs.getSelectedIndex() == 0) {
                                    controller.refresh();
                                    hoSoController.refresh();
                                } else customerPanel.taiLai();
                            });
                    frame.add(tabs);
                    frame.setSize(1280, 840);
                    frame.setMinimumSize(new Dimension(1050, 740));
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                    controller.refresh();
                });
    }
}
