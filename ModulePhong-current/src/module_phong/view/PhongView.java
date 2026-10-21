package module_phong.view;
import common.HotelTheme;
import module_phong.model.*;
import module_phong.controller.PhongController;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Locale;
import static module_phong.model.TrangThaiPhong.*;

/** Embeddable management module. Construct and mutate on the Swing EDT. */
public class PhongView extends JPanel {
    private final PhongController controller;
    private final CongViecView congViecView;
    private final JLabel lblTongQuan = new JLabel();
    private final JLabel lblThongBao = new JLabel("Sẵn sàng");
    private final Editor pnlPhong, pnlLoaiPhong, pnlTang;
    private final JPanel pnlSoDoPhong = new JPanel(new GridLayout(0, 4, 12, 12));
    private final JComboBox<Object> cboLocTang = new JComboBox<>();
    private final JComboBox<Object> cboLocTrangThai = new JComboBox<>();
    private final JTabbedPane tabPhong = new JTabbedPane();
    public PhongView(PhongController controller, module_phong.controller.HoSoPhongController hoSoController, String mode) {
        this(controller);
        tabPhong.addTab("Thông tin & phản hồi",new HoSoPhongView(hoSoController,mode));
        tabPhong.addChangeListener(e -> { if(tabPhong.getSelectedIndex()==5) hoSoController.refresh(); });
    }
    public PhongView(PhongController controller) {
        this.controller = controller;
        this.congViecView = new CongViecView(controller);
        setLayout(new BorderLayout(0, 18));
        setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        JPanel heading = new JPanel(new BorderLayout(0, 8));
        JLabel title = new JLabel("Quản lý phòng");
        title.setFont(new Font("SansSerif", Font.BOLD, 26)); title.setForeground(HotelTheme.INK);
        heading.add(title, BorderLayout.NORTH); heading.add(lblTongQuan, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnTaiLai = new JButton("Tải lại dữ liệu");
        btnTaiLai.addActionListener(e -> controller.refresh()); actions.add(btnTaiLai); actions.add(lblThongBao);
        heading.add(actions, BorderLayout.SOUTH);
        add(heading, BorderLayout.NORTH);
        pnlPhong = new Editor(0, new String[]{"Mã phòng", "Số phòng", "Loại phòng", "Tầng", "Trạng thái", "Giá / đêm (VND)"});
        pnlLoaiPhong = new Editor(1, new String[]{"Mã loại", "Tên loại phòng", "Sức chứa", "Giá / đêm (VND)"});
        pnlTang = new Editor(2, new String[]{"Mã tầng", "Tên tầng", "Số phòng"});
        tabPhong.addTab("Phòng", pnlPhong); tabPhong.addTab("Loại phòng", pnlLoaiPhong);
        tabPhong.addTab("Tầng", pnlTang); tabPhong.addTab("Sơ đồ phòng", statusPanel());
        tabPhong.addTab("Vệ sinh và bảo trì", congViecView);
        add(tabPhong, BorderLayout.CENTER);
        controller.onChange(this::refresh);
        controller.onChange(congViecView::refresh);
        controller.onError(message -> JOptionPane.showMessageDialog(this, message, "Kiểm tra thông tin", JOptionPane.WARNING_MESSAGE));
        controller.onBusy(value -> { lblThongBao.setText(value ? "Đang xử lý…" : "Sẵn sàng"); setCursor(Cursor.getPredefinedCursor(value ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR)); });
        refresh();
    }
    public void taiLai() { controller.refresh(); }
    private JPanel statusPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cboLocTrangThai.addItem("Tất cả trạng thái");
        for (TrangThaiPhong s : TrangThaiPhong.values()) cboLocTrangThai.addItem(s);
        cboLocTang.setPreferredSize(new Dimension(170, 30));
        cboLocTrangThai.setPreferredSize(new Dimension(190, 30));
        filters.add(new JLabel("Tầng:")); filters.add(cboLocTang);
        filters.add(new JLabel("Trạng thái:")); filters.add(cboLocTrangThai);
        filters.add(new JLabel("Chọn thẻ phòng để xem thông tin"));
        cboLocTang.addActionListener(e -> refreshBoard());
        cboLocTrangThai.addActionListener(e -> refreshBoard());
        panel.add(filters, BorderLayout.NORTH);
        JPanel container = new JPanel(new BorderLayout()); container.add(pnlSoDoPhong, BorderLayout.NORTH);
        panel.add(new JScrollPane(container), BorderLayout.CENTER); return panel;
    }
    private void refresh() {
        long empty = controller.rooms().stream().filter(r -> r.trangThai() == TrangThaiPhong.TRONG).count();
        long occupied = controller.rooms().stream().filter(r -> r.trangThai() == TrangThaiPhong.DANG_SU_DUNG).count();
        lblTongQuan.setText(controller.rooms().size() + " phòng     •     " + empty + " phòng trống     •     " +
            occupied + " đang ở     •     " + controller.floors().size() + " tầng");
        pnlPhong.load(); pnlLoaiPhong.load(); pnlTang.load();
        Object selected = cboLocTang.getSelectedItem();
        cboLocTang.removeAllItems(); cboLocTang.addItem("Tất cả tầng");
        for (Tang f : controller.floors()) cboLocTang.addItem(f);
        if (selected instanceof Tang f && controller.floor(f.maTang()) != null)
            cboLocTang.setSelectedItem(controller.floor(f.maTang()));
        refreshBoard();
    }
    private void refreshBoard() {
        pnlSoDoPhong.removeAll();
        for (Phong r : controller.rooms()) {
            if (cboLocTang.getSelectedItem() instanceof Tang f && !f.maTang().equals(r.maTang())) continue;
            if (cboLocTrangThai.getSelectedItem() instanceof TrangThaiPhong s && s != r.trangThai()) continue;
            JButton card = new JButton("<html><b>Phòng " + html(r.soPhong()) + "</b><br>" +
                html(controller.type(r.maLoaiPhong()).tenLoai()) + " · " + html(controller.floor(r.maTang()).tenTang()) +
                "<br><br>" + r.trangThai().toString() + "</html>");
            Color color = switch (r.trangThai()) {
                case TRONG -> new Color(33, 139, 111);
                case DA_DAT -> new Color(117, 86, 179);
                case DANG_SU_DUNG -> new Color(56, 111, 175);
                case DANG_DON -> new Color(185, 132, 39);
                case BAO_TRI -> new Color(164, 76, 78);
            };
            card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
            card.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            card.setBackground(Color.WHITE); card.setForeground(HotelTheme.INK);
            card.setFocusPainted(false); card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.setPreferredSize(new Dimension(205, 135)); card.setHorizontalAlignment(SwingConstants.LEFT);
            card.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Phòng " + r.maPhong() + " • " + r.trangThai() + "\nDùng tab Vệ sinh và bảo trì để tạo công việc.\nĐặt/check-in/check-out do module đặt phòng thực hiện."));
            pnlSoDoPhong.add(card);
        }
        if (pnlSoDoPhong.getComponentCount() == 0) pnlSoDoPhong.add(new JLabel("Không có phòng phù hợp."));
        pnlSoDoPhong.revalidate(); pnlSoDoPhong.repaint();
    }
    private static String html(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    private class Editor extends JPanel {
        final int kind;
        final DefaultTableModel modelDanhSach;
        final JTable tblDanhSach;
        final JTextField txtMa = new JTextField(), txtTen = new JTextField();
        final JTextField txtGia = new JTextField(), txtSucChua = new JTextField();
        final JComboBox<LoaiPhong> cboLoaiPhong = new JComboBox<>();
        final JComboBox<Tang> cboTang = new JComboBox<>();
        final JComboBox<TrangThaiPhong> cboTrangThai = new JComboBox<>(TrangThaiPhong.values());
        final JTextField txtTimKiem = new JTextField(24);
        final JLabel lblSoBanGhi = new JLabel();
        String maDangChon;
        Editor(int kind, String[] columns) {
            this.kind = kind; setLayout(new BorderLayout(14, 14));
            setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
            modelDanhSach = new DefaultTableModel(columns, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
                public Class<?> getColumnClass(int c) {
                    if (kind == 0 && c == 5 || kind == 1 && c == 3) return BigDecimal.class;
                    if (kind == 1 && c == 2) return Integer.class;
                    if (kind == 2 && c == 2) return Long.class;
                    return Object.class;
                }
            };
            tblDanhSach = new JTable(modelDanhSach); HotelTheme.table(tblDanhSach);
            JPanel listing = new JPanel(new BorderLayout(0, 10));
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            toolbar.add(new JLabel("Tìm kiếm:")); toolbar.add(txtTimKiem); toolbar.add(lblSoBanGhi);
            JScrollPane tableScroll = new JScrollPane(tblDanhSach);
            tableScroll.setColumnHeaderView(tblDanhSach.getTableHeader());
            listing.add(toolbar, BorderLayout.NORTH); listing.add(tableScroll, BorderLayout.CENTER);
            JPanel form = new JPanel(new BorderLayout(0, 12));
            form.setBorder(BorderFactory.createTitledBorder("Thông tin " + (kind == 0 ? "phòng" : kind == 1 ? "loại phòng" : "tầng")));
            form.setPreferredSize(new Dimension(290, 430));
            JPanel fields = new JPanel(new GridLayout(0, 1, 4, 5));
            field(fields, "Mã *", txtMa); field(fields, kind == 0 ? "Số phòng *" : "Tên *", txtTen);
            if (kind == 0) { field(fields, "Loại phòng *", cboLoaiPhong); field(fields, "Tầng *", cboTang); field(fields, "Giá riêng VND (trống = giá loại)", txtGia); field(fields, "Trạng thái (chỉ xem)", cboTrangThai); cboTrangThai.setEnabled(false); }
            if (kind == 1) { field(fields, "Sức chứa (người) *", txtSucChua); field(fields, "Giá / đêm (VND) *", txtGia); }
            JPanel top = new JPanel(new BorderLayout()); top.add(fields, BorderLayout.NORTH); form.add(top, BorderLayout.CENTER);
            JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
            button(buttons, "Thêm", () -> save(false)); button(buttons, "Cập nhật", () -> save(true));
            button(buttons, kind == 0 ? "Ngừng sử dụng" : "Xóa", this::delete); button(buttons, "Làm mới", this::clear);
            form.add(buttons, BorderLayout.SOUTH);
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listing, form);
            split.setResizeWeight(1); split.setDividerLocation(720); split.setBorder(null);
            add(split, BorderLayout.CENTER);
            tblDanhSach.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && tblDanhSach.getSelectedRow() >= 0) select();
            });
            txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { filter(); }
                public void removeUpdate(DocumentEvent e) { filter(); }
                public void changedUpdate(DocumentEvent e) { filter(); }
            });
        }
        void field(JPanel p, String label, JComponent input) { p.add(new JLabel(label)); p.add(input); }
        void button(JPanel p, String text, Runnable action) {
            JButton b = new JButton(text); b.addActionListener(e -> action.run()); p.add(b);
        }
        void filter() {
            @SuppressWarnings("unchecked")
            TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>)tblDanhSach.getRowSorter();
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String q = txtTimKiem.getText().trim().toLowerCase(Locale.ROOT);
                    for (int i = 0; i < entry.getValueCount(); i++)
                        if (entry.getStringValue(i).toLowerCase(Locale.ROOT).contains(q)) return true;
                    return false;
                }
            });
            lblSoBanGhi.setText(tblDanhSach.getRowCount() + " bản ghi");
        }
        void load() {
            String keep = maDangChon;
            modelDanhSach.setRowCount(0);
            if (kind == 0) {
                cboLoaiPhong.removeAllItems(); cboTang.removeAllItems();
                for (LoaiPhong t : controller.types()) cboLoaiPhong.addItem(t);
                for (Tang f : controller.floors()) cboTang.addItem(f);
                for (Phong r : controller.rooms()) modelDanhSach.addRow(new Object[]{r.maPhong(), r.soPhong(),
                    controller.type(r.maLoaiPhong()).tenLoai(), controller.floor(r.maTang()).tenTang(), r.trangThai(), (r.giaPhong() == null ? controller.type(r.maLoaiPhong()).giaTheoNgay() : r.giaPhong())});
            } else if (kind == 1) {
                for (LoaiPhong t : controller.types()) modelDanhSach.addRow(new Object[]{t.maLoaiPhong(), t.tenLoai(), t.soNguoiToiDa(), t.giaTheoNgay()});
            } else for (Tang f : controller.floors()) modelDanhSach.addRow(new Object[]{f.maTang(), f.tenTang(),
                controller.rooms().stream().filter(r -> r.maTang().equals(f.maTang())).count()});
            filter();
            if (keep != null) for (int row = 0; row < modelDanhSach.getRowCount(); row++)
                if (keep.equals(modelDanhSach.getValueAt(row, 0))) {
                    int view = tblDanhSach.convertRowIndexToView(row);
                    if (view >= 0) { tblDanhSach.setRowSelectionInterval(view, view); select(); }
                }
        }
        void select() {
            int row = tblDanhSach.convertRowIndexToModel(tblDanhSach.getSelectedRow());
            maDangChon = modelDanhSach.getValueAt(row, 0).toString();
            txtMa.setText(maDangChon); txtMa.setEditable(false);
            if (kind == 0) {
                Phong r = controller.room(maDangChon); txtTen.setText(r.soPhong()); txtGia.setText(r.giaPhong() == null ? "" : r.giaPhong().toPlainString());
                cboLoaiPhong.setSelectedItem(controller.type(r.maLoaiPhong())); cboTang.setSelectedItem(controller.floor(r.maTang())); cboTrangThai.setSelectedItem(r.trangThai());
            } else if (kind == 1) {
                LoaiPhong t = controller.type(maDangChon); txtTen.setText(t.tenLoai());
                txtSucChua.setText("" + t.soNguoiToiDa()); txtGia.setText(t.giaTheoNgay().toPlainString());
            } else txtTen.setText(controller.floor(maDangChon).tenTang());
        }
        void clear() {
            maDangChon = null; tblDanhSach.clearSelection(); txtMa.setEditable(true);
            txtMa.setText(""); txtTen.setText(""); txtSucChua.setText(""); txtGia.setText("");
            cboTrangThai.setSelectedItem(TrangThaiPhong.TRONG);
        }
        void save(boolean update) {
            try {
                if (update && maDangChon == null) throw new IllegalArgumentException("Chọn một dòng để cập nhật.");
                String id = txtMa.getText().trim().toUpperCase(Locale.ROOT), label = txtTen.getText().trim();
                if (kind == 0) {
                    LoaiPhong t = (LoaiPhong)cboLoaiPhong.getSelectedItem(); Tang f = (Tang)cboTang.getSelectedItem();
                    controller.saveRoom(new Phong(id, label, t == null ? null : t.maLoaiPhong(), f == null ? null : f.maTang(),
                        txtGia.getText().isBlank() ? null : new BigDecimal(txtGia.getText().trim()), (TrangThaiPhong)cboTrangThai.getSelectedItem(), false), update, this::clear);
                } else if (kind == 1) controller.saveType(new LoaiPhong(id, label, Integer.parseInt(txtSucChua.getText().trim()), new BigDecimal(txtGia.getText().trim())), update, this::clear);
                else controller.saveFloor(new Tang(id, label), update, this::clear);
            } catch (NumberFormatException ex) { error("Sức chứa và giá phải là số. Ví dụ: 2 và 650000."); }
            catch (IllegalArgumentException ex) { error(ex.getMessage()); }
        }
        void delete() {
            if (maDangChon == null) { error("Chọn một dòng để xóa."); return; }
            if (JOptionPane.showConfirmDialog(this, (kind == 0 ? "Ngừng sử dụng " : "Xóa ") + maDangChon + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try {
                if (kind == 0) controller.deleteRoom(maDangChon, this::clear);
                else if (kind == 1) controller.deleteType(maDangChon, this::clear); else controller.deleteFloor(maDangChon, this::clear);
            } catch (IllegalArgumentException ex) { error(ex.getMessage()); }
        }
        void error(String text) { JOptionPane.showMessageDialog(this, text, "Kiểm tra thông tin", JOptionPane.WARNING_MESSAGE); }
    }
}
