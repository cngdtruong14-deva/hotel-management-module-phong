package module_khachhang.view;

import java.awt.Component;
import javax.swing.*;
import module_khachhang.model.*;

final class NhanEnumRenderer extends DefaultListCellRenderer {
    @Override public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean selected, boolean focus) {
        Object label = value;
        if (value instanceof GioiTinh g) label = g.nhan();
        if (value instanceof LoaiGiayTo t) label = t.nhan();
        if (value instanceof TrangThaiKhachHang s) label = s.nhan();
        return super.getListCellRendererComponent(list, label, index, selected, focus);
    }
}
