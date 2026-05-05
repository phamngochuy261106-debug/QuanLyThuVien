package com.library.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class UIConstants {

    public static final Color C_BG       = new Color(248, 249, 252);
    public static final Color C_SIDEBAR  = new Color(26,  35,  50);
    public static final Color C_SECT_HDR = new Color(18,  25,  38);
    public static final Color C_PRIMARY  = new Color(41,  98,  218);
    public static final Color C_SUCCESS  = new Color(22,  163, 112);
    public static final Color C_DANGER   = new Color(220, 53,  53);
    public static final Color C_WARNING  = new Color(230, 140, 20);
    public static final Color C_WHITE    = Color.WHITE;
    public static final Color C_BORDER   = new Color(218, 225, 237);
    public static final Color C_TEXT_MUT = new Color(100, 116, 139);
    public static final Color C_ROW_ALT  = new Color(248, 250, 254);
    public static final Color C_ROW_SEL  = new Color(219, 234, 254);
    public static final Color C_HDR_BG   = new Color(241, 245, 252);

    public static final Font F_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font F_BOLD   = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font F_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font F_TABLE  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_TABLE_H= new Font("Segoe UI", Font.BOLD,  13);

    public static JButton btn(String text, Color fg, Color bg) {
        JButton b = new JButton(text);
        b.setForeground(fg); b.setBackground(bg); b.setFont(F_BOLD);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(bg.darker()); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }

    public static JLabel lbl(String s) {
        if (s == null) return new JLabel();
        JLabel l = new JLabel(s); l.setFont(F_BOLD); return l;
    }

    public static JLabel infoLabel() {
        JLabel l = new JLabel("—"); l.setFont(F_NORMAL); l.setForeground(C_TEXT_MUT); return l;
    }

    public static JTextField tf() {
        JTextField t = new JTextField(15); t.setFont(F_NORMAL);
        t.setPreferredSize(new Dimension(150, 32));
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        return t;
    }

    public static JTextField tf(int cols) {
        JTextField t = new JTextField(cols); t.setFont(F_NORMAL);
        t.setPreferredSize(new Dimension(cols * 8 + 20, 32));
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        return t;
    }

    public static JTextField searchField(String hint) {
        JTextField t = new JTextField(20); t.setFont(F_NORMAL);
        t.setPreferredSize(new Dimension(220, 34));
        t.putClientProperty("JTextField.placeholderText", hint);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return t;
    }

    public static JPanel rowPanel(Component... cs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setOpaque(false);
        for (Component c : cs) if (c != null) p.add(c);
        return p;
    }

    public static JPanel formPanel() {
        JPanel f = new JPanel(new GridBagLayout()); f.setBackground(C_WHITE);
        f.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        return f;
    }

    public static GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(5, 6, 5, 6);
        return g;
    }

    public static void fRow(JPanel f, GridBagConstraints g, int row, String label, JComponent comp) {
        g.gridx=0; g.gridy=row; g.gridwidth=1; g.weightx=0; f.add(lbl(label), g);
        g.gridx=1; g.weightx=1; f.add(comp, g);
    }

    public static JPanel btnPanel(JButton save, JButton cancel) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false); p.add(cancel); p.add(save); return p;
    }

    public static EmptyBorder pad(int n) { return new EmptyBorder(n, n, n, n); }

    public static JPanel titledGroup(String title) {
        JPanel p = new JPanel(); p.setBackground(C_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER),
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(2,8,8,8),
                title, TitledBorder.LEFT, TitledBorder.TOP, F_BOLD, C_PRIMARY)));
        return p;
    }

    public static JPanel statCard(String title, JLabel val, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 0)); p.setBackground(C_WHITE);
        p.setBorder(BorderFactory.createLineBorder(C_BORDER));

        // Accent bar trai
        JPanel accentBar = new JPanel(); accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(4, 0)); p.add(accentBar, BorderLayout.WEST);

        // Noi dung chinh
        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel lb = new JLabel(title); lb.setForeground(C_TEXT_MUT); lb.setFont(F_SMALL);
        val.setFont(new Font("Segoe UI", Font.BOLD, 28)); val.setForeground(accent);

        content.add(lb, BorderLayout.NORTH);
        content.add(val, BorderLayout.CENTER);

        p.add(content, BorderLayout.CENTER); return p;
    }

    public static JTable styledTable(DefaultTableModel m) {
        JTable t = new JTable(m) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row%2==0?C_WHITE:C_ROW_ALT);
                return c;
            }
        };
        t.setRowHeight(36); t.setFont(F_TABLE);
        t.setSelectionBackground(C_ROW_SEL); t.setSelectionForeground(Color.BLACK);
        t.setGridColor(C_BORDER); t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader h = t.getTableHeader();
        h.setFont(F_TABLE_H); h.setBackground(C_HDR_BG);
        h.setForeground(new Color(51,65,85));
        h.setPreferredSize(new Dimension(0, 40));
        h.setBorder(BorderFactory.createMatteBorder(0,0,2,0,C_PRIMARY));
        h.setReorderingAllowed(false);
        return t;
    }

    public static TableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lb=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                lb.setHorizontalAlignment(CENTER);
                String val = v!=null?v.toString():"";
                if (!sel) {
                    if (val.contains("hạn")||val.contains("Hạn")) { lb.setForeground(C_DANGER); lb.setFont(F_BOLD); }
                    else if (val.contains("mượn")||val.contains("Mượn")) { lb.setForeground(C_WARNING); lb.setFont(F_BOLD); }
                    else if (val.contains("trả")||val.contains("Trả")) { lb.setForeground(C_SUCCESS); lb.setFont(F_NORMAL); }
                    else lb.setForeground(Color.BLACK);
                }
                return lb;
            }
        };
    }

    public static void addRow4(JPanel f, GridBagConstraints g, int row,
            String l1, JComponent c1, String l2, JComponent c2,
            String l3, JComponent c3, String l4, JComponent c4) {
        g.gridy=row;
        g.gridx=0;g.weightx=0;f.add(lbl(l1),g);g.gridx=1;g.weightx=1;if(c1!=null)f.add(c1,g);
        if(l2!=null){g.gridx=2;g.weightx=0;f.add(lbl(l2),g);g.gridx=3;g.weightx=1;if(c2!=null)f.add(c2,g);}
        if(l3!=null){g.gridx=4;g.weightx=0;f.add(lbl(l3),g);g.gridx=5;g.weightx=1;if(c3!=null)f.add(c3,g);}
        if(l4!=null){g.gridx=6;g.weightx=0;f.add(lbl(l4),g);g.gridx=7;g.weightx=1;if(c4!=null)f.add(c4,g);}
    }

    public static void loadCombo(JComboBox<String> cbo, String sql) {
        try (Connection c=com.library.dao.DBConnection.getConnection();
             ResultSet rs=c.createStatement().executeQuery(sql)) {
            while(rs.next()) cbo.addItem(rs.getString(1));
        } catch (Exception ignored) {}
    }

    public static Integer comboId(Connection c, String bang, String colId,
            String colTen, JComboBox<String> cbo) throws Exception {
        if(cbo.getSelectedIndex()<0||cbo.getSelectedItem()==null) return null;
        PreparedStatement ps=c.prepareStatement("SELECT "+colId+" FROM "+bang+" WHERE "+colTen+"=?");
        ps.setString(1,(String)cbo.getSelectedItem());
        ResultSet rs=ps.executeQuery();
        return rs.next()?rs.getInt(1):null;
    }

    public static String fmt(SimpleDateFormat sdf, java.sql.Date d) { return d!=null?sdf.format(d):""; }
    public static int parseInt(String s, int def) { try{return Integer.parseInt(s.trim());}catch(Exception e){return def;} }
    public static double parseDouble(String s, double def) { try{return Double.parseDouble(s.trim());}catch(Exception e){return def;} }

    public static String removeAccent(String s) {
        if(s==null) return "";
        s=s.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]","a"); s=s.replaceAll("[èéẹẻẽêềếệểễ]","e");
        s=s.replaceAll("[ìíịỉĩ]","i"); s=s.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]","o");
        s=s.replaceAll("[ùúụủũưừứựửữ]","u"); s=s.replaceAll("[ỳýỵỷỹ]","y"); s=s.replaceAll("[đ]","d");
        s=s.replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]","A"); s=s.replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]","E");
        s=s.replaceAll("[ÌÍỊỈĨ]","I"); s=s.replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]","O");
        s=s.replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]","U"); s=s.replaceAll("[ỲÝỴỶỸ]","Y"); s=s.replaceAll("[Đ]","D");
        return s;
    }

    public static boolean matchSearch(String text, String kw) {
        if(kw==null||kw.isEmpty()) return true;
        String kl=kw.toLowerCase();
        return text.toLowerCase().contains(kl)||removeAccent(text).toLowerCase().contains(kl);
    }
}