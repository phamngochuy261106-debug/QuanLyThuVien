package com.library.dao;

import com.library.model.PhieuMuon;
import java.sql.*;
import java.util.ArrayList;

public class PhieuMuonDAO {

    public ArrayList<PhieuMuon> getAll() {
        ArrayList<PhieuMuon> ds = new ArrayList<>();
        String sql = "SELECT p.*, d.HoTen AS TenDG, s.TenSach FROM PhieuMuon p LEFT JOIN DocGia d ON p.MaDG=d.MaDG LEFT JOIN Sach s ON p.MaSach=s.MaSach ORDER BY p.MaPM DESC";
        try (Connection c = DBConnection.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                PhieuMuon p = new PhieuMuon();
                p.maPM = rs.getInt("MaPM"); p.maDG = rs.getInt("MaDG"); p.maSach = rs.getInt("MaSach");
                p.ngayMuon = rs.getDate("NgayMuon"); p.ngayHenTra = rs.getDate("NgayHenTra");
                p.ngayTra = rs.getDate("NgayTra"); p.trangThai = rs.getString("TrangThai");
                p.tenDG = rs.getString("TenDG"); p.tenSach = rs.getString("TenSach");
                ds.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean muonSach(int maDG, int maSach, java.util.Date ngayHenTra) {
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO PhieuMuon(MaDG,MaSach,NgayHenTra) VALUES(?,?,?)");
            ps.setInt(1, maDG); ps.setInt(2, maSach);
            ps.setDate(3, new java.sql.Date(ngayHenTra.getTime()));
            if (ps.executeUpdate() > 0) {
                c.createStatement().executeUpdate("UPDATE Sach SET ConLai=ConLai-1 WHERE MaSach=" + maSach);
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean traSach(int maPM, int maSach) {
        try (Connection c = DBConnection.getConnection()) {
            c.createStatement().executeUpdate("UPDATE PhieuMuon SET TrangThai=N'DaTra',NgayTra=GETDATE() WHERE MaPM=" + maPM);
            c.createStatement().executeUpdate("UPDATE Sach SET ConLai=ConLai+1 WHERE MaSach=" + maSach);
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int dem(String trangThai) {
        try (Connection c = DBConnection.getConnection();
             ResultSet rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM PhieuMuon WHERE TrangThai='" + trangThai + "'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}