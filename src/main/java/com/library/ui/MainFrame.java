package com.library.ui;

import com.library.dao.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import static com.library.ui.UIConstants.*;

public class MainFrame extends JFrame {

    private final String currentUser, currentRole;
    private JPanel contentPanel;

    private static String fmtVT(String v) {
        if ("Admin".equals(v))     return "Quản trị viên";
        if ("NhanVien".equals(v))  return "Nhân viên";
        return v == null ? "" : v;
    }

    public MainFrame(String hoTen, String vaiTro) {
        this.currentUser = hoTen;
        this.currentRole = vaiTro;
        setTitle("Phần Mềm Quản Lý Thư Viện");
        setSize(1280, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Icon ung dung - ve sach bang BufferedImage
        try {
            java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = icon.createGraphics();
            ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ig.setColor(new Color(37, 99, 235));
            ig.fillRoundRect(0, 0, 32, 32, 8, 8);
            ig.setColor(Color.WHITE);
            ig.fillRoundRect(4, 6, 7, 20, 2, 2);
            ig.fillRoundRect(13, 6, 6, 20, 2, 2);
            ig.fillRoundRect(21, 6, 7, 20, 2, 2);
            ig.setColor(new Color(200, 220, 255));
            ig.fillRect(4, 6, 7, 2); ig.fillRect(13, 6, 6, 2); ig.fillRect(21, 6, 7, 2);
            ig.dispose();
            setIconImage(icon);
        } catch (Exception ignored) {}
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1024, 600));

        // Content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(C_BG);

        // Root layout
        setLayout(new BorderLayout());
        add(buildToolbar(),  BorderLayout.NORTH);
        add(buildSidebar(),  BorderLayout.WEST);
        add(contentPanel,    BorderLayout.CENTER);
        add(buildStatus(),   BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            show("Tổng Quan", new DashPanel(currentUser, currentRole));
            kiemTraSapHanTra();
        });
    }

    // ── TOOLBAR ─────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        p.setBackground(new Color(245, 247, 250));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));
        for (String[] x : new String[][]{
            {"Tổng quan","dash"},{"Tài liệu","tailieu"},
            {"Độc giả","docgia"},{"Mượn","muon"},
            {"Trả","tra"},{"Thống kê","thongke"}}) {
            JButton b = new JButton(x[0]);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            b.setFocusPainted(false); b.setBorderPainted(false);
            b.setBackground(new Color(245, 247, 250));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> handleNav(x[1]));
            p.add(b);
        }
        JTextField tf = new JTextField(20);
        tf.setPreferredSize(new Dimension(220, 27));
        tf.putClientProperty("JTextField.placeholderText", "Tìm kiếm tài liệu...");
        JButton bTim = btn("Tìm", C_WHITE, C_PRIMARY);
        bTim.addActionListener(e ->
            show("Tìm kiếm", buildSearchPanel(tf.getText().trim())));
        tf.addActionListener(e -> bTim.doClick());
        p.add(Box.createHorizontalStrut(12));
        p.add(tf); p.add(bTim);
        return p;
    }

    // ── SIDEBAR ─────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(new Color(26, 35, 50));
        side.setPreferredSize(new Dimension(260, 0));

        // Badge
        JPanel badge = new JPanel();
        badge.setLayout(new BoxLayout(badge, BoxLayout.Y_AXIS));
        badge.setBackground(new Color(15, 23, 42));
        badge.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel n = new JLabel(currentUser);
        n.setForeground(Color.WHITE);
        n.setFont(new Font("Segoe UI", Font.BOLD, 13));
        n.setAlignmentX(LEFT_ALIGNMENT);

        JLabel r = new JLabel(fmtVT(currentRole));
        r.setForeground(new Color(148, 163, 184));
        r.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        r.setAlignmentX(LEFT_ALIGNMENT);

        badge.add(n); badge.add(Box.createVerticalStrut(2)); badge.add(r);
        side.add(badge, BorderLayout.NORTH);

        // Menu list
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(new Color(26, 35, 50));

        addGroup(menu, "QUẢN LÝ TÀI LIỆU", new String[][]{
            {"Biên mục tài liệu","tailieu"},
            {"Danh sách tài liệu","dstailieu"}
        });
        addGroup(menu, "QUẢN LÝ ĐỘC GIẢ", new String[][]{
            {"Danh sách độc giả","docgia"}
        });
        addGroup(menu, "MƯỢN – TRẢ", new String[][]{
            {"Mượn tài liệu","muon"},
            {"Trả tài liệu","tra"},
            {"Độc giả quá hạn","quahan"},
            {"Tình hình mượn trả","tracuumt"}
        });
        addGroup(menu, "DANH MỤC", new String[][]{
            {"Thể loại","theloai"},
            {"Tác giả","tacgia"},
            {"Nhà xuất bản","nxb"}
        });
        addGroup(menu, "HỆ THỐNG", new String[][]{
            {"Hồ sơ người dùng","hsnguoidung"},
            {"Thống kê báo cáo","thongke"},
            {"Đổi mật khẩu","doipass"},
            {"Đăng xuất","logout"}
        });

        JScrollPane sp = new JScrollPane(menu,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(10);
        side.add(sp, BorderLayout.CENTER);
        return side;
    }

    private void addGroup(JPanel menu, String title, String[][] items) {
        // Section header
        JLabel hdr = new JLabel(title);
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 10));
        hdr.setForeground(new Color(100, 116, 139));
        hdr.setOpaque(true);
        hdr.setBackground(new Color(15, 23, 42));
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 55, 75)),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        menu.add(hdr);

        // Menu items
        for (String[] it : items) {
            JButton b = new JButton("  " + it[0]);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            b.setForeground(new Color(200, 210, 225));
            b.setBackground(new Color(26, 35, 50));
            b.setOpaque(true);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorder(BorderFactory.createEmptyBorder(5, 16, 5, 8));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    b.setBackground(new Color(37, 52, 72));
                    b.setForeground(Color.WHITE);
                }
                public void mouseExited(MouseEvent e) {
                    b.setBackground(new Color(26, 35, 50));
                    b.setForeground(new Color(200, 210, 225));
                }
            });
            b.addActionListener(e -> handleNav(it[1]));
            menu.add(b);
        }
    }

    // ── Thông báo sắp đến hạn ──────────────────────────────────────
    private void kiemTraSapHanTra() {
        new Thread(() -> {
            try (java.sql.Connection c = com.library.dao.DBConnection.getConnection();
                 java.sql.ResultSet rs = c.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM MuonTra WHERE NgayTra IS NULL " +
                    "AND HanTra BETWEEN GETDATE() AND DATEADD(day,3,GETDATE())")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    int n = rs.getInt(1);
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                            "⚠  Có " + n + " tài liệu sắp đến hạn trả trong 3 ngày tới!\n" +
                            "Vào mục 'Tình hình mượn trả' để kiểm tra.",
                            "Nhắc nhở hạn trả", JOptionPane.WARNING_MESSAGE));
                }
            } catch (Exception ignored) {}
        }).start();
    }

    // ── STATUS BAR ──────────────────────────────────────────────────
    private JPanel buildStatus() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(245, 247, 250));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));
        p.setPreferredSize(new Dimension(0, 22));
        JLabel lb = new JLabel("  Đăng nhập: " + currentUser +
            " (" + fmtVT(currentRole) + ")  |  " +
            new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lb.setForeground(C_TEXT_MUT);
        p.add(lb, BorderLayout.WEST);
        return p;
    }

    // ── SHOW PANEL ──────────────────────────────────────────────────
    public void show(String title, JComponent panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ── NAV HANDLER ─────────────────────────────────────────────────
    private void handleNav(String key) {
        switch (key) {
            case "dash"        -> show("Tổng Quan",          new DashPanel(currentUser, currentRole));
            case "tailieu"     -> show("Biên Mục Tài Liệu",  new TaiLieuPanel(currentUser));
            case "dstailieu"   -> show("Danh Sách Tài Liệu", buildDSTLPanel());
            case "docgia"      -> show("Quản Lý Độc Giả",    new DocGiaPanel());
            case "muon"        -> show("Mượn Tài Liệu",      new MuonPanel(currentUser));
            case "tra"         -> show("Trả Tài Liệu",       new TraPanel(currentUser));
            case "quahan"      -> show("Độc Giả Quá Hạn",    new QuaHanPanel());
            case "tracuumt"    -> show("Tình Hình Mượn Trả", buildMuonTraPanel());
            case "theloai"     -> show("Thể Loại",           new DanhMucPanel("TheLoai","MaTheLoai","TenTheLoai","GhiChu","Thể loại"));
            case "tacgia"      -> show("Tác Giả",            new DanhMucPanel("TacGia","MaTG","TenTG","GhiChu","Tác giả"));
            case "nxb"         -> show("Nhà Xuất Bản",       new DanhMucPanel("NhaXuatBan","MaNXB","TenNXB","GhiChu","NXB"));
            case "thongke"     -> show("Thống Kê",           new ThongKePanel());
            case "hsnguoidung" -> show("Hồ Sơ Người Dùng",   new HoSoNDPanel());
            case "doipass"     -> show("Đổi Mật Khẩu",       new DoiPassPanel(this, currentUser));
            case "logout"      -> {
                if (JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn đăng xuất?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION) == 0) {
                    dispose();
                    new LoginFrame().setVisible(true);
                }
            }
            default -> JOptionPane.showMessageDialog(this, "Đang phát triển: " + key);
        }
    }

    // ── PANEL: DANH SÁCH TÀI LIỆU ───────────────────────────────────
    private JPanel buildDSTLPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(C_BG); p.setBorder(pad(16));

        JLabel title = new JLabel("Danh Sách Tài Liệu");
        title.setFont(F_TITLE); title.setForeground(new Color(30, 41, 59));

        JTextField tFind = tf(24);
        tFind.putClientProperty("JTextField.placeholderText", "Tìm theo tên, tác giả...");
        JButton bTim = btn("Tìm kiếm", C_WHITE, C_PRIMARY);

        javax.swing.table.DefaultTableModel md = new javax.swing.table.DefaultTableModel(
            new String[]{"Mã","Tên tài liệu","Thể loại","Tác giả","NXB","Năm XB","Tổng số"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tb = styledTable(md);
        JScrollPane sp = new JScrollPane(tb);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER));

        Runnable load = () -> {
            md.setRowCount(0);
            String k = tFind.getText().trim();
            String sql = "SELECT t.MaTL,t.TenTL,tl.TenTheLoai,tg.TenTG,n.TenNXB,t.NamXB,t.TongSo"
                + " FROM TaiLieu t"
                + " LEFT JOIN TheLoai tl ON t.MaTheLoai=tl.MaTheLoai"
                + " LEFT JOIN TacGia tg ON t.MaTG=tg.MaTG"
                + " LEFT JOIN NhaXuatBan n ON t.MaNXB=n.MaNXB"
                + (k.isEmpty() ? "" : " WHERE t.TenTL LIKE N'%"+k+"%' OR tg.TenTG LIKE N'%"+k+"%'")
                + " ORDER BY t.MaTL";
            try (Connection c = DBConnection.getConnection();
                 ResultSet rs = c.createStatement().executeQuery(sql)) {
                while (rs.next())
                    md.addRow(new Object[]{rs.getInt(1),rs.getString(2),
                        rs.getString(3),rs.getString(4),rs.getString(5),
                        rs.getInt(6),rs.getInt(7)});
            } catch (Exception e) { e.printStackTrace(); }
        };
        load.run();
        bTim.addActionListener(e -> load.run());
        tFind.addActionListener(e -> load.run());

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(lbl("Tìm:")); row.add(tFind); row.add(bTim);

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(title, BorderLayout.NORTH);
        north.add(row, BorderLayout.CENTER);

        p.add(north, BorderLayout.NORTH);
        p.add(sp,    BorderLayout.CENTER);
        return p;
    }

    // ── PANEL: TÌNH HÌNH MƯỢN TRẢ ───────────────────────────────────
    private JPanel buildMuonTraPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(C_BG); p.setBorder(pad(16));

        JLabel title = new JLabel("Tình Hình Mượn Trả");
        title.setFont(F_TITLE); title.setForeground(new Color(30, 41, 59));

        javax.swing.table.DefaultTableModel md = new javax.swing.table.DefaultTableModel(
            new String[]{"ID","Số thẻ","Họ tên","Tên tài liệu","Kiểu mượn",
                "Ngày mượn","Hạn trả","Ngày trả","Trạng thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tb = styledTable(md);
        tb.getColumnModel().getColumn(8).setCellRenderer(statusRenderer());
        JScrollPane sp = new JScrollPane(tb);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try (Connection c = DBConnection.getConnection();
             ResultSet rs = c.createStatement().executeQuery(
                "SELECT m.ID_MuonTra,m.SoThe,d.HoTen,tl.TenTL,m.KieuMuon,"
                +"m.NgayMuon,m.HanTra,m.NgayTra FROM MuonTra m"
                +" LEFT JOIN DocGia d ON m.SoThe=d.SoThe"
                +" LEFT JOIN TLChitiet tc ON m.MaCaBiet=tc.MaCaBiet"
                +" LEFT JOIN TaiLieu tl ON tc.MaTL=tl.MaTL"
                +" ORDER BY m.ID_MuonTra DESC")) {
            while (rs.next()) {
                String tt = rs.getDate("NgayTra") != null ? "Đã trả"
                    : (rs.getDate("HanTra") != null
                        && rs.getDate("HanTra").before(new Date())
                        ? "Quá hạn" : "Đang mượn");
                md.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                    rs.getString(5), fmt(sdf, rs.getDate(6)),
                    fmt(sdf, rs.getDate(7)), fmt(sdf, rs.getDate(8)), tt
                });
            }
        } catch (Exception e) { e.printStackTrace(); }

        p.add(title, BorderLayout.NORTH);
        p.add(sp,    BorderLayout.CENTER);
        return p;
    }

    // ── PANEL: TÌM KIẾM ─────────────────────────────────────────────
    private JPanel buildSearchPanel(String kw) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(C_BG); p.setBorder(pad(16));

        JLabel title = new JLabel("Tìm Kiếm Tài Liệu");
        title.setFont(F_TITLE); title.setForeground(new Color(30, 41, 59));

        JTextField tFind = tf(26); tFind.setText(kw);
        tFind.putClientProperty("JTextField.placeholderText", "Nhập tên hoặc tác giả...");
        JButton bTim = btn("Tìm", C_WHITE, C_PRIMARY);

        javax.swing.table.DefaultTableModel md = new javax.swing.table.DefaultTableModel(
            new String[]{"Mã","Tên tài liệu","Thể loại","Tác giả","NXB","Năm XB","Tổng số"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tb = styledTable(md);
        JScrollPane sp = new JScrollPane(tb);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER));

        Runnable search = () -> {
            md.setRowCount(0);
            String k = tFind.getText().trim();
            if (k.isEmpty()) return;
            try (Connection c = DBConnection.getConnection();
                 ResultSet rs = c.createStatement().executeQuery(
                    "SELECT t.MaTL,t.TenTL,tl.TenTheLoai,tg.TenTG,n.TenNXB,t.NamXB,t.TongSo"
                    +" FROM TaiLieu t"
                    +" LEFT JOIN TheLoai tl ON t.MaTheLoai=tl.MaTheLoai"
                    +" LEFT JOIN TacGia tg ON t.MaTG=tg.MaTG"
                    +" LEFT JOIN NhaXuatBan n ON t.MaNXB=n.MaNXB"
                    +" WHERE t.TenTL LIKE N'%"+k+"%' OR tg.TenTG LIKE N'%"+k+"%'"
                    +" ORDER BY t.MaTL")) {
                while (rs.next())
                    md.addRow(new Object[]{rs.getInt(1),rs.getString(2),
                        rs.getString(3),rs.getString(4),rs.getString(5),
                        rs.getInt(6),rs.getInt(7)});
            } catch (Exception e) { e.printStackTrace(); }
        };
        bTim.addActionListener(e -> search.run());
        tFind.addActionListener(e -> search.run());
        if (!kw.isEmpty()) search.run();

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(lbl("Từ khóa:")); row.add(tFind); row.add(bTim);

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(title, BorderLayout.NORTH);
        north.add(row, BorderLayout.CENTER);

        p.add(north, BorderLayout.NORTH);
        p.add(sp,    BorderLayout.CENTER);
        return p;
    }

    public static void main(String[] args) {
        try { com.formdev.flatlaf.FlatLightLaf.setup(); }
        catch (Exception e) { e.printStackTrace(); }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}