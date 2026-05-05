package com.library.dao;

import com.library.model.Sach;
import java.sql.*;
import java.util.ArrayList;

public class SachDAO {

    public ArrayList<Sach> getAll() {
        ArrayList<Sach> ds = new ArrayList<>();
        String sql = "SELECT s.*, t.TenTL FROM Sach s LEFT JOIN TheLoai t ON s.MaTL=t.MaTL";
        try (Connection c = DBConnection.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) ds.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public ArrayList<Sach> timKiem(String kw) {
        ArrayList<Sach> ds = new ArrayList<>();
        String sql = "SELECT s.*, t.TenTL FROM Sach s LEFT JOIN TheLoai t ON s.MaTL=t.MaTL WHERE s.TenSach LIKE ? OR s.TacGia LIKE ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + kw + "%"); ps.setString(2, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ds.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean them(Sach s) {
        String sql = "INSERT INTO Sach(TenSach,TacGia,MaTL,NXB,NamXB,SoLuong,ConLai,Gia) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,s.tenSach); ps.setString(2,s.tacGia); ps.setInt(3,s.maTL);
            ps.setString(4,s.nxb); ps.setInt(5,s.namXB); ps.setInt(6,s.soLuong);
            ps.setInt(7,s.conLai); ps.setDouble(8,s.gia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean sua(Sach s) {
        String sql = "UPDATE Sach SET TenSach=?,TacGia=?,MaTL=?,NXB=?,NamXB=?,SoLuong=?,ConLai=?,Gia=? WHERE MaSach=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,s.tenSach); ps.setString(2,s.tacGia); ps.setInt(3,s.maTL);
            ps.setString(4,s.nxb); ps.setInt(5,s.namXB); ps.setInt(6,s.soLuong);
            ps.setInt(7,s.conLai); ps.setDouble(8,s.gia); ps.setInt(9,s.maSach);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean xoa(int id) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM Sach WHERE MaSach=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Sach map(ResultSet rs) throws SQLException {
        Sach s = new Sach();
        s.maSach = rs.getInt("MaSach"); s.tenSach = rs.getString("TenSach");
        s.tacGia = rs.getString("TacGia"); s.maTL = rs.getInt("MaTL");
        s.nxb = rs.getString("NXB"); s.namXB = rs.getInt("NamXB");
        s.soLuong = rs.getInt("SoLuong"); s.conLai = rs.getInt("ConLai");
        s.gia = rs.getDouble("Gia");
        try { s.tenTL = rs.getString("TenTL"); } catch (Exception ignored) {}
        return s;
    }
}