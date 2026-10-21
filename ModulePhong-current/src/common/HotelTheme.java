package common;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/** Common styling for every team's JPanel; no external dependency. */
public final class HotelTheme {
    public static final Color INK = new Color(28, 46, 66);
    public static final Color ACCENT = new Color(20, 112, 115);
    public static final Color BACKGROUND = new Color(244, 247, 250);
    private HotelTheme() {}
    public static void install() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception ignored) { }
        Font font = new Font("SansSerif", Font.PLAIN, 13);
        for (Object key : UIManager.getDefaults().keySet().toArray())
            if (UIManager.get(key) instanceof Font) UIManager.put(key, font);
        UIManager.put("control", BACKGROUND);
        UIManager.put("nimbusBase", INK);
        UIManager.put("nimbusSelectionBackground", ACCENT);
    }
    public static void table(JTable table) {
        table.setRowHeight(34); table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int column) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
                if (!selected) { setBackground(row % 2 == 0 ? Color.WHITE : BACKGROUND); setForeground(INK); }
                return this;
            }
        });
    }
}
