package com.library.ui;

import com.library.dao.DBConnection;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import static com.library.ui.UIConstants.*;

public class TaiLieuPanel extends JPanel {

    private final String currentUser;

    public TaiLieuPanel(String currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(C_BG);

        JTabbedPane sub = new JTabbedPane();
        sub.setFont(F_BOLD);
        sub.addTab("  Nhập tài liệu  ",  buildNhapTab());
        sub.addTab("  Danh sách tài liệu  ", buildDanhSachTab());
        add(sub, BorderLayout.CENTER);
    }

    private JPanel buildNhapTab() {
        JPanel wrap = new JPanel(new BorderLayout(0, 14));
        wrap.setBackground(C_BG); wrap.setBorder(pad(18));

        // Nhóm bắt buộc
        JPanel grpBB = titledGroup("Thông tin yêu cầu bắt buộc");
        JTextField tTen = tf(24);
        tTen.putClientProperty("JTextField.placeholderText","Tên tài liệu / sách...");
        JComboBox<String> cboTL=new JComboBox<>(), cboDV=new JComboBox<>();
        loadCombo(cboTL,"SELECT TenTheLoai FROM TheLoai ORDER BY TenTheLoai");
        loadCombo(cboDV,"SELECT TenDV FROM DonVi ORDER BY TenDV");
        grpBB.setLayout(new FlowLayout(FlowLayout.LEFT,12,8));
        grpBB.add(lbl("Tên tài liệu *")); grpBB.add(tTen);
        grpBB.add(lbl("Thể loại"));       grpBB.add(cboTL);
        grpBB.add(lbl("Ngành/Khoa"));     grpBB.add(cboDV);

        // Nhóm bổ sung
        JPanel grpBS = titledGroup("Thông tin bổ sung");
        JComboBox<String> cboTG=new JComboBox<>(), cboNXB=new JComboBox<>(),
                          cboNN=new JComboBox<>(),  cboVT=new JComboBox<>();
        loadCombo(cboTG, "SELECT TenTG FROM TacGia ORDER BY TenTG");
        loadCombo(cboNXB,"SELECT TenNXB FROM NhaXuatBan ORDER BY TenNXB");
        loadCombo(cboNN, "SELECT TenNgonNgu FROM NgonNgu ORDER BY TenNgonNgu");
        // Load VitriTL phan cap: Tang truoc, Ke con thu le
        try (java.sql.Connection cnn = com.library.dao.DBConnection.getConnection()) {
            // Load tang (cha)
            java.sql.ResultSet rsTang = cnn.createStatement().executeQuery(
                "SELECT MaVT,TenVT FROM VitriTL WHERE ChaVT IS NULL ORDER BY TenVT");
            while (rsTang.next()) {
                cboVT.addItem(rsTang.getString("TenVT")); // Tang 1, Tang 2
                // Load ke con cua tang nay
                java.sql.PreparedStatement ps2 = cnn.prepareStatement(
                    "SELECT TenVT FROM VitriTL WHERE ChaVT=? ORDER BY TenVT");
                ps2.setInt(1, rsTang.getInt("MaVT"));
                java.sql.ResultSet rsKe = ps2.executeQuery();
                while (rsKe.next()) {
                    cboVT.addItem("  ↳ " + rsKe.getString("TenVT")); // ↳ Ke A1, Ke B2...
                }
            }
        } catch (Exception ignored) {}

        JTextField tNam=tf(6), tLan=tf(4), tSoTrang=tf(5), tKhoGiay=tf(6), tGia=tf(10), tSoPH=tf(5);
        tLan.setText("1"); tSoPH.setText("1"); tNam.putClientProperty("JTextField.placeholderText","2024");
        JTextArea tNoiDung=new JTextArea(3,28); tNoiDung.setFont(F_NORMAL); tNoiDung.setLineWrap(true);
        JDateChooser dcNgayPH=new JDateChooser(); dcNgayPH.setDateFormatString("dd/MM/yyyy"); dcNgayPH.setPreferredSize(new Dimension(140,32));

        grpBS.setLayout(new GridBagLayout()); GridBagConstraints g=gbc(); int row=0;
        addRow4(grpBS,g,row++,"Tác giả",cboTG,"Nhà XB",cboNXB,"Năm XB",tNam,"Lần tái bản",tLan);
        addRow4(grpBS,g,row++,"Ngôn ngữ",cboNN,"Số trang",tSoTrang,"Khổ giấy",tKhoGiay,"Giá bìa (đ)",tGia);
        g.gridx=0;g.gridy=row;g.gridwidth=1;g.weightx=0; grpBS.add(lbl("Nội dung tóm tắt"),g);
        g.gridx=1;g.gridy=row;g.gridwidth=5;g.weightx=1; grpBS.add(new JScrollPane(tNoiDung),g);
        g.gridx=6;g.gridy=row;g.gridwidth=1;g.weightx=0; grpBS.add(lbl("Số phát hành"),g);
        g.gridx=7;g.gridy=row;g.gridwidth=1;g.weightx=0; grpBS.add(tSoPH,g); row++;
        g.gridx=0;g.gridy=row;g.gridwidth=1;g.weightx=0; grpBS.add(lbl("Vị trí lưu trữ"),g);
        g.gridx=1;g.gridy=row;g.gridwidth=3;g.weightx=1; grpBS.add(cboVT,g);
        g.gridx=6;g.gridy=row;g.gridwidth=1;g.weightx=0; grpBS.add(lbl("Ngày phát hành"),g);
        g.gridx=7;g.gridy=row;g.gridwidth=1;g.weightx=0; grpBS.add(dcNgayPH,g); row++;

        JButton bNew=btn("Tạo mới",C_WHITE,new Color(108,117,125));
        JButton bAdd=btn("Thêm tài liệu",C_WHITE,C_SUCCESS);
        JPanel btnP=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btnP.setOpaque(false); btnP.add(bNew); btnP.add(bAdd);
        g.gridx=0;g.gridy=row;g.gridwidth=8; grpBS.add(btnP,g);

        bNew.addActionListener(e -> {
            tTen.setText(""); tNam.setText(""); tSoTrang.setText(""); tGia.setText("");
            tLan.setText("1"); tSoPH.setText("1"); tNoiDung.setText(""); tKhoGiay.setText("");
        });
        bAdd.addActionListener(e -> save(tTen,cboTL,cboDV,cboTG,cboNXB,tNam,cboNN,tNoiDung,tSoTrang,tKhoGiay,tLan,tGia,tSoPH,dcNgayPH,cboVT));

        wrap.add(grpBB, BorderLayout.NORTH);
        wrap.add(grpBS, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildDanhSachTab() {
        JPanel p=new JPanel(new BorderLayout(0,10)); p.setBackground(C_BG); p.setBorder(pad(14));

        JTextField txt=searchField("Tìm tên tài liệu, tác giả, thể loại...");
        JButton bTim=btn("Tìm kiếm",C_WHITE,C_PRIMARY);
        JButton bXoa=btn("Xóa",C_WHITE,C_DANGER);
        JLabel lbCount=new JLabel(""); lbCount.setFont(F_SMALL); lbCount.setForeground(C_TEXT_MUT);
        p.add(rowPanel(txt,bTim,bXoa,lbCount),BorderLayout.NORTH);

        DefaultTableModel md=new DefaultTableModel(
            new String[]{"Mã TL","Thể loại","Tên tài liệu","Tác giả","Nhà XB","Năm XB","Lần TB","Số trang","Giá bìa","Ngôn ngữ","Vị trí","Tổng số"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        JTable tb=styledTable(md);
        JScrollPane sp=new JScrollPane(tb); sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        p.add(sp,BorderLayout.CENTER);

        Runnable load=()->{ loadTable(md,txt.getText().trim()); lbCount.setText("Tìm thấy: "+md.getRowCount()+" tài liệu"); };
        load.run();
        bTim.addActionListener(e->load.run()); txt.addActionListener(e->load.run());
        txt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){SwingUtilities.invokeLater(load::run);}
            public void removeUpdate(javax.swing.event.DocumentEvent e){SwingUtilities.invokeLater(load::run);}
            public void changedUpdate(javax.swing.event.DocumentEvent e){}
        });
        bXoa.addActionListener(e->{
            int r=tb.getSelectedRow(); if(r<0){msg("Chọn tài liệu cần xóa!"); return;}
            if(confirm("Xóa tài liệu \""+md.getValueAt(r,2)+"\"?")){
                try(Connection c=DBConnection.getConnection()){
                    c.createStatement().executeUpdate("DELETE FROM TaiLieu WHERE MaTL="+(int)md.getValueAt(r,0));
                    msg("Xóa thành công!"); load.run();
                }catch(Exception ex){msg("Không thể xóa! Tài liệu có thể đang được mượn.");}
            }
        });
        return p;
    }

    private void loadTable(DefaultTableModel md, String kw) {
        md.setRowCount(0); DecimalFormat df=new DecimalFormat("#,###");
        try(Connection c=DBConnection.getConnection(); ResultSet rs=c.createStatement().executeQuery(
            "SELECT t.MaTL,tl.TenTheLoai,t.TenTL,tg.TenTG,n.TenNXB,t.NamXB,t.LanTB,t.SoTrang,t.GiaBia,nn.TenNgonNgu,v.TenVT,t.TongSo"
            +" FROM TaiLieu t LEFT JOIN TheLoai tl ON t.MaTheLoai=tl.MaTheLoai"
            +" LEFT JOIN TacGia tg ON t.MaTG=tg.MaTG LEFT JOIN NhaXuatBan n ON t.MaNXB=n.MaNXB"
            +" LEFT JOIN NgonNgu nn ON t.MaNgonNgu=nn.MaNgonNgu LEFT JOIN VitriTL v ON t.MaVT=v.MaVT"
            +" ORDER BY t.MaTL")){
            while(rs.next()){
                String tenTL=rs.getString(3)!=null?rs.getString(3):"";
                String tacGia=rs.getString(4)!=null?rs.getString(4):"";
                String theLoai=rs.getString(2)!=null?rs.getString(2):"";
                if(!kw.isEmpty()&&!matchSearch(tenTL,kw)&&!matchSearch(tacGia,kw)&&!matchSearch(theLoai,kw)) continue;
                md.addRow(new Object[]{rs.getInt(1),theLoai,tenTL,tacGia,rs.getString(5),
                    rs.getInt(6),rs.getInt(7),rs.getInt(8),df.format(rs.getDouble(9))+"đ",
                    rs.getString(10),rs.getString(11),rs.getInt(12)});
            }
        }catch(Exception e){e.printStackTrace();}
    }

    private void save(JTextField tTen,JComboBox<String> cboTL,JComboBox<String> cboDV,
            JComboBox<String> cboTG,JComboBox<String> cboNXB,JTextField tNam,
            JComboBox<String> cboNN,JTextArea tND,JTextField tSP,JTextField tKG,
            JTextField tLan,JTextField tGia,JTextField tSoPH,JDateChooser dcNgayPH,JComboBox<String> cboVT){
        if(tTen.getText().trim().isEmpty()){msg("Vui lòng nhập tên tài liệu!"); return;}
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(
            "INSERT INTO TaiLieu(TenTL,MaTheLoai,MaDV,MaTG,MaNXB,NamXB,MaNgonNgu,NoiDung,SoTrang,KhoGiay,LanTB,GiaBia,SoPH,NgayPH,TongSo,MaVT,NgayCN)"
            +" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,1,?,GETDATE())")){
            ps.setString(1,tTen.getText().trim());
            ps.setObject(2,comboId(c,"TheLoai","MaTheLoai","TenTheLoai",cboTL));
            ps.setObject(3,comboId(c,"DonVi","MaDV","TenDV",cboDV));
            ps.setObject(4,comboId(c,"TacGia","MaTG","TenTG",cboTG));
            ps.setObject(5,comboId(c,"NhaXuatBan","MaNXB","TenNXB",cboNXB));
            ps.setObject(6,parseInt(tNam.getText(),0));
            ps.setObject(7,comboId(c,"NgonNgu","MaNgonNgu","TenNgonNgu",cboNN));
            ps.setString(8,tND.getText().trim());
            ps.setInt(9,parseInt(tSP.getText(),0)); ps.setString(10,tKG.getText().trim());
            ps.setInt(11,parseInt(tLan.getText(),1)); ps.setDouble(12,parseDouble(tGia.getText(),0));
            ps.setInt(13,parseInt(tSoPH.getText(),1));
            ps.setObject(14,dcNgayPH.getDate()!=null?new java.sql.Date(dcNgayPH.getDate().getTime()):null);
            ps.setObject(15,comboId(c,"VitriTL","MaVT","TenVT",cboVT));
            ps.executeUpdate(); msg("Thêm tài liệu thành công!");
        }catch(Exception e){msg("Lỗi: "+e.getMessage());}
    }

    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
    private boolean confirm(String s){return JOptionPane.showConfirmDialog(this,s,"Xác nhận",JOptionPane.YES_NO_OPTION)==0;}
}