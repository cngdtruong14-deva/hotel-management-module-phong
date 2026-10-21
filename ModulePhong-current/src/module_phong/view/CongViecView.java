package module_phong.view;
import common.HotelTheme;
import module_phong.controller.PhongController;
import module_phong.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class CongViecView extends JPanel {
    private final PhongController controller;
    private final JComboBox<String> cboLoaiCongViec = new JComboBox<>(new String[]{"VESINH","BAOTRI"});
    private final JComboBox<String> cboPhong = new JComboBox<>();
    private final JTextField txtNoiDung = new JTextField(26), txtNguoiPhuTrach = new JTextField(16);
    private final DefaultTableModel modelCongViec = new DefaultTableModel(new String[]{"Mã công việc","Loại","Mã phòng","Nội dung","Phụ trách","Bắt đầu","Kết thúc"},0) {
        public boolean isCellEditable(int row,int column) { return false; }
    };
    private final JTable tblCongViec = new JTable(modelCongViec) {
        public String getToolTipText(java.awt.event.MouseEvent event) {
            int r=rowAtPoint(event.getPoint()), c=columnAtPoint(event.getPoint());
            return r<0 || c<0 ? null : String.valueOf(getValueAt(r,c));
        }
    };
    public CongViecView(PhongController controller) {
        this.controller=controller; setLayout(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(16,0,0,0));
        JPanel form = new JPanel(new GridLayout(0,2,12,8));
        form.add(new JLabel("Loại công việc")); form.add(cboLoaiCongViec);
        form.add(new JLabel("Phòng")); form.add(cboPhong);
        form.add(new JLabel("Nội dung")); form.add(txtNoiDung);
        form.add(new JLabel("Người phụ trách")); form.add(txtNguoiPhuTrach);
        JButton btnBatDau = new JButton("Bắt đầu công việc"), btnHoanTat = new JButton("Hoàn tất dòng đã chọn");
        form.add(btnBatDau); form.add(btnHoanTat); add(form,BorderLayout.NORTH);
        HotelTheme.table(tblCongViec);
        tblCongViec.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths={285,90,90,260,140,160,160};
        for(int i=0;i<widths.length;i++) tblCongViec.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JScrollPane scroll=new JScrollPane(tblCongViec); scroll.setColumnHeaderView(tblCongViec.getTableHeader()); add(scroll,BorderLayout.CENTER);
        btnBatDau.addActionListener(e -> {
            String ma=(String)cboPhong.getSelectedItem();
            if(ma==null) { JOptionPane.showMessageDialog(this,"Chọn phòng."); return; }
            controller.startJob((String)cboLoaiCongViec.getSelectedItem(),ma,txtNoiDung.getText().trim(),txtNguoiPhuTrach.getText().trim(),() -> { txtNoiDung.setText(""); txtNguoiPhuTrach.setText(""); });
        });
        btnHoanTat.addActionListener(e -> {
            int row=tblCongViec.getSelectedRow();
            if(row<0) { JOptionPane.showMessageDialog(this,"Chọn công việc."); return; }
            row=tblCongViec.convertRowIndexToModel(row);
            if(JOptionPane.showConfirmDialog(this,"Hoàn tất công việc và chuyển phòng về TRONG?","Xác nhận",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
                controller.completeJob(modelCongViec.getValueAt(row,0).toString());
        });
    }
    public void refresh() {
        Object selected=cboPhong.getSelectedItem(); cboPhong.removeAllItems();
        for(Phong p:controller.rooms()) cboPhong.addItem(p.maPhong());
        if(selected!=null) cboPhong.setSelectedItem(selected);
        modelCongViec.setRowCount(0);
        DateTimeFormatter format=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for(CongViecPhong j:controller.jobs()) modelCongViec.addRow(new Object[]{j.maCongViec(),j.loai(),j.maPhong(),j.noiDung(),j.nguoiPhuTrach(),format.format(j.batDau()),j.ketThuc()==null?"Đang thực hiện":format.format(j.ketThuc())});
    }
}
