package module_phong.view;
import common.HotelTheme;
import module_phong.controller.HoSoPhongController;
import module_phong.model.HoSoPhong.*;
import module_phong.model.Phong;
import module_khachhang.model.KhachHang;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Room manager workspace. No booking/check-in/out/service-consumption actions. */
public final class HoSoPhongView extends JPanel {
 private final HoSoPhongController ctl;
 private HoSoPhongController.Snapshot snapshot=new HoSoPhongController.Snapshot(DuLieu.rong(),java.util.List.of(),java.util.List.of());
 private final JComboBox<String> cboPhong=new JComboBox<>();private final JComboBox<KhachHang> cboKhach=new JComboBox<>();
 private final JTextArea txtMoTa=area(3),txtGhiChu=area(2);
 private final JTextField txtMaTienNghi=new JTextField(),txtTenTienNghi=new JTextField(),txtMaYeuCau=new JTextField(newId());
 private final JSpinner spnSoLuong=new JSpinner(new SpinnerNumberModel(1,0,10000,1));
 private final JComboBox<String> cboTinhTrang=new JComboBox<>(new String[]{"TOT","CAN_SUA","NGUNG_DUNG"});
 private final JLabel lblStatus=new JLabel("Chọn phòng để xem thông tin."),lblReview=new JLabel(),lblLuuTru=new JLabel();
 private final DefaultTableModel amenities=model("Mã tiện nghi","Tên tiện nghi / thiết bị","Số lượng","Tình trạng");
 private final DefaultTableModel reviews=model("Khách hàng","Lượt ở","Điểm / 5","Nội dung review","Ngày đánh giá");
 private final DefaultTableModel requests=model("Mã yêu cầu","Mã khách","Ghi chú","Kết quả","Phiếu từ Lưu trú","Phản hồi");
 private final DefaultTableModel stays=model("Khách hàng","Lượt ở","Trạng thái nguồn","Phiên bản nhận","Lần nhận phản hồi");
 private final DefaultTableModel issues=model("Mã phản ánh","Khách hàng","Lượt ở","Nội dung","Trạng thái","Cập nhật");
 private final java.util.List<JComponent> controls=new ArrayList<>();private boolean updating;
 public HoSoPhongView(HoSoPhongController ctl,String mode){
  this.ctl=ctl;setLayout(new BorderLayout(10,12));setBorder(BorderFactory.createEmptyBorder(12,0,0,0));
  JPanel top=new JPanel(new BorderLayout(0,6));JPanel chooser=new JPanel(new FlowLayout(FlowLayout.LEFT));
  chooser.add(new JLabel("Phòng:"));cboPhong.setPreferredSize(new Dimension(160,30));chooser.add(cboPhong);controls.add(cboPhong);chooser.add(button("Tải lại",ctl::refresh));top.add(chooser,BorderLayout.NORTH);
  top.add(new JLabel(mode+" • Yêu cầu chỉ chuyển cho bộ phận đặt phòng; không tự giữ phòng."),BorderLayout.CENTER);top.add(lblStatus,BorderLayout.SOUTH);add(top,BorderLayout.NORTH);
  JTabbedPane tabs=new JTabbedPane();tabs.addTab("Thông tin & tiện nghi",details());tabs.addTab("Review phòng",reviewPanel());tabs.addTab("Yêu cầu đặt phòng",requestPanel());tabs.addTab("Lưu trú & phản ánh",stayPanel());add(tabs,BorderLayout.CENTER);
  cboPhong.addActionListener(e->{if(!updating)renderRoom();});
  ctl.onData(this::render);ctl.onBusy(b->{for(var c:controls)c.setEnabled(!b);if(b)lblStatus.setText("Đang tải / lưu dữ liệu…");});
  ctl.onError(message->{lblStatus.setText("Lỗi: "+message);JOptionPane.showMessageDialog(this,message,"Kiểm tra dữ liệu",JOptionPane.WARNING_MESSAGE);});
  ctl.onSuccess(lblStatus::setText);SwingUtilities.invokeLater(ctl::refresh);
 }
 private static JTextArea area(int rows){var a=new JTextArea(rows,25);a.setLineWrap(true);a.setWrapStyleWord(true);return a;}
 private static String newId(){return "YC"+UUID.randomUUID().toString().replace("-","").substring(0,18).toUpperCase(Locale.ROOT);}
 private static DefaultTableModel model(String... names){return new DefaultTableModel(names,0){public boolean isCellEditable(int r,int c){return false;}};}
 private static JTable table(DefaultTableModel m){var t=new JTable(m);HotelTheme.table(t);
  t.setToolTipText("Nhấp đúp một dòng để đọc đầy đủ nội dung.");
  t.addMouseListener(new java.awt.event.MouseAdapter(){public void mouseClicked(java.awt.event.MouseEvent e){if(e.getClickCount()!=2||t.getSelectedRow()<0)return;int row=t.convertRowIndexToModel(t.getSelectedRow());var text=new StringBuilder();for(int i=0;i<m.getColumnCount();i++)text.append(m.getColumnName(i)).append(": ").append(Objects.toString(m.getValueAt(row,i),"")).append("\n\n");var a=area(15);a.setText(text.toString());a.setEditable(false);a.setCaretPosition(0);JOptionPane.showMessageDialog(t,new JScrollPane(a),"Chi tiết",JOptionPane.INFORMATION_MESSAGE);}});return t;}
 private static JScrollPane scroll(JTable t){var s=new JScrollPane(t);s.setColumnHeaderView(t.getTableHeader());return s;}
 private static void field(JPanel p,String text,JComponent c){p.add(new JLabel(text));p.add(c);}
 private JButton button(String label,Runnable r){var b=new JButton(label);controls.add(b);b.addActionListener(e->{try{r.run();}catch(IllegalArgumentException ex){lblStatus.setText(ex.getMessage());}});return b;}
 private String room(){String value=(String)cboPhong.getSelectedItem();if(value==null)throw new IllegalArgumentException("Chọn phòng.");return value;}
 private JPanel details(){
  JPanel panel=new JPanel(new BorderLayout(8,8));JPanel top=new JPanel(new BorderLayout(6,6));top.add(new JLabel("Mô tả phòng, hướng nhìn, đặc điểm:"),BorderLayout.NORTH);top.add(new JScrollPane(txtMoTa),BorderLayout.CENTER);controls.add(txtMoTa);
  top.add(button("Lưu mô tả",()->{var p=new ChiTiet(room(),txtMoTa.getText().trim());ctl.run(s->s.luuChiTiet(p),"Đã lưu mô tả phòng.");}),BorderLayout.EAST);panel.add(top,BorderLayout.NORTH);
  var tbl=table(amenities);panel.add(scroll(tbl),BorderLayout.CENTER);
  JPanel bottom=new JPanel(new BorderLayout(6,6));JPanel form=new JPanel(new GridLayout(2,4,8,6));field(form,"Mã tiện nghi",txtMaTienNghi);field(form,"Tên thiết bị",txtTenTienNghi);field(form,"Số lượng",spnSoLuong);field(form,"Tình trạng",cboTinhTrang);
  controls.addAll(java.util.List.of(txtMaTienNghi,txtTenTienNghi,spnSoLuong,cboTinhTrang));bottom.add(form,BorderLayout.CENTER);
  bottom.add(button("Lưu tiện nghi",()->{try{spnSoLuong.commitEdit();}catch(java.text.ParseException ex){throw new IllegalArgumentException("Số lượng phải là số nguyên.");}
   var p=new TienNghi(room(),txtMaTienNghi.getText().trim().toUpperCase(Locale.ROOT),txtTenTienNghi.getText().trim(),(Integer)spnSoLuong.getValue(),(String)cboTinhTrang.getSelectedItem());ctl.run(s->s.luuTienNghi(p),"Đã lưu tiện nghi. Số lượng 0 dùng khi phòng không còn thiết bị.");}),BorderLayout.SOUTH);panel.add(bottom,BorderLayout.SOUTH);
  tbl.getSelectionModel().addListSelectionListener(e->{int row=tbl.getSelectedRow();if(e.getValueIsAdjusting()||row<0)return;row=tbl.convertRowIndexToModel(row);txtMaTienNghi.setText(amenities.getValueAt(row,0).toString());txtTenTienNghi.setText(amenities.getValueAt(row,1).toString());spnSoLuong.setValue(amenities.getValueAt(row,2));cboTinhTrang.setSelectedItem(amenities.getValueAt(row,3));});return panel;
 }
 private JPanel reviewPanel(){var p=new JPanel(new BorderLayout(6,8));p.add(lblReview,BorderLayout.NORTH);p.add(scroll(table(reviews)),BorderLayout.CENTER);p.add(new JLabel("Review chỉ đọc, gắn khách và lượt ở đã được xác minh; tiếp nhận qua cổng module nguồn."),BorderLayout.SOUTH);return p;}
 private JPanel requestPanel(){var p=new JPanel(new BorderLayout(8,8));var top=new JPanel(new BorderLayout(8,8));var form=new JPanel(new GridLayout(2,2,8,6));field(form,"Mã yêu cầu (giữ mã khi thử gửi lại)",txtMaYeuCau);field(form,"Khách hàng",cboKhach);controls.add(txtMaYeuCau);controls.add(cboKhach);controls.add(txtGhiChu);top.add(form,BorderLayout.NORTH);top.add(new JScrollPane(txtGhiChu),BorderLayout.CENTER);
  var buttons=new JPanel(new FlowLayout(FlowLayout.LEFT));buttons.add(button("Chuyển yêu cầu",()->{var k=(KhachHang)cboKhach.getSelectedItem();if(k==null)throw new IllegalArgumentException("Chọn khách đang hoạt động.");String id=txtMaYeuCau.getText().trim().toUpperCase(Locale.ROOT),r=room(),note=txtGhiChu.getText().trim();ctl.run(s->s.guiYeuCau(id,k.maKhachHang(),r,note),"Đã lưu yêu cầu vào hàng đợi. Kết quả tiếp nhận xem ở bảng bên dưới.");}));buttons.add(button("Yêu cầu mới",()->{txtMaYeuCau.setText(newId());txtGhiChu.setText("");}));top.add(buttons,BorderLayout.SOUTH);p.add(top,BorderLayout.NORTH);p.add(scroll(table(requests)),BorderLayout.CENTER);p.add(new JLabel("Chờ module Lưu trú đọc hàng đợi và phản hồi. Phòng mong muốn chưa được đảm bảo còn trống."),BorderLayout.SOUTH);return p;
 }
 private JPanel stayPanel(){var p=new JPanel(new BorderLayout(6,8));p.add(lblLuuTru,BorderLayout.NORTH);var split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,scroll(table(stays)),scroll(table(issues)));split.setResizeWeight(.4);split.setDividerLocation(160);p.add(split,BorderLayout.CENTER);p.add(new JLabel("Phản ánh do module Lưu trú gửi về. Việc xử lý/trả kết quả thuộc bộ phận nguồn; bảng này chỉ theo dõi."),BorderLayout.SOUTH);return p;}
 private void render(HoSoPhongController.Snapshot p){snapshot=p;updating=true;String old=(String)cboPhong.getSelectedItem();var oldKh=(KhachHang)cboKhach.getSelectedItem();cboPhong.removeAllItems();for(Phong r:p.rooms())cboPhong.addItem(r.maPhong());if(old!=null&&p.rooms().stream().anyMatch(r->r.maPhong().equals(old)))cboPhong.setSelectedItem(old);
  cboKhach.removeAllItems();for(var k:p.customers()){cboKhach.addItem(k);if(oldKh!=null&&k.maKhachHang().equals(oldKh.maKhachHang()))cboKhach.setSelectedItem(k);}updating=false;renderRoom();lblStatus.setText("Đã tải dữ liệu. Trạng thái tiếp nhận xem theo từng yêu cầu.");}
 private void renderRoom(){String r=(String)cboPhong.getSelectedItem();var d=snapshot.data();txtMoTa.setText(d.chiTiets().stream().filter(x->x.maPhong().equals(r)).map(ChiTiet::moTa).findFirst().orElse(""));txtMaTienNghi.setText("");txtTenTienNghi.setText("");spnSoLuong.setValue(1);
  amenities.setRowCount(0);d.tienNghis().stream().filter(x->x.maPhong().equals(r)).forEach(x->amenities.addRow(new Object[]{x.maTienNghi(),x.tenTienNghi(),x.soLuong(),x.tinhTrang()}));
  var rated=d.danhGias().stream().filter(x->x.maPhong().equals(r)).toList();reviews.setRowCount(0);rated.forEach(x->reviews.addRow(new Object[]{x.maKhachHang(),x.maDatPhong(),x.diem(),x.noiDung(),x.taoLuc()}));lblReview.setText(rated.isEmpty()?"Chưa nhận review cho phòng này.":String.format(Locale.ROOT,"%d review • Điểm trung bình %.1f / 5",rated.size(),rated.stream().mapToInt(DanhGia::diem).average().orElse(0)));
  requests.setRowCount(0);d.yeuCaus().stream().filter(x->x.maPhong().equals(r)).forEach(x->requests.addRow(new Object[]{x.maYeuCau(),x.maKhachHang(),x.ghiChu(),x.trangThai(),x.maDatPhong(),x.lyDo()}));
  stays.setRowCount(0);var roomStays=d.luuTrus().stream().filter(x->x.maPhong().equals(r)).toList();roomStays.forEach(x->stays.addRow(new Object[]{x.maKhachHang(),x.maDatPhong(),x.trangThai(),x.phienBan(),x.capNhatLuc()}));
  var roomIssues=d.phanAnhs().stream().filter(x->x.maPhong().equals(r)).toList();issues.setRowCount(0);roomIssues.forEach(x->issues.addRow(new Object[]{x.maPhanAnh(),x.maKhachHang(),x.maDatPhong(),x.noiDung(),x.trangThai(),x.capNhatLuc()}));
  lblLuuTru.setText(roomStays.isEmpty()?"Chưa nhận thông tin lưu trú từ module nguồn.":"Khách đang ở: "+roomStays.stream().filter(x->x.trangThai().equals("DANG_O")).count()+" • Phản ánh chưa đóng: "+roomIssues.stream().filter(x->!x.trangThai().equals("DA_XU_LY")).count());
 }
}
