package module_khachhang.view;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import module_khachhang.controller.KhachHangController;
import module_khachhang.model.*;

/** Panel tích hợp MainFrame: chỉ hiển thị, nhận thao tác và gọi Controller. */
public final class KhachHangPanel extends JPanel {
    private final KhachHangController controller;
    private final JTextField txtTimKiem = new JTextField(22), txtQuocTich = new JTextField(3);
    private final JComboBox<Object> cboGioiTinh = new JComboBox<>(), cboTrangThai = new JComboBox<>();
    private static final class KhachHangTableModel extends AbstractTableModel {
        private final String[] columns = {"Mã khách", "Họ tên", "Giấy tờ", "Điện thoại", "Email",
                "Giới tính", "Quốc tịch", "Trạng thái"};
        private Object[][] data = new Object[0][];
        @Override public int getRowCount() { return data.length; }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Object getValueAt(int row, int column) { return data[row][column]; }
        @Override public boolean isCellEditable(int row, int column) { return false; }
        void thayDuLieu(Object[][] values) {
            // Một sự kiện cho toàn bộ snapshot, tránh sorter đọc bảng đang thêm từng dòng.
            data = values;
            fireTableDataChanged();
        }
    }
    private final KhachHangTableModel model = new KhachHangTableModel();
    private final JTable tblKhachHang = new JTable(model);
    private final JLabel lblThongBao = new JLabel("Đang tải dữ liệu…");
    private final JButton btnThem = new JButton("Thêm khách"), btnSua = new JButton("Sửa"),
            btnChiTiet = new JButton("Xem chi tiết"), btnTrangThai = new JButton("Vô hiệu hóa"),
            btnTaiLai = new JButton("Tải lại"), btnBoLoc = new JButton("Bỏ lọc");
    private final Timer henTimKiem;
    private List<KhachHang> danhSachKhach = List.of();
    private boolean busy;

    public KhachHangPanel(KhachHangController controller, String mode) {
        super(new BorderLayout(12, 16));
        this.controller = controller;
        controller.ganView(this);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setBackground(new Color(245, 247, 250));
        henTimKiem = new Timer(350, e -> taiLai());
        henTimKiem.setRepeats(false);
        taoGiaoDien(mode);
        ganSuKien();
        capNhatNut();
        SwingUtilities.invokeLater(this::taiLai);
    }
    private void taoGiaoDien(String mode) {
        JLabel title = new JLabel("Quản lý khách hàng");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(title, BorderLayout.NORTH);
        heading.add(new JLabel(mode), BorderLayout.SOUTH);
        JPanel filters = new JPanel(new GridLayout(2, 1, 0, 4));
        filters.setOpaque(false);
        JPanel query = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        query.setOpaque(false);
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        options.setOpaque(false);
        txtTimKiem.setToolTipText("Tìm theo mã, họ tên, giấy tờ hoặc điện thoại");
        label(query, "Tìm kiếm", txtTimKiem);
        query.add(btnBoLoc);
        cboGioiTinh.addItem("Tất cả giới tính");
        for (var g : GioiTinh.values()) cboGioiTinh.addItem(g);
        cboTrangThai.addItem("Tất cả trạng thái");
        for (var s : TrangThaiKhachHang.values()) cboTrangThai.addItem(s);
        cboGioiTinh.setRenderer(new NhanEnumRenderer());
        cboTrangThai.setRenderer(new NhanEnumRenderer());
        label(options, "Giới tính", cboGioiTinh);
        label(options, "Trạng thái", cboTrangThai);
        txtQuocTich.setToolTipText("Mã quốc gia: VN, US, GB…; để trống để xem tất cả");
        label(options, "Quốc tịch", txtQuocTich);
        filters.add(query);
        filters.add(options);
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(filters, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        for (JButton b : new JButton[]{btnThem, btnSua, btnChiTiet, btnTrangThai, btnTaiLai}) actions.add(b);
        btnThem.setMnemonic(KeyEvent.VK_N);
        btnTaiLai.setMnemonic(KeyEvent.VK_R);
        top.add(actions, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        tblKhachHang.setRowHeight(32);
        tblKhachHang.setAutoCreateRowSorter(true);
        tblKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblKhachHang.setFillsViewportHeight(true);
        tblKhachHang.getTableHeader().setReorderingAllowed(false);
        tblKhachHang.getAccessibleContext().setAccessibleName("Danh sách khách hàng");
        JScrollPane tableScroll = new JScrollPane(tblKhachHang);
        tableScroll.setColumnHeaderView(tblKhachHang.getTableHeader());
        add(tableScroll, BorderLayout.CENTER);
        add(lblThongBao, BorderLayout.SOUTH);
    }

    private void ganSuKien() {
        DocumentListener change = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { henTimKiem.restart(); }
            public void removeUpdate(DocumentEvent e) { henTimKiem.restart(); }
            public void changedUpdate(DocumentEvent e) { henTimKiem.restart(); }
        };
        txtTimKiem.getDocument().addDocumentListener(change);
        txtQuocTich.getDocument().addDocumentListener(change);
        cboGioiTinh.addActionListener(e -> taiLai());
        cboTrangThai.addActionListener(e -> taiLai());
        btnTaiLai.addActionListener(e -> taiLai());
        btnBoLoc.addActionListener(e -> {
            txtTimKiem.setText("");
            txtQuocTich.setText("");
            cboGioiTinh.setSelectedIndex(0);
            cboTrangThai.setSelectedIndex(0);
            taiLai();
        });
        tblKhachHang.getSelectionModel().addListSelectionListener(e -> capNhatNut());
        btnThem.addActionListener(e -> moForm(null));
        btnSua.addActionListener(e -> { var p = getKhachDangChon(); if (p != null) moForm(p); });
        btnChiTiet.addActionListener(e -> xemChiTiet());
        btnTrangThai.addActionListener(e -> { var p = getKhachDangChon(); if (p != null) controller.doiTrangThai(p); });
        tblKhachHang.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2 && !busy) xemChiTiet(); }
        });
        tblKhachHang.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "chiTiet");
        tblKhachHang.getActionMap().put("chiTiet", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (!busy) xemChiTiet(); }
        });
    }

    private static void label(JPanel target, String text, JComponent component) {
        JLabel label = new JLabel(text);
        label.setLabelFor(component);
        target.add(label);
        target.add(component);
    }
    private KhachHang getKhachDangChon() {
        int i = tblKhachHang.getSelectedRow();
        return i < 0 ? null : danhSachKhach.get(tblKhachHang.convertRowIndexToModel(i));
    }
    private void moForm(KhachHang p) {
        new KhachHangFormDialog(SwingUtilities.getWindowAncestor(this), controller, p).setVisible(true);
    }
    private void xemChiTiet() {
        var p = getKhachDangChon();
        if (p != null) new ChiTietKhachHangDialog(SwingUtilities.getWindowAncestor(this), p).setVisible(true);
    }
    public BoLocKhachHang boLoc() {
        return new BoLocKhachHang(txtTimKiem.getText(), cboGioiTinh.getSelectedItem() instanceof GioiTinh g ? g : null,
                txtQuocTich.getText(), cboTrangThai.getSelectedItem() instanceof TrangThaiKhachHang s ? s : null);
    }
    /** Điểm tích hợp: gọi sau khi dữ liệu thay đổi từ module khác. */
    public void taiLai() { controller.taiLai(); }
    public void hienThi(List<KhachHang> list) {
        KhachHang khachDangChon = getKhachDangChon();
        String selectedId = khachDangChon == null ? null : khachDangChon.maKhachHang();
        tblKhachHang.clearSelection();
        danhSachKhach = list;
        Object[][] values = danhSachKhach.stream().map(p -> new Object[]{p.maKhachHang(), p.hoTen(), p.soGiayTo(),
                p.soDienThoai(), p.email(), p.gioiTinh().nhan(), p.quocTich(), p.trangThai().nhan()}).toArray(Object[][]::new);
        model.thayDuLieu(values);
        for (int i = 0; i < danhSachKhach.size(); i++) if (danhSachKhach.get(i).maKhachHang().equals(selectedId)) {
            int index = tblKhachHang.convertRowIndexToView(i);
            tblKhachHang.setRowSelectionInterval(index, index);
            break;
        }
        capNhatSoLuong();
        capNhatNut();
    }
    public void dangXuLy(boolean value) {
        busy = value;
        for (var c : new JComponent[]{txtTimKiem, txtQuocTich, cboGioiTinh, cboTrangThai, btnBoLoc, btnTaiLai, btnThem}) c.setEnabled(!busy);
        tblKhachHang.setEnabled(!busy);
        capNhatNut();
        if (busy) lblThongBao.setText("Đang xử lý…");
        else capNhatSoLuong();
    }
    private void capNhatSoLuong() {
        lblThongBao.setText(danhSachKhach.isEmpty() ? "Không tìm thấy khách hàng. Thử bỏ lọc hoặc thêm khách mới."
                : danhSachKhach.size() + " khách hàng trong kết quả tìm kiếm");
    }

    private void capNhatNut() {
        var p = getKhachDangChon();
        boolean enabled = !busy && p != null;
        btnSua.setEnabled(enabled);
        btnChiTiet.setEnabled(enabled);
        btnTrangThai.setEnabled(enabled);
        btnTrangThai.setText(p != null && p.trangThai() == TrangThaiKhachHang.NGUNG_HOAT_DONG ? "Kích hoạt lại" : "Vô hiệu hóa");
    }
    public boolean xacNhanTrangThai(KhachHang p) {
        String action = p.trangThai() == TrangThaiKhachHang.HOAT_DONG ? "Vô hiệu hóa" : "Kích hoạt lại";
        return JOptionPane.showConfirmDialog(this, action + " khách " + p.hoTen() + "?\nHồ sơ và lịch sử vẫn được giữ.",
                action, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    public void baoLoi(String message) {
        lblThongBao.setText("Không hoàn thành thao tác. Có thể thử Tải lại.");
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.ERROR_MESSAGE);
    }
}
