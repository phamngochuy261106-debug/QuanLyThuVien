package com.library.ui;

import com.library.dao.DBConnection;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import static com.library.ui.UIConstants.*;

public class DocGiaPanel extends JPanel {

    private final DefaultTableModel md = new DefaultTableModel(
        new String[]{"Mã","Số thẻ","Họ tên","Số ĐT","Email","Địa chỉ","Lớp/Ngành","Hạn SD","Tình trạng"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    public DocGiaPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(C_BG); setBorder(pad(16));

        // ── Tiêu đề ──────────────────────────────────────────
        JLabel title = new JLabel("Quản Lý Độc Giả");
        title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));

        // ── Form nhập ─────────────────────────────────────────
        JPanel grpForm = titledGroup("Thông tin độc giả");
        JTextField tSoThe=tf(), tTen=tf(18), tSDT=tf(12), tEmail=tf(18), tDC=tf(22);
        JComboBox<String> cboTT  = new JComboBox<>(new String[]{"Hoạt động","Khóa"});
        JComboBox<String> cboLop = new JComboBox<>();
        JDateChooser dcHan = new JDateChooser();
        dcHan.setDateFormatString("dd/MM/yyyy");
        dcHan.setPreferredSize(new Dimension(140, 32));
        dcHan.setDate(new Date(System.currentTimeMillis()+365L*86400000));
        loadCombo(cboLop,"SELECT TenLop FROM Lop ORDER BY TenLop");

        grpForm.setLayout(new GridBagLayout());
        GridBagConstraints g=gbc(); int row=0;
        addRow4(grpForm,g,row++,"Số thẻ",tSoThe,"Họ tên *",tTen,"SDT",tSDT,"Email",tEmail);
        addRow4(grpForm,g,row++,"Địa chỉ",tDC,"Lớp/Ngành",cboLop,"Hạn sử dụng",dcHan,"Tình trạng",cboTT);

        JButton bNew = btn("Tạo mới",  C_WHITE, new Color(108,117,125));
        JButton bAdd = btn("Thêm mới", C_WHITE, C_SUCCESS);
        JButton bSua = btn("Cập nhật", C_WHITE, C_PRIMARY);
        JButton bXoa = btn("Xóa",      C_WHITE, C_DANGER);
        JButton bLs  = btn("Lịch sử mượn", C_WHITE, new Color(80,130,180));
        JPanel btnP  = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,4));
        btnP.setOpaque(false); btnP.add(bNew); btnP.add(bXoa); btnP.add(bLs); btnP.add(bSua); btnP.add(bAdd);
        g.gridx=0; g.gridy=row; g.gridwidth=8; grpForm.add(btnP,g);

        // ── Thanh tìm kiếm ────────────────────────────────────
        JTextField txtFind = searchField("Tìm tên, số thẻ, SDT, email...");
        JButton bTim = btn("Tìm kiếm", C_WHITE, C_PRIMARY);
        JLabel lbCount = new JLabel(""); lbCount.setFont(F_SMALL); lbCount.setForeground(C_TEXT_MUT);
        // Realtime search
        txtFind.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){reload();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){reload();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){reload();}
            void reload(){ SwingUtilities.invokeLater(()->bTim.doClick()); }
        });

        // ── Bảng danh sách ────────────────────────────────────
        JTable tb = styledTable(md);
        // Action nut Lich su - dat sau khi tb duoc khai bao
        bLs.addActionListener(e -> {
            int r = tb.getSelectedRow();
            if(r<0){JOptionPane.showMessageDialog(this,"Chọn độc giả để xem lịch sử!"); return;}
            String soThe=md.getValueAt(r,1)!=null?md.getValueAt(r,1).toString():"";
            String hoTen=md.getValueAt(r,2)!=null?md.getValueAt(r,2).toString():"";
            showLichSu(soThe, hoTen);
        });
        tb.getColumnModel().getColumn(8).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                JLabel lb=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if(!sel){String val=v!=null?v.toString():"";
                    if("Khóa".equals(val)||"Khóa".equals(val)||"Khoa".equals(val)){lb.setForeground(C_DANGER);lb.setFont(F_BOLD);}
                    else{lb.setForeground(C_SUCCESS);lb.setFont(F_NORMAL);}}
                return lb;
            }
        });

        // Click vào dòng → điền form
        tb.getSelectionModel().addListSelectionListener(e -> {
            int r = tb.getSelectedRow(); if(r<0) return;
            tSoThe.setText(s(md,r,1)); tTen.setText(s(md,r,2));
            tSDT.setText(s(md,r,3));   tEmail.setText(s(md,r,4));
            tDC.setText(s(md,r,5));
            String tenLop=s(md,r,6);
            for(int i=0;i<cboLop.getItemCount();i++)
                if(cboLop.getItemAt(i).equals(tenLop)){cboLop.setSelectedIndex(i);break;}
            cboTT.setSelectedItem(s(md,r,8));
        });

        Runnable loadAll = () -> {
            loadTable(txtFind.getText().trim());
            lbCount.setText("Tổng: " + md.getRowCount() + " độc giả");
        };
        loadAll.run();

        bNew.addActionListener(e -> {
            tSoThe.setText(""); tTen.setText(""); tSDT.setText(""); tEmail.setText(""); tDC.setText("");
            cboTT.setSelectedIndex(0); tb.clearSelection();
        });
        bTim.addActionListener(e -> { loadTable(txtFind.getText().trim()); lbCount.setText("Tìm thấy: "+md.getRowCount()); });
        txtFind.addActionListener(e -> bTim.doClick());
        bAdd.addActionListener(e -> {
            if(tTen.getText().trim().isEmpty()){msg("Vui lòng nhập họ tên!"); return;}
            if(tSDT.getText().trim().isEmpty()){msg("Vui lòng nhập số điện thoại!"); return;}
            doSave(-1,tSoThe,tTen,tSDT,tEmail,tDC,cboLop,dcHan,cboTT,loadAll);
        });
        bSua.addActionListener(e -> {
            int r=tb.getSelectedRow(); if(r<0){msg("Chọn độc giả cần cập nhật!"); return;}
            doSave((int)md.getValueAt(r,0),tSoThe,tTen,tSDT,tEmail,tDC,cboLop,dcHan,cboTT,loadAll);
        });
        bXoa.addActionListener(e -> {
            int r=tb.getSelectedRow(); if(r<0){msg("Chọn độc giả cần xóa!"); return;}
            if(confirm("Xóa độc giả \""+md.getValueAt(r,2)+"\"?")){
                try(Connection c=DBConnection.getConnection()){
                    c.createStatement().executeUpdate("DELETE FROM DocGia WHERE MaDG="+(int)md.getValueAt(r,0));
                    msg("Đã xóa thành công!"); loadAll.run();
                }catch(Exception ex){msg("Không thể xóa! Độc giả đang có phiếu mượn.");}
            }
        });

        JScrollPane sp = new JScrollPane(tb);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER));

        JLabel lbList = new JLabel("Danh sách độc giả");
        lbList.setFont(new Font("Segoe UI",Font.BOLD,14));
        lbList.setForeground(new Color(30,41,59));

        JPanel searchRow = rowPanel(txtFind, bTim, lbCount);

        // NORTH: tiêu đề + form
        JPanel north = new JPanel(new BorderLayout(0,8)); north.setOpaque(false);
        north.add(title, BorderLayout.NORTH);
        north.add(grpForm, BorderLayout.CENTER);

        // CENTER: label + tìm kiếm
        JPanel midRow = new JPanel(new BorderLayout(0,6)); midRow.setOpaque(false);
        midRow.add(lbList, BorderLayout.NORTH);
        midRow.add(searchRow, BorderLayout.CENTER);

        // CENTER tổng: mid + bảng
        JPanel center = new JPanel(new BorderLayout(0,6)); center.setOpaque(false);
        center.add(midRow, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);  // bảng chiếm hết phần còn lại

        add(north,  BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void loadTable(String kw) {
        md.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try (Connection c = DBConnection.getConnection();
             ResultSet rs = c.createStatement().executeQuery(
                "SELECT d.MaDG,d.SoThe,d.HoTen,d.SDT,d.Email,d.DiaChi,l.TenLop,d.HanSD,d.TinhTrang"
                +" FROM DocGia d LEFT JOIN Lop l ON d.MaLop=l.MaLop ORDER BY d.MaDG")) {
            while (rs.next()) {
                String hoTen=rs.getString(3)!=null?rs.getString(3):"";
                String soThe=rs.getString(2)!=null?rs.getString(2):"";
                String sdt  =rs.getString(4)!=null?rs.getString(4):"";
                String email=rs.getString(5)!=null?rs.getString(5):"";
                if (!kw.isEmpty() && !matchSearch(hoTen,kw) && !matchSearch(soThe,kw)
                    && !matchSearch(sdt,kw) && !matchSearch(email,kw)) continue;
                md.addRow(new Object[]{rs.getInt(1),soThe,hoTen,sdt,email,
                    rs.getString(6),rs.getString(7),fmt(sdf,rs.getDate(8)),rs.getString(9)});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void doSave(int maDG, JTextField tSoThe, JTextField tTen, JTextField tSDT,
            JTextField tEmail, JTextField tDC, JComboBox<String> cboLop,
            JDateChooser dcHan, JComboBox<String> cboTT, Runnable reload) {
        try (Connection c = DBConnection.getConnection()) {
            Integer maLop=comboId(c,"Lop","MaLop","TenLop",cboLop);
            String sql = maDG<0
                ? "INSERT INTO DocGia(SoThe,HoTen,SDT,Email,DiaChi,MaLop,HanSD,TinhTrang) VALUES(?,?,?,?,?,?,?,?)"
                : "UPDATE DocGia SET SoThe=?,HoTen=?,SDT=?,Email=?,DiaChi=?,MaLop=?,HanSD=?,TinhTrang=? WHERE MaDG=?";
            PreparedStatement ps=c.prepareStatement(sql);
            ps.setString(1,tSoThe.getText().trim()); ps.setString(2,tTen.getText().trim());
            ps.setString(3,tSDT.getText().trim());   ps.setString(4,tEmail.getText().trim());
            ps.setString(5,tDC.getText().trim());    ps.setObject(6,maLop);
            ps.setObject(7,dcHan.getDate()!=null?new java.sql.Date(dcHan.getDate().getTime()):null);
            ps.setString(8,(String)cboTT.getSelectedItem());
            if(maDG>=0) ps.setInt(9,maDG);
            ps.executeUpdate();
            msg(maDG<0?"Thêm độc giả thành công!":"Cập nhật thành công!"); reload.run();
        } catch (Exception ex) { msg("Lỗi: "+ex.getMessage()); }
    }

    private String s(DefaultTableModel m,int r,int c){return m.getValueAt(r,c)!=null?m.getValueAt(r,c).toString():"";}
    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
    private boolean confirm(String s){return JOptionPane.showConfirmDialog(this,s,"Xác nhận",JOptionPane.YES_NO_OPTION)==0;}

    private void showLichSu(String soThe, String hoTen) {
        javax.swing.table.DefaultTableModel mdLs = new javax.swing.table.DefaultTableModel(
            new String[]{"ID","Tên tài liệu","Kiểu mượn","Ngày mượn","Hạn trả","Ngày trả","Trạng thái"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("dd/MM/yyyy");
        try(java.sql.Connection c=com.library.dao.DBConnection.getConnection();
            java.sql.PreparedStatement ps=c.prepareStatement(
                "SELECT m.ID_MuonTra,tl.TenTL,m.KieuMuon,m.NgayMuon,m.HanTra,m.NgayTra "+
                "FROM MuonTra m LEFT JOIN TLChitiet tc ON m.MaCaBiet=tc.MaCaBiet "+
                "LEFT JOIN TaiLieu tl ON tc.MaTL=tl.MaTL WHERE m.SoThe=? ORDER BY m.ID_MuonTra DESC")){
            ps.setString(1,soThe); java.sql.ResultSet rs=ps.executeQuery();
            while(rs.next()){
                String tt=rs.getDate("NgayTra")!=null?"Đã trả":
                    (rs.getDate("HanTra")!=null&&rs.getDate("HanTra").before(new java.util.Date())?"Quá hạn":"Đang mượn");
                mdLs.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),
                    UIConstants.fmt(sdf,rs.getDate(4)),UIConstants.fmt(sdf,rs.getDate(5)),
                    UIConstants.fmt(sdf,rs.getDate(6)),tt});
            }
        }catch(Exception ex){ex.printStackTrace();}
        JTable tbLs=UIConstants.styledTable(mdLs);
        tbLs.getColumnModel().getColumn(6).setCellRenderer(UIConstants.statusRenderer());
        JScrollPane sp=new JScrollPane(tbLs); sp.setPreferredSize(new java.awt.Dimension(720,300));
        JDialog dlg=new JDialog((java.awt.Frame)SwingUtilities.getWindowAncestor(this),
            "Lịch sử mượn trả: "+hoTen+" ("+soThe+")",true);
        dlg.setLayout(new java.awt.BorderLayout(0,6));
        JLabel lb=new JLabel("  Tổng "+mdLs.getRowCount()+" lần mượn");
        lb.setFont(UIConstants.F_SMALL); lb.setForeground(UIConstants.C_TEXT_MUT);
        dlg.add(lb,java.awt.BorderLayout.NORTH); dlg.add(sp,java.awt.BorderLayout.CENTER);
        dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

}