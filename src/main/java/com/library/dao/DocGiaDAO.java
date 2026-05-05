package com.library.dao;

import com.library.model.DocGia;
import java.sql.*;
import java.util.ArrayList;

public class DocGiaDAO {

    public ArrayList<DocGia> getAll() {
        ArrayList<DocGia> ds = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); ResultSet rs = c.createStatement().executeQuery("SELECT * FROM DocGia ORDER BY MaDG")) {
            while (rs.next()) ds.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public ArrayList<DocGia> timKiem(String kw) {
        ArrayList<DocGia> ds = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM DocGia WHERE HoTen LIKE ? OR SDT LIKE ? OR SoThe LIKE ?")) {
            String k = "%" + kw + "%"; ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ds.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean them(DocGia d) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO DocGia(SoThe,HoTen,SDT,Email,DiaChi,HanSD,TinhTrang) VALUES(?,?,?,?,?,?,?)")) {
            ps.setString(1,d.soThe); ps.setString(2,d.hoTen); ps.setString(3,d.sdt); ps.setString(4,d.email); ps.setString(5,d.diaChi);
            ps.setDate(6, d.hanSD!=null ? new java.sql.Date(d.hanSD.getTime()) : null);
            ps.setString(7, d.tinhTrang!=null ? d.tinhTrang : "HoatDong");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean sua(DocGia d) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE DocGia SET SoThe=?,HoTen=?,SDT=?,Email=?,DiaChi=?,HanSD=?,TinhTrang=? WHERE MaDG=?")) {
            ps.setString(1,d.soThe); ps.setString(2,d.hoTen); ps.setString(3,d.sdt); ps.setString(4,d.email); ps.setString(5,d.diaChi);
            ps.setDate(6, d.hanSD!=null ? new java.sql.Date(d.hanSD.getTime()) : null);
            ps.setString(7,d.tinhTrang); ps.setInt(8,d.maDG);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean xoa(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM DocGia WHERE MaDG=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private DocGia map(ResultSet rs) throws SQLException {
        DocGia d = new DocGia();
        d.maDG = rs.getInt("MaDG"); d.soThe = rs.getString("SoThe");
        d.hoTen = rs.getString("HoTen"); d.sdt = rs.getString("SDT");
        d.email = rs.getString("Email"); d.diaChi = rs.getString("DiaChi");
        d.ngayCap = rs.getDate("NgayCap"); d.hanSD = rs.getDate("HanSD");
        d.tinhTrang = rs.getString("TinhTrang");
        return d;
    }
}