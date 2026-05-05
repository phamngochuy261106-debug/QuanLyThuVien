package com.library.ui;

import com.library.dao.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import static com.library.ui.UIConstants.*;

public class DanhMucPanel extends JPanel {

    private final String bang, colId, colTen, colGC;
    private final DefaultTableModel md;
    private final JTable tb;
    private final JTextField tTen, tGC;

    public DanhMucPanel(String bang, String colId, String colTen, String colGC, String label) {
        this.bang=bang; this.colId=colId; this.colTen=colTen; this.colGC=colGC;
        setLayout(new BorderLayout(0, 12));
        setBackground(C_BG); setBorder(pad(16));

        // Tiêu đề
        JLabel title = new JLabel("Danh Mục: " + label);
        title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));

        // Form
        JPanel grp = titledGroup("Thêm / Sửa " + label);
        tTen = tf(22); tGC = tf(28);
        tTen.putClientProperty("JTextField.placeholderText", "Nhập tên " + label + "...");
        tGC.putClientProperty("JTextField.placeholderText", "Ghi chú (nếu có)...");

        JButton bNew = btn("Tạo mới",  C_WHITE, new Color(108,117,125));
        JButton bAdd = btn("Thêm",     C_WHITE, C_SUCCESS);
        JButton bSua = btn("Cập nhật", C_WHITE, C_PRIMARY);
        JButton bXoa = btn("Xóa",      C_WHITE, C_DANGER);

        // Dong 1: ten + ghi chu
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row1.setOpaque(false);
        row1.add(lbl(label + " *")); row1.add(tTen);
        row1.add(lbl("Ghi chú"));    row1.add(tGC);

        // Dong 2: cac nut
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.setOpaque(false);
        row2.add(bNew); row2.add(bAdd); row2.add(bSua); row2.add(bXoa);

        grp.setLayout(new java.awt.BorderLayout(0, 2));
        grp.add(row1, java.awt.BorderLayout.NORTH);
        grp.add(row2, java.awt.BorderLayout.SOUTH);

        // Bảng
        md = new DefaultTableModel(new String[]{"Mã","Tên","Ghi chú"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tb = styledTable(md);
        tb.getSelectionModel().addListSelectionListener(e -> {
            int r = tb.getSelectedRow(); if(r<0) return;
            tTen.setText(md.getValueAt(r,1)!=null?md.getValueAt(r,1).toString():"");
            tGC.setText(md.getValueAt(r,2)!=null?md.getValueAt(r,2).toString():"");
        });

        JScrollPane sp = new JScrollPane(tb);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER));

        bNew.addActionListener(e -> { tTen.setText(""); tGC.setText(""); tb.clearSelection(); });
        bAdd.addActionListener(e -> doAdd());
        bSua.addActionListener(e -> doSua());
        bXoa.addActionListener(e -> doXoa());
        load();

        // Layout
        JPanel north = new JPanel(new BorderLayout(0,10)); north.setOpaque(false);
        north.add(title, BorderLayout.NORTH);
        north.add(grp, BorderLayout.CENTER);

        JLabel lbList = new JLabel("Danh sách");
        lbList.setFont(new Font("Segoe UI",Font.BOLD,14)); lbList.setForeground(new Color(30,41,59));
        JPanel center = new JPanel(new BorderLayout(0,8)); center.setOpaque(false);
        center.add(lbList, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void load() {
        md.setRowCount(0);
        try(Connection c=DBConnection.getConnection(); ResultSet rs=c.createStatement().executeQuery(
            "SELECT "+colId+","+colTen+","+colGC+" FROM "+bang+" ORDER BY "+colTen)) {
            while(rs.next()) md.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3)});
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void doAdd() {
        if(tTen.getText().trim().isEmpty()) { msg("Nhập tên!"); return; }
        try(Connection c=DBConnection.getConnection();
            PreparedStatement ps=c.prepareStatement("INSERT INTO "+bang+"("+colTen+","+colGC+") VALUES(?,?)")) {
            ps.setString(1,tTen.getText().trim()); ps.setString(2,tGC.getText().trim());
            ps.executeUpdate(); msg("Thêm thành công!"); tTen.setText(""); tGC.setText(""); load();
        } catch(Exception ex) { msg("Lỗi: Tên đã tồn tại!"); }
    }

    private void doSua() {
        int r=tb.getSelectedRow(); if(r<0){msg("Chọn dòng cần sửa!"); return;}
        if(tTen.getText().trim().isEmpty()){msg("Nhập tên!"); return;}
        try(Connection c=DBConnection.getConnection()) {
            PreparedStatement ps=c.prepareStatement("UPDATE "+bang+" SET "+colTen+"=?,"+colGC+"=? WHERE "+colId+"=?");
            ps.setString(1,tTen.getText().trim()); ps.setString(2,tGC.getText().trim()); ps.setInt(3,(int)md.getValueAt(r,0));
            ps.executeUpdate(); msg("Cập nhật thành công!"); load();
        } catch(Exception ex) { msg("Lỗi!"); }
    }

    private void doXoa() {
        int r=tb.getSelectedRow(); if(r<0){msg("Chọn dòng cần xóa!"); return;}
        if(!confirm("Xóa [" + md.getValueAt(r,1) + "]?")) return;
        try(Connection c=DBConnection.getConnection()) {
            c.createStatement().executeUpdate("DELETE FROM "+bang+" WHERE "+colId+"="+(int)md.getValueAt(r,0));
            msg("Xóa thành công!"); load();
        } catch(Exception ex) { msg("Không thể xóa! Dữ liệu đang được sử dụng."); }
    }

    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
    private boolean confirm(String s){return JOptionPane.showConfirmDialog(this,s,"Xác nhận",JOptionPane.YES_NO_OPTION)==0;}
}

// ── Lop Panel ─────────────────────────────────────────────────────
class LopPanel extends JPanel {
    private final DefaultTableModel md=new DefaultTableModel(new String[]{"Mã","Tên lớp","Đơn vị","Ghi chú"},0){
        public boolean isCellEditable(int r,int c){return false;}};
    LopPanel(){
        setLayout(new BorderLayout(0,12)); setBackground(C_BG); setBorder(pad(16));
        JLabel title=new JLabel("Danh Mục: Lớp / Ngành"); title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));
        JPanel grp=titledGroup("Thêm / Sửa lớp");
        JTextField tTen=tf(18),tGC=tf(22);
        tTen.putClientProperty("JTextField.placeholderText","Tên lớp/ngành...");
        JComboBox<String> cboDV=new JComboBox<>();
        loadCombo(cboDV,"SELECT TenDV FROM DonVi ORDER BY TenDV");

        JButton bNew=btn("Tạo mới",C_WHITE,new Color(108,117,125));
        JButton bAdd=btn("Thêm",C_WHITE,C_SUCCESS);
        JButton bSua=btn("Cập nhật",C_WHITE,C_PRIMARY);
        JButton bXoa=btn("Xóa",C_WHITE,C_DANGER);

        JPanel row1=new JPanel(new FlowLayout(FlowLayout.LEFT,10,4)); row1.setOpaque(false);
        row1.add(lbl("Tên lớp *")); row1.add(tTen);
        row1.add(lbl("Đơn vị"));    row1.add(cboDV);
        row1.add(lbl("Ghi chú"));   row1.add(tGC);

        JPanel row2=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4)); row2.setOpaque(false);
        row2.add(bNew); row2.add(bAdd); row2.add(bSua); row2.add(bXoa);

        grp.setLayout(new java.awt.BorderLayout(0,2));
        grp.add(row1,java.awt.BorderLayout.NORTH);
        grp.add(row2,java.awt.BorderLayout.SOUTH);

        JTable tb=styledTable(md);
        JScrollPane sp=new JScrollPane(tb); sp.setBorder(BorderFactory.createLineBorder(C_BORDER));

        Runnable load=()->{md.setRowCount(0);
            try(Connection c=DBConnection.getConnection();ResultSet rs=c.createStatement().executeQuery(
                "SELECT l.MaLop,l.TenLop,d.TenDV,l.GhiChu FROM Lop l LEFT JOIN DonVi d ON l.MaDV=d.MaDV ORDER BY l.TenLop")){
                while(rs.next())md.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4)});
            }catch(Exception e){e.printStackTrace();}};
        load.run();

        // Click row -> fill form
        tb.getSelectionModel().addListSelectionListener(e->{
            int r=tb.getSelectedRow(); if(r<0) return;
            tTen.setText(md.getValueAt(r,1)!=null?md.getValueAt(r,1).toString():"");
            tGC.setText(md.getValueAt(r,3)!=null?md.getValueAt(r,3).toString():"");
            String tenDV=md.getValueAt(r,2)!=null?md.getValueAt(r,2).toString():"";
            for(int i=0;i<cboDV.getItemCount();i++) if(cboDV.getItemAt(i).equals(tenDV)){cboDV.setSelectedIndex(i);break;}
        });

        bNew.addActionListener(e->{tTen.setText("");tGC.setText("");cboDV.setSelectedIndex(0);tb.clearSelection();});

        bAdd.addActionListener(e->{
            if(tTen.getText().trim().isEmpty()){msg("Nhập tên lớp!"); return;}
            try(Connection c=DBConnection.getConnection()){
                Integer maDV=comboId(c,"DonVi","MaDV","TenDV",cboDV);
                PreparedStatement ps=c.prepareStatement("INSERT INTO Lop(TenLop,MaDV,GhiChu) VALUES(?,?,?)");
                ps.setString(1,tTen.getText().trim());ps.setObject(2,maDV);ps.setString(3,tGC.getText().trim());
                ps.executeUpdate();msg("Thêm thành công!");tTen.setText("");tGC.setText("");load.run();
            }catch(Exception ex){msg("Lỗi!");}
        });

        bSua.addActionListener(e->{
            int r=tb.getSelectedRow(); if(r<0){msg("Chọn dòng cần sửa!"); return;}
            if(tTen.getText().trim().isEmpty()){msg("Nhập tên lớp!"); return;}
            try(Connection c=DBConnection.getConnection()){
                Integer maDV=comboId(c,"DonVi","MaDV","TenDV",cboDV);
                PreparedStatement ps=c.prepareStatement("UPDATE Lop SET TenLop=?,MaDV=?,GhiChu=? WHERE MaLop=?");
                ps.setString(1,tTen.getText().trim());ps.setObject(2,maDV);
                ps.setString(3,tGC.getText().trim());ps.setInt(4,(int)md.getValueAt(r,0));
                ps.executeUpdate();msg("Cập nhật thành công!");load.run();
            }catch(Exception ex){msg("Lỗi!");}
        });

        bXoa.addActionListener(e->{
            int r=tb.getSelectedRow(); if(r<0){msg("Chọn dòng cần xóa!"); return;}
            if(confirm("Xóa lớp [" + md.getValueAt(r,1) + "]?")){
                try(Connection c=DBConnection.getConnection()){
                    c.createStatement().executeUpdate("DELETE FROM Lop WHERE MaLop="+(int)md.getValueAt(r,0));
                    msg("Xóa thành công!");load.run();
                }catch(Exception ex){msg("Không thể xóa! Đang được sử dụng.");}
            }
        });

        JPanel north=new JPanel(new BorderLayout(0,10)); north.setOpaque(false);
        north.add(title,BorderLayout.NORTH); north.add(grp,BorderLayout.CENTER);
        JLabel lbL=new JLabel("Danh sách"); lbL.setFont(new Font("Segoe UI",Font.BOLD,14));
        JPanel center=new JPanel(new BorderLayout(0,8)); center.setOpaque(false);
        center.add(lbL,BorderLayout.NORTH); center.add(sp,BorderLayout.CENTER);
        add(north,BorderLayout.NORTH); add(center,BorderLayout.CENTER);
    }
    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
    private boolean confirm(String s){return JOptionPane.showConfirmDialog(this,s,"Xác nhận",JOptionPane.YES_NO_OPTION)==0;}
}


class NCCPanel extends JPanel {
    private final DefaultTableModel md=new DefaultTableModel(new String[]{"Mã","Tên NCC","Địa chỉ","Điện thoại"},0){
        public boolean isCellEditable(int r,int c){return false;}};
    NCCPanel(){
        setLayout(new BorderLayout(0,12)); setBackground(C_BG); setBorder(pad(16));
        JLabel title=new JLabel("Danh Mục: Nhà Cung Cấp"); title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));
        JPanel grp=titledGroup("Thêm nhà cung cấp");
        JTextField tTen=tf(18),tDC=tf(22),tDT=tf(14);
        tTen.putClientProperty("JTextField.placeholderText","Tên nhà cung cấp...");
        JButton bAdd=btn("Thêm",C_WHITE,C_SUCCESS),bXoa=btn("Xóa",C_WHITE,C_DANGER);
        grp.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));
        grp.add(lbl("Tên NCC *")); grp.add(tTen);
        grp.add(lbl("Địa chỉ"));   grp.add(tDC);
        grp.add(lbl("Điện thoại")); grp.add(tDT);
        grp.add(bAdd); grp.add(bXoa);
        JTable tb=styledTable(md);
        JScrollPane sp=new JScrollPane(tb); sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        Runnable load=()->{md.setRowCount(0);
            try(Connection c=DBConnection.getConnection();ResultSet rs=c.createStatement().executeQuery(
                "SELECT * FROM NhaCungCap ORDER BY TenNCC")){
                while(rs.next())md.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4)});
            }catch(Exception e){}};
        load.run();
        bAdd.addActionListener(e->{if(tTen.getText().trim().isEmpty()){msg("Nhập tên NCC!"); return;}
            try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(
                "INSERT INTO NhaCungCap(TenNCC,DiaChi,DienThoai) VALUES(?,?,?)")){
                ps.setString(1,tTen.getText().trim());ps.setString(2,tDC.getText().trim());ps.setString(3,tDT.getText().trim());
                ps.executeUpdate();msg("Thêm thành công!");tTen.setText("");tDC.setText("");tDT.setText("");load.run();
            }catch(Exception ex){msg("Lỗi!");}});
        bXoa.addActionListener(e->{int r=tb.getSelectedRow();if(r<0){msg("Chọn dòng!");return;}
            if(confirm("Xóa?")){try(Connection c=DBConnection.getConnection()){
                c.createStatement().executeUpdate("DELETE FROM NhaCungCap WHERE MaNCC="+(int)md.getValueAt(r,0));
                msg("Xóa thành công!");load.run();}catch(Exception ex){msg("Không thể xóa!");}}});
        JPanel north=new JPanel(new BorderLayout(0,10)); north.setOpaque(false);
        north.add(title,BorderLayout.NORTH); north.add(grp,BorderLayout.CENTER);
        JLabel lbL=new JLabel("Danh sách"); lbL.setFont(new Font("Segoe UI",Font.BOLD,14));
        JPanel center=new JPanel(new BorderLayout(0,8)); center.setOpaque(false);
        center.add(lbL,BorderLayout.NORTH); center.add(sp,BorderLayout.CENTER);
        add(north,BorderLayout.NORTH); add(center,BorderLayout.CENTER);
    }
    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
    private boolean confirm(String s){return JOptionPane.showConfirmDialog(this,s,"Xác nhận",JOptionPane.YES_NO_OPTION)==0;}
}

// ── Vitri Panel ───────────────────────────────────────────────────
class VitriPanel extends JPanel {
    private final DefaultTableModel md=new DefaultTableModel(new String[]{"Mã","Tên vị trí","Mô tả","Vị trí cha"},0){
        public boolean isCellEditable(int r,int c){return false;}};
    VitriPanel(){
        setLayout(new BorderLayout(0,12)); setBackground(C_BG); setBorder(pad(16));
        JLabel title=new JLabel("Danh Mục: Vị Trí Lưu Trữ"); title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));
        JPanel grp=titledGroup("Thêm vị trí lưu trữ");
        JTextField tTen=tf(16),tMoTa=tf(22);
        tTen.putClientProperty("JTextField.placeholderText","VD: Kệ A1, Tầng 1...");
        JComboBox<String> cboCha=new JComboBox<>();
        cboCha.addItem("-- Không có --");
        loadCombo(cboCha,"SELECT TenVT FROM VitriTL ORDER BY TenVT");
        JButton bAdd=btn("Thêm",C_WHITE,C_SUCCESS),bXoa=btn("Xóa",C_WHITE,C_DANGER);
        grp.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));
        grp.add(lbl("Tên vị trí *")); grp.add(tTen);
        grp.add(lbl("Mô tả"));        grp.add(tMoTa);
        grp.add(lbl("Vị trí cha"));   grp.add(cboCha);
        grp.add(bAdd); grp.add(bXoa);
        JTable tb=styledTable(md);
        JScrollPane sp=new JScrollPane(tb); sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        Runnable load=()->{md.setRowCount(0);
            try(Connection c=DBConnection.getConnection();ResultSet rs=c.createStatement().executeQuery(
                "SELECT v.*,p.TenVT AS TenCha FROM VitriTL v LEFT JOIN VitriTL p ON v.ChaVT=p.MaVT ORDER BY v.MaVT")){
                while(rs.next())md.addRow(new Object[]{rs.getInt(1),rs.getString("TenVT"),rs.getString("MoTa"),rs.getString("TenCha")});
            }catch(Exception e){}};
        load.run();
        bAdd.addActionListener(e->{if(tTen.getText().trim().isEmpty()){msg("Nhập tên vị trí!"); return;}
            try(Connection c=DBConnection.getConnection()){
                Integer chaId=cboCha.getSelectedIndex()>0?comboId(c,"VitriTL","MaVT","TenVT",cboCha):null;
                PreparedStatement ps=c.prepareStatement("INSERT INTO VitriTL(TenVT,MoTa,ChaVT) VALUES(?,?,?)");
                ps.setString(1,tTen.getText().trim());ps.setString(2,tMoTa.getText().trim());ps.setObject(3,chaId);
                ps.executeUpdate();msg("Thêm thành công!");tTen.setText("");tMoTa.setText("");load.run();
            }catch(Exception ex){msg("Lỗi!");}});
        bXoa.addActionListener(e->{int r=tb.getSelectedRow();if(r<0){msg("Chọn dòng!");return;}
            if(confirm("Xóa?")){try(Connection c=DBConnection.getConnection()){
                c.createStatement().executeUpdate("DELETE FROM VitriTL WHERE MaVT="+(int)md.getValueAt(r,0));
                msg("Xóa thành công!");load.run();}catch(Exception ex){msg("Không thể xóa!");}}});
        JPanel north=new JPanel(new BorderLayout(0,10)); north.setOpaque(false);
        north.add(title,BorderLayout.NORTH); north.add(grp,BorderLayout.CENTER);
        JLabel lbL=new JLabel("Danh sách"); lbL.setFont(new Font("Segoe UI",Font.BOLD,14));
        JPanel center=new JPanel(new BorderLayout(0,8)); center.setOpaque(false);
        center.add(lbL,BorderLayout.NORTH); center.add(sp,BorderLayout.CENTER);
        add(north,BorderLayout.NORTH); add(center,BorderLayout.CENTER);
    }
    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
    private boolean confirm(String s){return JOptionPane.showConfirmDialog(this,s,"Xác nhận",JOptionPane.YES_NO_OPTION)==0;}
}