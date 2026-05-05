package com.library.ui;

import com.library.dao.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import static com.library.ui.UIConstants.*;

public class DashPanel extends JPanel {

    private final JLabel lSach = new JLabel("0");
    private final JLabel lDG   = new JLabel("0");
    private final JLabel lMuon = new JLabel("0");
    private final JLabel lQH   = new JLabel("0");
    private final DefaultTableModel mdRecent = new DefaultTableModel(
        new String[]{"Mã","Số thẻ","Họ tên","Tài liệu","Ngày mượn","Hạn trả","Trạng thái"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    public DashPanel(String user, String role) {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_BG);
        setBorder(pad(20));

        // ── NORTH: Tiêu đề ───────────────────────────────────
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Tổng Quan Thư Viện");
        title.setFont(F_TITLE);
        title.setForeground(new Color(30, 41, 59));

        JLabel sub = new JLabel("Chào mừng, " + user + "  ·  "
            + new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        sub.setFont(F_SMALL);
        sub.setForeground(C_TEXT_MUT);

        JPanel titleText = new JPanel(new GridLayout(2, 1, 0, 2));
        titleText.setOpaque(false);
        titleText.add(title);
        titleText.add(sub);

        JButton bRef = btn("Làm mới", C_WHITE, C_PRIMARY);
        bRef.addActionListener(e -> loadData());

        north.add(titleText, BorderLayout.WEST);
        north.add(bRef, BorderLayout.EAST);
        add(north, BorderLayout.NORTH);

        // ── CENTER: Toàn bộ nội dung ──────────────────────────
        // Dùng GridBagLayout để kiểm soát chính xác kích thước
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1.0;
        gc.gridx = 0;

        // Row 0: 4 stat cards — chiều cao cố định 110px
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.add(statCard("Tổng tài liệu", lSach, C_PRIMARY));
        cards.add(statCard("Độc giả",        lDG,   C_SUCCESS));
        cards.add(statCard("Đang mượn",      lMuon, C_WARNING));
        cards.add(statCard("Quá hạn",        lQH,   C_DANGER));

        gc.gridy = 0;
        gc.weighty = 0;
        gc.ipady = 0;
        gc.insets = new Insets(0, 0, 18, 0);
        cards.setPreferredSize(new Dimension(0, 90));
        center.add(cards, gc);

        // Row 1: Label "Phiếu mượn gần đây"
        JLabel lbRecent = new JLabel("Phiếu mượn gần đây");
        lbRecent.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbRecent.setForeground(new Color(30, 41, 59));

        gc.gridy = 1;
        gc.weighty = 0;
        gc.ipady = 0;
        gc.insets = new Insets(0, 0, 8, 0);
        center.add(lbRecent, gc);

        // Row 2: Bảng mượn gần đây — chiếm phần còn lại
        JTable tb = styledTable(mdRecent);
        tb.getColumnModel().getColumn(6).setCellRenderer(statusRenderer());
        JScrollPane sp = new JScrollPane(tb);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER));

        gc.gridy = 2;
        gc.weighty = 1.0;
        gc.ipady = 0;
        gc.insets = new Insets(0, 0, 0, 0);
        center.add(sp, gc);

        add(center, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        new SwingWorker<Void, Void>() {
            int s, d, m, q;
            java.util.List<Object[]> rows = new ArrayList<>();

            protected Void doInBackground() {
                try (Connection c = DBConnection.getConnection()) {
                    ResultSet rs;
                    rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM TaiLieu"); if(rs.next())s=rs.getInt(1);
                    rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM DocGia"); if(rs.next())d=rs.getInt(1);
                    rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM MuonTra WHERE NgayTra IS NULL"); if(rs.next())m=rs.getInt(1);
                    rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM MuonTra WHERE NgayTra IS NULL AND HanTra<GETDATE()"); if(rs.next())q=rs.getInt(1);
                    rs=c.createStatement().executeQuery(
                        "SELECT TOP 10 m.ID_MuonTra,m.SoThe,d.HoTen,tl.TenTL,m.NgayMuon,m.HanTra,m.NgayTra"
                        +" FROM MuonTra m LEFT JOIN DocGia d ON m.SoThe=d.SoThe"
                        +" LEFT JOIN TLChitiet tc ON m.MaCaBiet=tc.MaCaBiet"
                        +" LEFT JOIN TaiLieu tl ON tc.MaTL=tl.MaTL ORDER BY m.ID_MuonTra DESC");
                    SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
                    while(rs.next()){
                        String tt=rs.getDate("NgayTra")!=null?"Đã trả"
                            :(rs.getDate("HanTra")!=null&&rs.getDate("HanTra").before(new Date())?"Quá hạn":"Đang mượn");
                        rows.add(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),
                            rs.getString(4),fmt(sdf,rs.getDate(5)),fmt(sdf,rs.getDate(6)),tt});
                    }
                } catch(Exception e){ e.printStackTrace(); }
                return null;
            }

            protected void done() {
                lSach.setText(s+""); lDG.setText(d+""); lMuon.setText(m+""); lQH.setText(q+"");
                mdRecent.setRowCount(0);
                for(Object[] row:rows) mdRecent.addRow(row);
            }
        }.execute();
    }
}