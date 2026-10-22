package module_datphong.view;

import module_datphong.controller.DatPhongController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel khung cho nhom dat phong va luu tru; hien tai chi hien thi du lieu. */
public final class DatPhongPanel extends JPanel {
    private final DatPhongController controller;
    private final JButton btnTaiLai = new JButton("Tải lại");
    private final JLabel lblTrangThai =
            new JLabel("Bộ khung — nhóm Lưu trú bổ sung form và lệnh xử lý.");
    private final DefaultTableModel mdlYeuCau =
            model("Mã yêu cầu", "Khách", "Phòng", "Ghi chú", "Trạng thái", "Phiên bản");
    private final DefaultTableModel mdlPhieu =
            model("Mã phiếu", "Khách", "Phòng", "Trạng thái", "Giá chốt");

    public DatPhongPanel(DatPhongController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(8, 8));
        var top = new JPanel(new BorderLayout());
        top.add(lblTrangThai);
        top.add(btnTaiLai, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        var tabs = new JTabbedPane();
        tabs.addTab("Yêu cầu từ Quản lý phòng", new JScrollPane(new JTable(mdlYeuCau)));
        tabs.addTab("Phiếu đặt & lưu trú", new JScrollPane(new JTable(mdlPhieu)));
        add(tabs);
        btnTaiLai.addActionListener(e -> taiLai());
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    public void taiLai() {
        controller.taiLai(
                s -> {
                    mdlYeuCau.setRowCount(0);
                    mdlPhieu.setRowCount(0);
                    s.yeuCau()
                            .forEach(
                                    p ->
                                            mdlYeuCau.addRow(
                                                    new Object[] {
                                                        p.maYeuCau(),
                                                        p.maKhachHang(),
                                                        p.maPhong(),
                                                        p.ghiChu(),
                                                        p.trangThai(),
                                                        p.phienBan()
                                                    }));
                    s.phieu()
                            .forEach(
                                    p ->
                                            mdlPhieu.addRow(
                                                    new Object[] {
                                                        p.maDatPhong(),
                                                        p.maKhachHang(),
                                                        p.maPhong(),
                                                        p.trangThai(),
                                                        p.giaPhongChot()
                                                    }));
                    lblTrangThai.setText(
                            "Đã tải. Đây là bộ khung đọc dữ liệu; chưa tự xử lý yêu cầu.");
                },
                message -> lblTrangThai.setText("Lỗi: " + message),
                busy -> btnTaiLai.setEnabled(!busy));
    }
}
