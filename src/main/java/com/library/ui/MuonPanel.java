package com.library.ui;

import com.library.dao.DBConnection;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import static com.library.ui.UIConstants.*;

public class MuonPanel extends JPanel {

    private final String currentUser;
    private final DefaultTableModel mdList = new DefaultTableModel(
        new String[]{"ID","Số thẻ","Họ tên","Mã CB","Tên tài liệu","Kiểu mượn","Ngày mượn","Hạn trả","Người cho mượn"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    public MuonPanel(String currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 12));
        setBackground(C_BG); setBorder(pad(16));

        // ── NORTH: Tiêu đề + Form ────────────────────────────
        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);

        JLabel title = new JLabel("Mượn Tài Liệu");
        title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));
        north.add(title, BorderLayout.NORTH);

        // Form mượn
        JPanel grp = titledGroup("Thông tin mượn tài liệu");
        grp.setLayout(new GridBagLayout()); GridBagConstraints g = gbc();

        ButtonGroup bgKieu = new ButtonGroup();
        JRadioButton rbVe  = new JRadioButton("Mượn về", true);
        JRadioButton rbDoc = new JRadioButton("Mượn đọc tại chỗ");
        rbVe.setOpaque(false); rbDoc.setOpaque(false);
        rbVe.setFont(F_NORMAL); rbDoc.setFont(F_NORMAL);
        bgKieu.add(rbVe); bgKieu.add(rbDoc);

        JTextField tSoThe = tf(14), tMaCB = tf(12);
        tSoThe.putClientProperty("JTextField.placeholderText","VD: TV001");
        tMaCB.putClientProperty("JTextField.placeholderText","Mã cá biệt");

        JLabel lbHoTen=infoLabel(), lbDonVi=infoLabel();
        JLabel lbTenTL=infoLabel(), lbTinhTrang=infoLabel(), lbViTri=infoLabel();

        JDateChooser dcHan = new JDateChooser();
        dcHan.setDateFormatString("dd/MM/yyyy");
        dcHan.setPreferredSize(new Dimension(140, 32));
        dcHan.setDate(new Date(System.currentTimeMillis()+14L*86400000));

        int r=0;
        g.gridx=0;g.gridy=r;g.weightx=0; grp.add(lbl("Số thẻ độc giả *"),g);
        g.gridx=1;g.weightx=1; grp.add(tSoThe,g);
        g.gridx=2;g.weightx=0; grp.add(lbl("Mã cá biệt *"),g);
        g.gridx=3;g.weightx=1; grp.add(tMaCB,g);
        g.gridx=4;g.weightx=0; grp.add(lbl("Hình thức"),g);
        g.gridx=5;g.weightx=1;
        JPanel kp=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); kp.setOpaque(false); kp.add(rbVe); kp.add(rbDoc);
        grp.add(kp,g); r++;

        g.gridx=0;g.gridy=r;g.weightx=0; grp.add(lbl("Họ tên độc giả"),g);
        g.gridx=1;g.weightx=1; grp.add(lbHoTen,g);
        g.gridx=2;g.weightx=0; grp.add(lbl("Tên tài liệu"),g);
        g.gridx=3;g.weightx=1; grp.add(lbTenTL,g);
        g.gridx=4;g.weightx=0; grp.add(lbl("Tình trạng TL"),g);
        g.gridx=5;g.weightx=1; grp.add(lbTinhTrang,g); r++;

        g.gridx=0;g.gridy=r;g.weightx=0; grp.add(lbl("Đơn vị"),g);
        g.gridx=1;g.weightx=1; grp.add(lbDonVi,g);
        g.gridx=2;g.weightx=0; grp.add(lbl("Vị trí lưu trữ"),g);
        g.gridx=3;g.weightx=1; grp.add(lbViTri,g);
        g.gridx=4;g.weightx=0; grp.add(lbl("Hạn trả *"),g);
        g.gridx=5;g.weightx=1; grp.add(dcHan,g); r++;

        JButton bNew  = btn("Tạo mới",      C_WHITE, new Color(108,117,125));
        JButton bMuon = btn("Xác nhận mượn", C_WHITE, C_PRIMARY);
        JButton bIn   = btn("In phiếu",   C_WHITE, new Color(80,130,180));
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); bp.setOpaque(false);
        bp.add(bNew); bp.add(bIn); bp.add(bMuon);
        g.gridx=0;g.gridy=r;g.gridwidth=6; grp.add(bp,g);

        north.add(grp, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        // Auto fill
        tSoThe.addActionListener(e -> fillDocGia(tSoThe.getText().trim(), lbHoTen, lbDonVi));
        tMaCB.addActionListener(e  -> fillTaiLieu(tMaCB.getText().trim(), lbTenTL, lbTinhTrang, lbViTri));

        bNew.addActionListener(e -> {
            tSoThe.setText(""); tMaCB.setText("");
            lbHoTen.setText("—"); lbTenTL.setText("—");
            lbTinhTrang.setText(""); lbViTri.setText(""); lbDonVi.setText("");
        });
        bMuon.addActionListener(e -> doMuon(tSoThe, tMaCB, dcHan, rbVe));

        bIn.addActionListener(e -> {
            if(tSoThe.getText().trim().isEmpty()||tMaCB.getText().trim().isEmpty()){
                javax.swing.JOptionPane.showMessageDialog(null,"Nhập số thẻ và mã cá biệt trước khi in!");
                return;
            }
            java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
            java.awt.Frame frame = (win instanceof java.awt.Frame)?(java.awt.Frame)win:null;
            PdfExporter.xuatPhieuMuon(
                MuonPanel.this,
                tSoThe.getText().trim(),
                lbHoTen.getText(),
                lbDonVi.getText(),
                tMaCB.getText().trim(),
                lbTenTL.getText(),
                lbViTri.getText(),
                rbVe.isSelected()?"Mượn về":"Mượn đọc tại chỗ",
                dcHan.getDate()!=null?new java.text.SimpleDateFormat("dd/MM/yyyy").format(dcHan.getDate()):"",
                currentUser
            );
        });

        // ── CENTER: Bảng đang mượn ────────────────────────────
        JLabel lbList = new JLabel("Danh sách tài liệu đang mượn");
        lbList.setFont(new Font("Segoe UI",Font.BOLD,14));
        lbList.setForeground(new Color(30,41,59));

        JTable tb = styledTable(mdList);
        tb.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                JLabel lb=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if(!sel){try{Date han=new SimpleDateFormat("dd/MM/yyyy").parse(v!=null?v.toString():"01/01/2099");
                    lb.setForeground(han.before(new Date())?C_DANGER:Color.BLACK);}catch(Exception ignored){}}
                return lb;
            }
        });
        loadList();

        JScrollPane sp=new JScrollPane(tb); sp.setBorder(BorderFactory.createLineBorder(C_BORDER));

        JPanel center = new JPanel(new BorderLayout(0,8)); center.setOpaque(false);
        center.add(lbList, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void fillDocGia(String soThe, JLabel lbHoTen, JLabel lbDV) {
        if(soThe.isEmpty()) return;
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(
            "SELECT d.HoTen,dv.TenDV FROM DocGia d LEFT JOIN Lop l ON d.MaLop=l.MaLop LEFT JOIN DonVi dv ON l.MaDV=dv.MaDV WHERE d.SoThe=?")){
            ps.setString(1,soThe); ResultSet rs=ps.executeQuery();
            if(rs.next()){ lbHoTen.setText(rs.getString(1)!=null?rs.getString(1):"Không tìm thấy"); lbDV.setText(rs.getString(2)!=null?rs.getString(2):""); }
            else lbHoTen.setText("Không tìm thấy số thẻ này");
        }catch(Exception ex){ex.printStackTrace();}
    }

    private void fillTaiLieu(String maCB, JLabel lbTenTL, JLabel lbTT, JLabel lbVT) {
        if(maCB.isEmpty()) return;
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(
            "SELECT tl.TenTL,tc.TinhTrang,v.TenVT FROM TLChitiet tc JOIN TaiLieu tl ON tc.MaTL=tl.MaTL LEFT JOIN VitriTL v ON tl.MaVT=v.MaVT WHERE tc.MaCaBiet=?")){
            ps.setString(1,maCB); ResultSet rs=ps.executeQuery();
            if(rs.next()){ lbTenTL.setText(rs.getString(1)!=null?rs.getString(1):""); lbTT.setText(rs.getString(2)!=null?rs.getString(2):""); lbVT.setText(rs.getString(3)!=null?rs.getString(3):""); }
            else lbTenTL.setText("Không tìm thấy mã cá biệt");
        }catch(Exception ex){ex.printStackTrace();}
    }

    private void doMuon(JTextField tSoThe, JTextField tMaCB, JDateChooser dcHan, JRadioButton rbVe) {
        String soThe=tSoThe.getText().trim(), maCB=tMaCB.getText().trim();
        if(soThe.isEmpty()){msg("Nhập số thẻ độc giả!"); return;}
        if(maCB.isEmpty()){msg("Nhập mã cá biệt!"); return;}
        if(dcHan.getDate()==null){msg("Chọn hạn trả!"); return;}
        try(Connection c=DBConnection.getConnection()){
            PreparedStatement ps=c.prepareStatement("SELECT TinhTrang FROM DocGia WHERE SoThe=?");
            ps.setString(1,soThe); ResultSet rs=ps.executeQuery();
            if(!rs.next()){msg("Số thẻ không tồn tại!"); return;}
            if("Khoa".equals(rs.getString(1))){msg("Thẻ này đã bị khóa!"); return;}
            rs=c.createStatement().executeQuery("SELECT MaCaBiet FROM TLChitiet WHERE MaCaBiet="+maCB);
            if(!rs.next()){msg("Mã cá biệt không tồn tại!"); return;}
            rs=c.createStatement().executeQuery("SELECT ID_MuonTra FROM MuonTra WHERE MaCaBiet="+maCB+" AND NgayTra IS NULL");
            if(rs.next()){msg("Tài liệu đang được mượn bởi người khác!"); return;}
            if(dcHan.getDate().before(new Date())){msg("Hạn trả phải từ ngày mai trở đi!"); return;}
            ps=c.prepareStatement("INSERT INTO MuonTra(SoThe,MaCaBiet,KieuMuon,NgayMuon,NguoiChoMuon,HanTra,Loai) VALUES(?,?,?,GETDATE(),?,?,'Muon')");
            ps.setString(1,soThe); ps.setInt(2,Integer.parseInt(maCB));
            ps.setString(3,rbVe.isSelected()?"Mượn về":"Mượn đọc");
            ps.setString(4,currentUser);
            ps.setDate(5,new java.sql.Date(dcHan.getDate().getTime()));
            ps.executeUpdate(); msg("Lập phiếu mượn thành công!"); loadList();
        }catch(Exception ex){msg("Lỗi: "+ex.getMessage());}
    }

    private void loadList() {
        mdList.setRowCount(0); SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
        try(Connection c=DBConnection.getConnection(); ResultSet rs=c.createStatement().executeQuery(
            "SELECT TOP 50 m.*,d.HoTen,tl.TenTL FROM MuonTra m"
            +" LEFT JOIN DocGia d ON m.SoThe=d.SoThe"
            +" LEFT JOIN TLChitiet tc ON m.MaCaBiet=tc.MaCaBiet"
            +" LEFT JOIN TaiLieu tl ON tc.MaTL=tl.MaTL"
            +" WHERE m.NgayTra IS NULL ORDER BY m.HanTra, m.ID_MuonTra DESC")){
            while(rs.next())
                mdList.addRow(new Object[]{rs.getInt("ID_MuonTra"),rs.getString("SoThe"),rs.getString("HoTen"),
                    rs.getInt("MaCaBiet"),rs.getString("TenTL"),rs.getString("KieuMuon"),
                    fmt(sdf,rs.getDate("NgayMuon")),fmt(sdf,rs.getDate("HanTra")),rs.getString("NguoiChoMuon")});
        }catch(Exception e){e.printStackTrace();}
    }

    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
}