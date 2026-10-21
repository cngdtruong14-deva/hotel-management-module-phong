package module_khachhang.view;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.*;
import java.util.List;
import javax.swing.*;
import module_khachhang.controller.KhachHangController;
import module_khachhang.model.*;

public final class KhachHangFormDialog extends JDialog {
    private final JTextField txtHoTen = new JTextField(25), txtSoGiayTo = new JTextField(), txtQuocGiaCap = new JTextField(),
            txtDienThoai = new JTextField(), txtEmail = new JTextField(), txtNgaySinh = new JTextField(),
            txtQuocTich = new JTextField(), txtDiaChi = new JTextField();
    private final JComboBox<Object> cboLoaiGiayTo = new JComboBox<>();
    private final JComboBox<GioiTinh> cboGioiTinh = new JComboBox<>(GioiTinh.values());
    private final JButton btnLuu = new JButton("Lưu hồ sơ"), btnHuy = new JButton("Hủy");
    private final JLabel lblThongBao = new JLabel(" ");
    private final JPanel pnlThongTin = new JPanel(new GridBagLayout());
    private int hang;

    public KhachHangFormDialog(Window owner, KhachHangController controller, KhachHang p) {
        super(owner, p == null ? "Thêm khách hàng" : "Sửa " + p.maKhachHang(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        taoGiaoDien();
        hienThi(p);
        cboLoaiGiayTo.addActionListener(e -> { if (cboLoaiGiayTo.getSelectedItem() == LoaiGiayTo.CCCD && txtQuocGiaCap.getText().isBlank()) txtQuocGiaCap.setText("VN"); });
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> {
            try {
                DuLieuKhachHang data = docForm();
                controller.luu(this, p == null ? null : p.maKhachHang(), data);
            } catch (DateTimeParseException ex) { baoLoi("Ngày sinh phải đúng dd/MM/yyyy, ví dụ 25/12/2000."); }
        });
        setSize(610, 620);
        setMinimumSize(new Dimension(480, 420));
        setLocationRelativeTo(owner);
        SwingUtilities.invokeLater(txtHoTen::requestFocusInWindow);
    }
    private void taoGiaoDien() {
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        cboLoaiGiayTo.addItem("Chưa cung cấp");
        for (var t : LoaiGiayTo.values()) cboLoaiGiayTo.addItem(t);
        cboLoaiGiayTo.setRenderer(new NhanEnumRenderer());
        cboGioiTinh.setRenderer(new NhanEnumRenderer());
        cboGioiTinh.setSelectedItem(GioiTinh.KHONG_CUNG_CAP);
        themTruong("Họ tên *", txtHoTen);
        themTruong("Loại giấy tờ", cboLoaiGiayTo);
        themTruong("Số giấy tờ", txtSoGiayTo);
        themTruong("Nước cấp (VN/US…)", txtQuocGiaCap);
        themTruong("Điện thoại", txtDienThoai);
        themTruong("Email", txtEmail);
        themTruong("Ngày sinh (dd/MM/yyyy)", txtNgaySinh);
        themTruong("Giới tính", cboGioiTinh);
        themTruong("Quốc tịch (VN/US…)", txtQuocTich);
        themTruong("Địa chỉ", txtDiaChi);
        body.add(new JScrollPane(pnlThongTin), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        lblThongBao.setForeground(new Color(160, 30, 30));
        bottom.add(lblThongBao, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnHuy);
        buttons.add(btnLuu);
        bottom.add(buttons, BorderLayout.SOUTH);
        body.add(bottom, BorderLayout.SOUTH);
        setContentPane(body);
        getRootPane().setDefaultButton(btnLuu);
        getRootPane().registerKeyboardAction(e -> { if (btnHuy.isEnabled()) dispose(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void hienThi(KhachHang p) {
        if (p != null) {
            txtHoTen.setText(p.hoTen());
            cboLoaiGiayTo.setSelectedItem(p.loaiGiayTo() == null ? "Chưa cung cấp" : p.loaiGiayTo());
            txtSoGiayTo.setText(p.soGiayTo());
            txtQuocGiaCap.setText(p.quocGiaCap());
            txtDienThoai.setText(p.soDienThoai());
            txtEmail.setText(p.email());
            txtQuocTich.setText(p.quocTich());
            txtDiaChi.setText(p.diaChi());
            cboGioiTinh.setSelectedItem(p.gioiTinh());
            if (p.ngaySinh() != null) txtNgaySinh.setText(p.ngaySinh().format(DateTimeFormatter.ofPattern("dd/MM/uuuu")));
        }
    }

    private DuLieuKhachHang docForm() {
        LocalDate date = txtNgaySinh.getText().isBlank() ? null : LocalDate.parse(txtNgaySinh.getText().trim(),
                DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
        return new DuLieuKhachHang(txtHoTen.getText(), cboLoaiGiayTo.getSelectedItem() instanceof LoaiGiayTo t ? t : null,
                txtSoGiayTo.getText(), txtQuocGiaCap.getText(), txtDienThoai.getText(), txtEmail.getText(), date,
                (GioiTinh) cboGioiTinh.getSelectedItem(), txtQuocTich.getText(), txtDiaChi.getText());
    }

    private void themTruong(String text, JComponent component) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = hang++;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(7, 4, 7, 12);
        JLabel label = new JLabel(text);
        label.setLabelFor(component);
        pnlThongTin.add(label, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        pnlThongTin.add(component, c);
    }
    public void dangLuu(boolean busy) {
        btnLuu.setEnabled(!busy);
        btnHuy.setEnabled(!busy);
        for (Component c : pnlThongTin.getComponents()) if (c instanceof JComponent) c.setEnabled(!busy);
        setDefaultCloseOperation(busy ? DO_NOTHING_ON_CLOSE : DISPOSE_ON_CLOSE);
        lblThongBao.setText(busy ? "Đang kiểm tra và lưu…" : " ");
    }
    public void baoLoi(String message) {
        lblThongBao.setText("Chưa lưu được hồ sơ.");
        JOptionPane.showMessageDialog(this, message, "Kiểm tra dữ liệu", JOptionPane.ERROR_MESSAGE);
    }
    public boolean xacNhanTrung(List<String> warnings) {
        return JOptionPane.showConfirmDialog(this, "Có hồ sơ trùng điện thoại hoặc họ tên/ngày sinh:\n"
                + String.join("\n", warnings.stream().limit(8).toList())
                + "\nBạn đã kiểm tra và vẫn muốn lưu?", "Kiểm tra hồ sơ trùng",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
