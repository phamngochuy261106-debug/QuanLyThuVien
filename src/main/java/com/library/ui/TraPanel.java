package com.library.ui;

import com.library.dao.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import static com.library.ui.UIConstants.*;

public class TraPanel extends JPanel {

    private final String currentUser;
    private final DefaultTableModel md = new DefaultTableModel(
        new String[]{"ID","Số thẻ","Họ tên","Mã CB","Tên tài liệu","Kiểu mượn","Tình trạng","Ngày mượn","Người cho mượn","Hạn trả"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    public TraPanel(String currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 12));
        setBackground(C_BG); setBorder(pad(16));

        // ── NORTH: Tiêu đề + Form ─────────────────────────────
        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);

        JLabel title = new JLabel("Trả Tài Liệu");
        title.setFont(F_TITLE); title.setForeground(new Color(30,41,59));
        north.add(title, BorderLayout.NORTH);

        JPanel grp = titledGroup("Thông tin trả tài liệu");
        grp.setLayout(new GridBagLayout()); GridBagConstraints g = gbc();

        JTextField tSoThe=tf(14), tMaCB=tf(12), tCapNhat=tf(16);
        tSoThe.putClientProperty("JTextField.placeholderText","Số thẻ hoặc click dòng bên dưới");
        tMaCB.putClientProperty("JTextField.placeholderText","Mã cá biệt");
        tCapNhat.putClientProperty("JTextField.placeholderText","VD: Tốt, Rách nhẹ...");

        g.gridx=0;g.gridy=0;g.weightx=0; grp.add(lbl("Số thẻ"),g);
        g.gridx=1;g.weightx=1; grp.add(tSoThe,g);
        g.gridx=2;g.weightx=0; grp.add(lbl("Mã cá biệt *"),g);
        g.gridx=3;g.weightx=1; grp.add(tMaCB,g);
        g.gridx=4;g.weightx=0; grp.add(lbl("Cập nhật tình trạng TL"),g);
        g.gridx=5;g.weightx=1; grp.add(tCapNhat,g);

        JButton bNew = btn("Tạo mới",      C_WHITE, new Color(108,117,125));
        JButton bIn  = btn("In phiếu",  C_WHITE, new Color(80,130,180));
        JButton bTra = btn("Xác nhận trả", C_WHITE, C_SUCCESS);
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); bp.setOpaque(false);
        bp.add(bNew); bp.add(bIn); bp.add(bTra);
        g.gridx=0;g.gridy=1;g.gridwidth=6; grp.add(bp,g);

        north.add(grp, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        // ── CENTER: Bảng đang mượn ────────────────────────────
        JLabel lbList = new JLabel("Danh sách tài liệu đang mượn (click để chọn)");
        lbList.setFont(new Font("Segoe UI",Font.BOLD,14));
        lbList.setForeground(new Color(30,41,59));

        JTable tb = styledTable(md);
        tb.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                JLabel lb=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if(!sel){try{Date han=new SimpleDateFormat("dd/MM/yyyy").parse(v!=null?v.toString():"01/01/2099");
                    if(han.before(new Date())){lb.setForeground(C_DANGER);lb.setFont(F_BOLD);}
                    else lb.setForeground(Color.BLACK);}catch(Exception ignored){}}
                return lb;
            }
        });

        Runnable loadList = () -> {
            md.setRowCount(0); SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
            try(Connection c=DBConnection.getConnection(); ResultSet rs=c.createStatement().executeQuery(
                "SELECT m.ID_MuonTra,m.SoThe,d.HoTen,m.MaCaBiet,tl.TenTL,m.KieuMuon,tc.TinhTrang,"
                +"m.NgayMuon,m.NguoiChoMuon,m.HanTra FROM MuonTra m"
                +" LEFT JOIN DocGia d ON m.SoThe=d.SoThe"
                +" LEFT JOIN TLChitiet tc ON m.MaCaBiet=tc.MaCaBiet"
                +" LEFT JOIN TaiLieu tl ON tc.MaTL=tl.MaTL"
                +" WHERE m.NgayTra IS NULL ORDER BY m.HanTra")){
                while(rs.next())
                    md.addRow(new Object[]{rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),
                        rs.getString(5),rs.getString(6),rs.getString(7),
                        fmt(sdf,rs.getDate(8)),rs.getString(9),fmt(sdf,rs.getDate(10))});
            }catch(Exception e){e.printStackTrace();}
        };
        loadList.run();

        tb.getSelectionModel().addListSelectionListener(e -> {
            int r=tb.getSelectedRow(); if(r<0) return;
            tSoThe.setText(md.getValueAt(r,1)!=null?md.getValueAt(r,1).toString():"");
            tMaCB.setText(md.getValueAt(r,3)!=null?md.getValueAt(r,3).toString():"");
        });

        bNew.addActionListener(e -> { tSoThe.setText(""); tMaCB.setText(""); tCapNhat.setText(""); tb.clearSelection(); });

        bIn.addActionListener(e -> {
            int selRow = tb.getSelectedRow();
            if(selRow < 0){ javax.swing.JOptionPane.showMessageDialog(null,"Click chọn dòng cần in phiếu!"); return; }
            String ngayMuon = md.getValueAt(selRow,7)!=null?md.getValueAt(selRow,7).toString():"";
            String hanTra   = md.getValueAt(selRow,9)!=null?md.getValueAt(selRow,9).toString():"";
            PdfExporter.xuatPhieuTra(
                TraPanel.this,
                md.getValueAt(selRow,1)!=null?md.getValueAt(selRow,1).toString():"",
                md.getValueAt(selRow,2)!=null?md.getValueAt(selRow,2).toString():"",
                md.getValueAt(selRow,3)!=null?md.getValueAt(selRow,3).toString():"",
                md.getValueAt(selRow,4)!=null?md.getValueAt(selRow,4).toString():"",
                ngayMuon, hanTra,
                md.getValueAt(selRow,6)!=null?md.getValueAt(selRow,6).toString():""
            );
        });
        bTra.addActionListener(e -> {
            if(tMaCB.getText().trim().isEmpty()){msg("Nhập mã cá biệt!"); return;}
            if(confirm("Xác nhận trả tài liệu (Mã CB: "+tMaCB.getText().trim()+")?")) {
                try(Connection c=DBConnection.getConnection()){
                    int maCB=Integer.parseInt(tMaCB.getText().trim());
                    ResultSet rs=c.createStatement().executeQuery("SELECT ID_MuonTra FROM MuonTra WHERE MaCaBiet="+maCB+" AND NgayTra IS NULL");
                    if(!rs.next()){msg("Không tìm thấy phiếu mượn còn hiệu lực!"); return;}
                    c.createStatement().executeUpdate("UPDATE MuonTra SET NgayTra=GETDATE(),NguoiNhan=N'"+currentUser+"',Loai='Tra' WHERE MaCaBiet="+maCB+" AND NgayTra IS NULL");
                    if(!tCapNhat.getText().trim().isEmpty())
                        c.createStatement().executeUpdate("UPDATE TLChitiet SET TinhTrang=N'"+tCapNhat.getText().trim()+"',NguoiCN=N'"+currentUser+"',NgayCN=GETDATE() WHERE MaCaBiet="+maCB);
                    msg("Trả tài liệu thành công!"); loadList.run(); bNew.doClick();
                }catch(NumberFormatException nfe){msg("Mã cá biệt phải là số!");}
                catch(Exception ex){msg("Lỗi: "+ex.getMessage());}
            }
        });

        JScrollPane sp=new JScrollPane(tb); sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        JPanel center=new JPanel(new BorderLayout(0,8)); center.setOpaque(false);
        center.add(lbList,BorderLayout.NORTH); center.add(sp,BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
    private boolean confirm(String s){return JOptionPane.showConfirmDialog(this,s,"Xác nhận",JOptionPane.YES_NO_OPTION)==0;}
}