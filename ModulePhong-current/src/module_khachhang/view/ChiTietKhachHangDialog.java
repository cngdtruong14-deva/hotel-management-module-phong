package module_khachhang.view;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import module_khachhang.model.KhachHang;

public final class ChiTietKhachHangDialog extends JDialog {
    public ChiTietKhachHangDialog(Window owner, KhachHang p) {
        super(owner, "Chi tiết " + p.maKhachHang(), ModalityType.APPLICATION_MODAL);
        JTextArea text = new JTextArea(); text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true);
        text.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16)); text.setMargin(new Insets(20, 20, 20, 20));
        text.setText("Mã khách: " + p.maKhachHang() + "\nHọ tên: " + p.hoTen()
                + "\nLoại giấy tờ: " + (p.loaiGiayTo() == null ? "Chưa cung cấp" : p.loaiGiayTo().nhan())
                + "\nSố giấy tờ: " + value(p.soGiayTo()) + "\nNước cấp: " + value(p.quocGiaCap())
                + "\nĐiện thoại: " + value(p.soDienThoai()) + "\nEmail: " + value(p.email())
                + "\nNgày sinh: " + (p.ngaySinh() == null ? "Chưa cung cấp" : p.ngaySinh().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu")))
                + "\nGiới tính: " + p.gioiTinh().nhan() + "\nQuốc tịch: " + value(p.quocTich())
                + "\nĐịa chỉ: " + value(p.diaChi()) + "\nNgày tạo: " + p.ngayTao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss"))
                + "\nTrạng thái: " + p.trangThai().nhan());
        add(new JScrollPane(text), BorderLayout.CENTER);
        JButton close = new JButton("Đóng"); close.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.add(close); add(bottom, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(close);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); setSize(570, 580); setLocationRelativeTo(owner);
    }
    private static String value(String s) { return s == null ? "Chưa cung cấp" : s; }
}
