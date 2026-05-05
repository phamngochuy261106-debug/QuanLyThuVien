package com.library.ui;

import com.library.dao.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.List;
import static com.library.ui.UIConstants.*;

// ── Phieu Nhap Panel ────────────────────────────────────────────
class PhieuNhapPanel extends JPanel {
    private final String currentUser;
    private final DefaultTableModel md = new DefaultTableModel(
        new String[]{"Số phiếu","Tài liệu","Thể loại","Nhà cung cấp","Người nhập","Ngày nhập","Số lượng"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    PhieuNhapPanel(String currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 12)); setBackground(C_BG); setBorder(pad(16));

        JLabel title = new JLabel("Phiếu Nhập Tài Liệu");
        title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));
        add(title, BorderLayout.NORTH);

        JPanel grp = titledGroup("Thêm phiếu nhập");
        grp.setLayout(new FlowLayout(FlowLayout.LEFT,12,8));
        JComboBox<String> cboTL=new JComboBox<>(), cboNCC=new JComboBox<>();
        loadCombo(cboTL,"SELECT TenTL FROM TaiLieu ORDER BY TenTL");
        loadCombo(cboNCC,"SELECT TenNCC FROM NhaCungCap ORDER BY TenNCC");
        JTextField tSL=tf(6); tSL.setText("1");
        JButton bAdd=btn("Thêm phiếu nhập",C_WHITE,C_SUCCESS);
        grp.add(lbl("Tài liệu *")); grp.add(cboTL);
        grp.add(lbl("Số lượng")); grp.add(tSL);
        grp.add(lbl("Nhà cung cấp")); grp.add(cboNCC);
        grp.add(bAdd);

        JTable tb=styledTable(md);
        JScrollPane sp=new JScrollPane(tb); sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        Runnable load=()->loadData();
        load.run();

        bAdd.addActionListener(e->{
            if(cboTL.getSelectedIndex()<0){msg("Chọn tài liệu!"); return;}
            try(Connection c=DBConnection.getConnection()){
                Integer maTL=comboId(c,"TaiLieu","MaTL","TenTL",cboTL);
                Integer maNCC=comboId(c,"NhaCungCap","MaNCC","TenNCC",cboNCC);
                if(maTL==null){msg("Chọn tài liệu!"); return;}
                PreparedStatement ps=c.prepareStatement("INSERT INTO PhieuNhap(MaTL,MaNCC,NguoiNhap,NgayNhap,SoLuong) VALUES(?,?,?,GETDATE(),?)");
                ps.setInt(1,maTL); ps.setObject(2,maNCC); ps.setString(3,currentUser); ps.setInt(4,parseInt(tSL.getText(),1));
                ps.executeUpdate(); msg("Thêm phiếu nhập thành công!"); load.run();
            }catch(Exception ex){msg("Lỗi: "+ex.getMessage());}
        });

        JLabel lbList=new JLabel("Danh sách phiếu nhập"); lbList.setFont(new Font("Segoe UI",Font.BOLD,14));
        JPanel bot=new JPanel(new BorderLayout(0,8)); bot.setOpaque(false);
        bot.add(lbList,BorderLayout.NORTH); bot.add(sp,BorderLayout.CENTER);
        add(grp,BorderLayout.CENTER); add(bot,BorderLayout.SOUTH);
    }

    private void loadData(){
        md.setRowCount(0); SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
        try(Connection c=DBConnection.getConnection(); ResultSet rs=c.createStatement().executeQuery(
            "SELECT pn.ID_Nhap,tl.TenTL,tl2.TenTheLoai,nc.TenNCC,pn.NguoiNhap,pn.NgayNhap,pn.SoLuong FROM PhieuNhap pn"
            +" LEFT JOIN TaiLieu tl ON pn.MaTL=tl.MaTL LEFT JOIN TheLoai tl2 ON tl.MaTheLoai=tl2.MaTheLoai"
            +" LEFT JOIN NhaCungCap nc ON pn.MaNCC=nc.MaNCC ORDER BY pn.ID_Nhap DESC")){
            while(rs.next()) md.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),fmt(sdf,rs.getDate(6)),rs.getInt(7)});
        }catch(Exception e){e.printStackTrace();}
    }
    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
}

// ── Vi Pham Panel ───────────────────────────────────────────────
class ViPhamPanel extends JPanel {
    private final String currentUser;
    private final DefaultTableModel md = new DefaultTableModel(
        new String[]{"Mã","Độc giả","Lý do vi phạm","Hình thức xử lý","Số tiền (đ)","Ngày XL","Người XL","Ghi chú"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    ViPhamPanel(String currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0,12)); setBackground(C_BG); setBorder(pad(16));

        JLabel title=new JLabel("Xử Lý Vi Phạm"); title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));
        JButton bThem=btn("+ Thêm phiếu phạt",C_WHITE,C_PRIMARY);
        JButton bXoa =btn("Xóa",C_WHITE,C_DANGER);
        JPanel topRow=new JPanel(new BorderLayout()); topRow.setOpaque(false);
        topRow.add(title,BorderLayout.WEST);
        topRow.add(rowPanel(bThem,bXoa),BorderLayout.EAST);
        add(topRow,BorderLayout.NORTH);

        JTable tb=styledTable(md);
        JScrollPane sp=new JScrollPane(tb); sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        Runnable load=()->loadData();
        load.run();

        bThem.addActionListener(e->dlgThem(load));
        bXoa.addActionListener(e->{
            int r=tb.getSelectedRow(); if(r<0){msg("Chọn phiếu phạt cần xóa!"); return;}
            if(confirm("Xóa phiếu phạt này?")){
                try(Connection c=DBConnection.getConnection()){
                    c.createStatement().executeUpdate("DELETE FROM XuLyViPham WHERE ID_Phat="+(int)md.getValueAt(r,0));
                    msg("Xóa thành công!"); load.run();
                }catch(Exception ex){msg("Lỗi!");}
            }
        });
        add(sp,BorderLayout.CENTER);
    }

    private void loadData(){
        md.setRowCount(0); SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy"); DecimalFormat df=new DecimalFormat("#,###");
        try(Connection c=DBConnection.getConnection(); ResultSet rs=c.createStatement().executeQuery(
            "SELECT x.*,d.HoTen FROM XuLyViPham x LEFT JOIN DocGia d ON x.MaDG=d.MaDG ORDER BY x.ID_Phat DESC")){
            while(rs.next()) md.addRow(new Object[]{rs.getInt("ID_Phat"),rs.getString("HoTen"),rs.getString("LyDoVP"),
                rs.getString("HTXuLy"),df.format(rs.getDouble("SoTien")),fmt(sdf,rs.getDate("NgayXL")),rs.getString("NguoiXL"),rs.getString("GhiChu")});
        }catch(Exception e){e.printStackTrace();}
    }

    private void dlgThem(Runnable reload){
        JDialog d=new JDialog((JFrame)SwingUtilities.getWindowAncestor(this),"Thêm phiếu phạt",true);
        d.setSize(500,340); d.setLocationRelativeTo(this);
        JPanel f=formPanel(); GridBagConstraints g=gbc();
        JComboBox<String> cboDG=new JComboBox<>(),cboPM=new JComboBox<>();
        JComboBox<String> cboLD=new JComboBox<>(new String[]{"Trả muộn","Làm mất tài liệu","Hư hỏng tài liệu","Khác"});
        JComboBox<String> cboHT=new JComboBox<>(new String[]{"Phạt tiền","Bồi thường tài liệu","Khóa thẻ","Cảnh cáo"});
        JTextField tTien=tf(12),tGC=tf(22);
        tTien.setText("0"); tTien.putClientProperty("JTextField.placeholderText","Số tiền phạt");
        List<Integer> dgIds=new ArrayList<>(),pmIds=new ArrayList<>();
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery("SELECT MaDG,HoTen FROM DocGia ORDER BY HoTen");
            while(rs.next()){dgIds.add(rs.getInt(1));cboDG.addItem(rs.getString(2));}
            rs=c.createStatement().executeQuery("SELECT m.ID_MuonTra,d.HoTen,tl.TenTL FROM MuonTra m LEFT JOIN DocGia d ON m.SoThe=d.SoThe LEFT JOIN TLChitiet tc ON m.MaCaBiet=tc.MaCaBiet LEFT JOIN TaiLieu tl ON tc.MaTL=tl.MaTL WHERE m.NgayTra IS NULL ORDER BY m.ID_MuonTra DESC");
            while(rs.next()){pmIds.add(rs.getInt(1));cboPM.addItem("MT"+rs.getInt(1)+" – "+rs.getString(2)+" – "+rs.getString(3));}
        }catch(Exception ignored){}
        int r=0;
        fRow(f,g,r++,"Độc giả",cboDG); fRow(f,g,r++,"Phiếu mượn liên quan",cboPM);
        fRow(f,g,r++,"Lý do vi phạm",cboLD); fRow(f,g,r++,"Hình thức xử lý",cboHT);
        fRow(f,g,r++,"Số tiền phạt",tTien); fRow(f,g,r++,"Ghi chú",tGC);
        JPanel bp=btnPanel(btn("Lưu",C_WHITE,C_PRIMARY),btn("Hủy",Color.BLACK,new Color(229,231,235)));
        g.gridy=r;g.gridx=0;g.gridwidth=2; f.add(bp,g);
        ((JButton)bp.getComponent(0)).addActionListener(e->{
            double tien=0; try{tien=Double.parseDouble(tTien.getText().replace(",","").trim());}catch(Exception x){}
            try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(
                "INSERT INTO XuLyViPham(ID_MuonTra,MaDG,LyDoVP,HTXuLy,SoTien,NguoiXL,GhiChu) VALUES(?,?,?,?,?,?,?)")){
                ps.setObject(1,cboPM.getSelectedIndex()>=0?pmIds.get(cboPM.getSelectedIndex()):null);
                ps.setObject(2,cboDG.getSelectedIndex()>=0?dgIds.get(cboDG.getSelectedIndex()):null);
                ps.setString(3,(String)cboLD.getSelectedItem()); ps.setString(4,(String)cboHT.getSelectedItem());
                ps.setDouble(5,tien); ps.setString(6,currentUser); ps.setString(7,tGC.getText().trim());
                ps.executeUpdate(); msg("Thêm thành công!"); d.dispose(); reload.run();
            }catch(Exception ex){msg("Lỗi: "+ex.getMessage());}
        });
        ((JButton)bp.getComponent(1)).addActionListener(e->d.dispose());
        d.setContentPane(new JScrollPane(f)); d.setVisible(true);
    }
    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
    private boolean confirm(String s){return JOptionPane.showConfirmDialog(this,s,"Xác nhận",JOptionPane.YES_NO_OPTION)==0;}
}

// ── Qua Han Panel ───────────────────────────────────────────────
class QuaHanPanel extends JPanel {
    private final DefaultTableModel md = new DefaultTableModel(
        new String[]{"ID","Số thẻ","Họ tên","SDT","Tên tài liệu","Ngày mượn","Hạn trả","Số ngày quá hạn"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    QuaHanPanel() {
        setLayout(new BorderLayout(0,12)); setBackground(C_BG); setBorder(pad(16));

        JLabel title=new JLabel("Độc Giả Mượn Quá Hạn"); title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));
        JButton bRef=btn("Làm mới",C_WHITE,C_PRIMARY);
        JPanel topRow=new JPanel(new BorderLayout()); topRow.setOpaque(false);
        topRow.add(title,BorderLayout.WEST); topRow.add(bRef,BorderLayout.EAST);
        add(topRow,BorderLayout.NORTH);

        JTable tb=styledTable(md);
        // Tô đỏ tất cả dòng
        tb.setDefaultRenderer(Object.class,new javax.swing.table.DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                Component comp=super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if(!sel){ comp.setBackground(r%2==0?new Color(255,245,245):new Color(255,237,237)); comp.setForeground(new Color(120,0,0)); }
                return comp;
            }
        });
        // Cột số ngày in đậm
        tb.getColumnModel().getColumn(7).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                JLabel lb=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if(!sel){lb.setBackground(r%2==0?new Color(255,245,245):new Color(255,237,237));lb.setForeground(C_DANGER);lb.setFont(F_BOLD);}
                return lb;
            }
        });

        JScrollPane sp=new JScrollPane(tb); sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        Runnable load=()->loadData();
        load.run(); bRef.addActionListener(e->load.run());

        // Nut In phieu phat
        JButton bIn=btn("In phiếu phạt",Color.WHITE,new Color(180,60,60));
        bIn.setPreferredSize(new java.awt.Dimension(140,32));
        bIn.addActionListener(e->{
            int r=tb.getSelectedRow(); if(r<0){JOptionPane.showMessageDialog(null,"Chọn dòng cần in!"); return;}
            String hanTra=md.getValueAt(r,6)!=null?md.getValueAt(r,6).toString():"";
            int soNgay=0;
            try{java.util.Date han=new java.text.SimpleDateFormat("dd/MM/yyyy").parse(hanTra);
                soNgay=(int)((new java.util.Date().getTime()-han.getTime())/(86400000L));}catch(Exception ignored){}
            PdfExporter.xuatPhieuPhat(QuaHanPanel.this,
                md.getValueAt(r,1)!=null?md.getValueAt(r,1).toString():"",
                md.getValueAt(r,2)!=null?md.getValueAt(r,2).toString():"",
                md.getValueAt(r,4)!=null?md.getValueAt(r,4).toString():"",
                hanTra, soNgay, soNgay*2000.0, "Phạt tiền trễ hạn");
        });
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        btnRow.setOpaque(false); btnRow.add(bIn);
        topRow.add(btnRow,BorderLayout.CENTER);
        add(sp,BorderLayout.CENTER);
    }

    private void loadData(){
        md.setRowCount(0); SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
        try(Connection c=DBConnection.getConnection(); ResultSet rs=c.createStatement().executeQuery(
            "SELECT m.ID_MuonTra,m.SoThe,d.HoTen,d.SDT,tl.TenTL,m.NgayMuon,m.HanTra,DATEDIFF(day,m.HanTra,GETDATE()) AS SoNgayQH"
            +" FROM MuonTra m LEFT JOIN DocGia d ON m.SoThe=d.SoThe LEFT JOIN TLChitiet tc ON m.MaCaBiet=tc.MaCaBiet"
            +" LEFT JOIN TaiLieu tl ON tc.MaTL=tl.MaTL WHERE m.NgayTra IS NULL AND m.HanTra<GETDATE() ORDER BY m.HanTra")){
            while(rs.next()) md.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                rs.getString(5),fmt(sdf,rs.getDate(6)),fmt(sdf,rs.getDate(7)),rs.getInt(8)+" ngày"});
        }catch(Exception e){e.printStackTrace();}
    }
}

// ── Thong Ke Panel ──────────────────────────────────────────────
class ThongKePanel extends JPanel {
    ThongKePanel() {
        setLayout(new BorderLayout(0,0)); setBackground(C_BG); setBorder(pad(16));

        // NORTH: tiêu đề
        JPanel north = new JPanel(new BorderLayout()); north.setOpaque(false);
        north.setBorder(BorderFactory.createEmptyBorder(0,0,14,0));
        JLabel title=new JLabel("Thống Kê & Báo Cáo"); title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));
        JButton bRef=btn("Làm mới",C_WHITE,C_PRIMARY);
        north.add(title,BorderLayout.WEST); north.add(bRef,BorderLayout.EAST);
        add(north, BorderLayout.NORTH);

        // CENTER: GridBagLayout xếp dọc
        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill=GridBagConstraints.BOTH; gc.weightx=1.0; gc.gridx=0;

        // Row 0: 6 cards 2 hàng x 3 cột
        JPanel cards=new JPanel(new GridLayout(2,3,12,12)); cards.setOpaque(false);
        JLabel l1=new JLabel("0"),l2=new JLabel("0"),l3=new JLabel("0"),
               l4=new JLabel("0"),l5=new JLabel("0"),l6=new JLabel("0");
        cards.add(statCard("Tổng tài liệu",l1,C_PRIMARY));
        cards.add(statCard("Đang được mượn",l2,C_WARNING));
        cards.add(statCard("Tổng độc giả",l3,C_SUCCESS));
        cards.add(statCard("Quá hạn chưa trả",l4,C_DANGER));
        cards.add(statCard("Tổng bản sao",l5,new Color(124,58,237)));
        cards.add(statCard("Phiếu nhập tháng này",l6,new Color(14,165,233)));

        gc.gridy=0; gc.weighty=0; gc.insets=new Insets(0,0,16,0);
        cards.setPreferredSize(new Dimension(0,180));
        center.add(cards, gc);

        // Row 1: label
        JLabel lbTop=new JLabel("Top 10 tài liệu được mượn nhiều nhất");
        lbTop.setFont(new Font("Segoe UI",Font.BOLD,14)); lbTop.setForeground(new Color(30,41,59));
        gc.gridy=1; gc.weighty=0; gc.insets=new Insets(0,0,8,0);
        center.add(lbTop, gc);

        // Row 2: bảng
        DefaultTableModel mdTop=new DefaultTableModel(new String[]{"Tên tài liệu","Tác giả","Số lần mượn"},0);
        JTable tbTop=styledTable(mdTop);
        JScrollPane sp=new JScrollPane(tbTop); sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        gc.gridy=2; gc.weighty=1.0; gc.insets=new Insets(0,0,0,0);
        center.add(sp, gc);

        add(center, BorderLayout.CENTER);

        Runnable load=()->{
            try(Connection c=DBConnection.getConnection()){
                ResultSet rs;
                rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM TaiLieu"); if(rs.next())l1.setText(rs.getInt(1)+"");
                rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM MuonTra WHERE NgayTra IS NULL"); if(rs.next())l2.setText(rs.getInt(1)+"");
                rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM DocGia"); if(rs.next())l3.setText(rs.getInt(1)+"");
                rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM MuonTra WHERE NgayTra IS NULL AND HanTra<GETDATE()"); if(rs.next())l4.setText(rs.getInt(1)+"");
                rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM TLChitiet"); if(rs.next())l5.setText(rs.getInt(1)+"");
                rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM PhieuNhap WHERE MONTH(NgayNhap)=MONTH(GETDATE()) AND YEAR(NgayNhap)=YEAR(GETDATE())"); if(rs.next())l6.setText(rs.getInt(1)+"");
                mdTop.setRowCount(0);
                rs=c.createStatement().executeQuery("SELECT TOP 10 tl.TenTL,tg.TenTG,COUNT(*) AS n FROM MuonTra m JOIN TLChitiet tc ON m.MaCaBiet=tc.MaCaBiet JOIN TaiLieu tl ON tc.MaTL=tl.MaTL LEFT JOIN TacGia tg ON tl.MaTG=tg.MaTG GROUP BY tl.TenTL,tg.TenTG ORDER BY n DESC");
                while(rs.next()) mdTop.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getInt(3)});
            }catch(Exception e){e.printStackTrace();}
        };
        load.run(); bRef.addActionListener(e->load.run());
    }
}

// ── Ho So Nguoi Dung Panel ──────────────────────────────────────
class HoSoNDPanel extends JPanel {
    private final DefaultTableModel md = new DefaultTableModel(
        new String[]{"Tên ĐN","Họ tên","Năm sinh","Giới tính","Chức danh","Điện thoại","Email","Vai trò"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    HoSoNDPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(C_BG); setBorder(pad(16));

        JLabel title = new JLabel("Hồ Sơ Người Dùng");
        title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));

        JPanel grp = titledGroup("Thêm người dùng mới");
        JTextField tHoTen=tf(18),tTenDN=tf(14),tChucDanh=tf(12),tNamSinh=tf(6),tDT=tf(12),tEmail=tf(18);
        JPasswordField tMK=new JPasswordField(14); tMK.setFont(F_NORMAL); tMK.setPreferredSize(new Dimension(130,32));
        ButtonGroup bgGT=new ButtonGroup();
        JRadioButton rbNam=new JRadioButton("Nam",true),rbNu=new JRadioButton("Nữ");
        rbNam.setOpaque(false); rbNu.setOpaque(false); rbNam.setFont(F_NORMAL); rbNu.setFont(F_NORMAL);
        bgGT.add(rbNam); bgGT.add(rbNu);
        JComboBox<String> cboVT=new JComboBox<>(new String[]{"NhanVien","Admin"});
        cboVT.setFont(F_NORMAL);

        grp.setLayout(new GridBagLayout()); GridBagConstraints g=gbc(); int row=0;
        addRow4(grp,g,row++,"Họ tên *",tHoTen,"Tên đăng nhập *",tTenDN,"Mật khẩu *",tMK,"Chức danh",tChucDanh);
        g.gridx=0;g.gridy=row;g.weightx=0; grp.add(lbl("Năm sinh"),g);
        g.gridx=1;g.weightx=1; grp.add(tNamSinh,g);
        g.gridx=2;g.weightx=0; grp.add(lbl("Giới tính"),g);
        g.gridx=3;
        JPanel gt=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); gt.setOpaque(false); gt.add(rbNam); gt.add(rbNu);
        grp.add(gt,g);
        g.gridx=4;g.weightx=0; grp.add(lbl("Điện thoại"),g);
        g.gridx=5;g.weightx=1; grp.add(tDT,g);
        g.gridx=6;g.weightx=0; grp.add(lbl("Vai trò"),g);
        g.gridx=7;g.weightx=1; grp.add(cboVT,g); row++;

        JButton bAdd=btn("Thêm người dùng",C_WHITE,C_SUCCESS);
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,4)); bp.setOpaque(false); bp.add(bAdd);
        g.gridx=0;g.gridy=row;g.gridwidth=8; grp.add(bp,g);

        JTable tb=styledTable(md);
        tb.getColumnModel().getColumn(7).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                JLabel lb=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if(!sel){String val=v!=null?v.toString():"";
                    if(val.contains("Quản")){lb.setForeground(C_PRIMARY);lb.setFont(F_BOLD);}
                    else lb.setForeground(Color.BLACK);}
                return lb;
            }
        });
        JScrollPane sp=new JScrollPane(tb); sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        Runnable load=()->loadData(); load.run();

        bAdd.addActionListener(e->{
            if(tHoTen.getText().trim().isEmpty()||tTenDN.getText().trim().isEmpty()){msg("Nhập đủ họ tên và tên đăng nhập!"); return;}
            String mk=new String(tMK.getPassword()); if(mk.isEmpty()){msg("Nhập mật khẩu!"); return;}
            try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(
                "INSERT INTO TaiKhoan(TenDN,MatKhau,HoTen,NamSinh,GioiTinh,ChucDanh,DienThoai,Email,VaiTro) VALUES(?,?,?,?,?,?,?,?,?)")){
                ps.setString(1,tTenDN.getText().trim()); ps.setString(2,mk);
                ps.setString(3,tHoTen.getText().trim());
                ps.setObject(4,parseInt(tNamSinh.getText(),0));
                ps.setString(5,rbNam.isSelected()?"Nam":"Nu");
                ps.setString(6,tChucDanh.getText().trim());
                ps.setString(7,tDT.getText().trim());
                ps.setString(8,tEmail.getText().trim());
                ps.setString(9,(String)cboVT.getSelectedItem());
                ps.executeUpdate(); msg("Thêm người dùng thành công!"); load.run();
            }catch(Exception ex){msg("Lỗi: Tên đăng nhập đã tồn tại!");}
        });

        JPanel north=new JPanel(new BorderLayout(0,10)); north.setOpaque(false);
        north.add(title, BorderLayout.NORTH);
        north.add(grp, BorderLayout.CENTER);

        JLabel lbList=new JLabel("Danh sách người dùng");
        lbList.setFont(new Font("Segoe UI",Font.BOLD,14)); lbList.setForeground(new Color(30,41,59));
        JPanel center=new JPanel(new BorderLayout(0,8)); center.setOpaque(false);
        center.add(lbList,BorderLayout.NORTH); center.add(sp,BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void loadData() {
        md.setRowCount(0);
        try(Connection c=DBConnection.getConnection();ResultSet rs=c.createStatement().executeQuery("SELECT * FROM TaiKhoan ORDER BY HoTen")){
            while(rs.next())
                md.addRow(new Object[]{
                    rs.getString("TenDN"), rs.getString("HoTen"),
                    rs.getInt("NamSinh")!=0?rs.getInt("NamSinh")+"":"",
                    "Nu".equals(rs.getString("GioiTinh"))?"Nữ":"Nam",
                    rs.getString("ChucDanh"), rs.getString("DienThoai"), rs.getString("Email"),
                    "Admin".equals(rs.getString("VaiTro"))?"Quản trị viên":"Nhân viên"
                });
        }catch(Exception e){e.printStackTrace();}
    }
    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
}

// ── Doi Pass Panel ──────────────────────────────────────────────
class DoiPassPanel extends JPanel {
    DoiPassPanel(JFrame parent, String currentUser) {
        setLayout(new GridBagLayout()); setBackground(C_BG);
        JPanel box=new JPanel(new GridBagLayout()); box.setBackground(C_WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER),
            BorderFactory.createEmptyBorder(32,44,32,44)));
        box.setPreferredSize(new Dimension(420,280));
        GridBagConstraints g=new GridBagConstraints(); g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(8,6,8,6); g.gridwidth=2;
        JLabel title=new JLabel("Thay Đổi Mật Khẩu"); title.setFont(F_TITLE);
        g.gridx=0;g.gridy=0; box.add(title,g); g.gridwidth=1;
        JPasswordField tCu=new JPasswordField(22),tMoi=new JPasswordField(22),tXN=new JPasswordField(22);
        int r=1;
        g.gridx=0;g.gridy=r;g.weightx=0; box.add(lbl("Mật khẩu hiện tại"),g); g.gridx=1;g.weightx=1; box.add(tCu,g); r++;
        g.gridx=0;g.gridy=r;g.weightx=0; box.add(lbl("Mật khẩu mới"),g);      g.gridx=1;g.weightx=1; box.add(tMoi,g); r++;
        g.gridx=0;g.gridy=r;g.weightx=0; box.add(lbl("Xác nhận mật khẩu mới"),g); g.gridx=1;g.weightx=1; box.add(tXN,g); r++;
        JButton bOK=btn("Xác nhận đổi mật khẩu",C_WHITE,C_PRIMARY);
        JButton bHuy=btn("Hủy",Color.BLACK,new Color(229,231,235));
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); bp.setOpaque(false); bp.add(bHuy); bp.add(bOK);
        g.gridx=0;g.gridy=r;g.gridwidth=2; box.add(bp,g);
        bHuy.addActionListener(e->{tCu.setText("");tMoi.setText("");tXN.setText("");});
        bOK.addActionListener(e->{
            String cu=new String(tCu.getPassword()),moi=new String(tMoi.getPassword()),xn=new String(tXN.getPassword());
            if(cu.isEmpty()||moi.isEmpty()){msg("Nhập đầy đủ!"); return;}
            if(!moi.equals(xn)){msg("Mật khẩu mới không khớp!"); return;}
            if(moi.length()<4){msg("Mật khẩu tối thiểu 4 ký tự!"); return;}
            try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(
                "UPDATE TaiKhoan SET MatKhau=? WHERE HoTen=? AND MatKhau=?")){
                ps.setString(1,moi); ps.setString(2,currentUser); ps.setString(3,cu);
                if(ps.executeUpdate()>0){msg("Đổi mật khẩu thành công! Vui lòng đăng nhập lại."); parent.dispose(); new LoginFrame().setVisible(true);}
                else msg("Mật khẩu hiện tại không đúng!");
            }catch(Exception ex){msg("Lỗi!");}
        });
        add(box);
    }
    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
}

// ── Backup Panel ────────────────────────────────────────────────
class BackupPanel extends JPanel {
    BackupPanel() {
        setLayout(new GridBagLayout()); setBackground(C_BG);
        JPanel box=new JPanel(new GridBagLayout()); box.setBackground(C_WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_BORDER),BorderFactory.createEmptyBorder(28,36,28,36)));
        GridBagConstraints g=new GridBagConstraints(); g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(10,6,10,6);
        JLabel title=new JLabel("Sao Lưu & Phục Hồi Dữ Liệu"); title.setFont(F_TITLE);
        g.gridx=0;g.gridy=0;g.gridwidth=3; box.add(title,g);

        JLabel lbSL=new JLabel("Sao lưu dữ liệu"); lbSL.setFont(F_BOLD); g.gridy=1;g.gridwidth=3; box.add(lbSL,g);
        JTextField tDBName=new JTextField("QuanLyThuVien",16),tPath=new JTextField(24);
        JButton bBrowseB=new JButton("Chọn thư mục"),bSaoLuu=btn("Sao lưu ngay",C_WHITE,C_PRIMARY);
        g.gridy=2;g.gridwidth=1;g.weightx=0; box.add(lbl("Tên CSDL"),g);
        g.gridx=1;g.weightx=1; box.add(tDBName,g);
        g.gridx=2;g.weightx=0; box.add(bSaoLuu,g);
        g.gridx=0;g.gridy=3;g.weightx=0; box.add(lbl("Thư mục lưu"),g);
        g.gridx=1;g.weightx=1; box.add(tPath,g);
        g.gridx=2;g.weightx=0; box.add(bBrowseB,g);

        JLabel lbPH=new JLabel("Phục hồi dữ liệu"); lbPH.setFont(F_BOLD);
        g.gridx=0;g.gridy=4;g.gridwidth=3; box.add(lbPH,g);
        JTextField tFile=new JTextField(24),tDest=new JTextField(16);
        JButton bBrowseR=new JButton("Chọn file backup"),bPhucHoi=btn("Phục hồi",C_WHITE,C_WARNING);
        g.gridy=5;g.gridwidth=1;g.weightx=0; box.add(lbl("File backup (.bak)"),g);
        g.gridx=1;g.weightx=1; box.add(tFile,g);
        g.gridx=2;g.weightx=0; box.add(bBrowseR,g);
        g.gridx=0;g.gridy=6;g.weightx=0; box.add(lbl("Tên CSDL đích"),g);
        g.gridx=1;g.weightx=1; box.add(tDest,g);
        g.gridx=2;g.weightx=0; box.add(bPhucHoi,g);

        JLabel lbSt=new JLabel(" "); lbSt.setFont(F_SMALL);
        g.gridx=0;g.gridy=7;g.gridwidth=3; box.add(lbSt,g);

        bBrowseB.addActionListener(e->{ JFileChooser fc=new JFileChooser(); fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) tPath.setText(fc.getSelectedFile().getAbsolutePath()); });
        bBrowseR.addActionListener(e->{ JFileChooser fc=new JFileChooser(); fc.setFileFilter(new FileNameExtensionFilter("Backup Files","bak")); if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) tFile.setText(fc.getSelectedFile().getAbsolutePath()); });
        bSaoLuu.addActionListener(e->{
            if(tPath.getText().trim().isEmpty()){msg("Chọn thư mục lưu!"); return;}
            String path=(tPath.getText().trim()+"\\"+tDBName.getText().trim()+"_backup.bak").replace("\\","\\\\");
            try(Connection c=DBConnection.getConnection()){
                c.createStatement().executeUpdate("BACKUP DATABASE "+tDBName.getText().trim()+" TO DISK='"+path+"' WITH FORMAT");
                lbSt.setText("Sao lưu thành công: "+path); lbSt.setForeground(C_SUCCESS);
                msg("Sao lưu thành công!");
            }catch(Exception ex){lbSt.setText("Lỗi sao lưu!"); lbSt.setForeground(C_DANGER); msg(ex.getMessage());}
        });
        bPhucHoi.addActionListener(e->{
            if(JOptionPane.showConfirmDialog(this,"Phục hồi sẽ GHI ĐÈ toàn bộ dữ liệu hiện tại. Bạn chắc chắn?","Cảnh báo",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=0) return;
            String file=tFile.getText().trim().replace("\\","\\\\");
            String dest=tDest.getText().trim().isEmpty()?"QuanLyThuVien":tDest.getText().trim();
            try(Connection c=DBConnection.getConnection()){
                c.createStatement().executeUpdate("ALTER DATABASE "+dest+" SET SINGLE_USER WITH ROLLBACK IMMEDIATE");
                c.createStatement().executeUpdate("RESTORE DATABASE "+dest+" FROM DISK='"+file+"' WITH REPLACE");
                c.createStatement().executeUpdate("ALTER DATABASE "+dest+" SET MULTI_USER");
                lbSt.setText("Phục hồi thành công!"); lbSt.setForeground(C_SUCCESS);
                msg("Phục hồi thành công! Vui lòng khởi động lại ứng dụng.");
            }catch(Exception ex){lbSt.setText("Lỗi phục hồi!"); lbSt.setForeground(C_DANGER); msg(ex.getMessage());}
        });
        add(box);
    }
    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
}