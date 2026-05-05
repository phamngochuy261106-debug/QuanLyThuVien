package com.library.ui;

import com.library.dao.DBConnection;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField txtUser = new JTextField(18);
    private JPasswordField txtPass = new JPasswordField(18);

    public LoginFrame() {
        setTitle("Phần Mềm Quản Lý Thư Viện");
        setSize(860, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new GridLayout(1, 2));

        // ── Bên trái: ảnh thư viện ───────────────────────────
        JPanel left = new JPanel(new BorderLayout()) {
            BufferedImage img = loadLibraryImage();

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (img != null) {
                    // Ve anh nen
                    g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                    // Overlay toi de chu noi bat
                    g2.setColor(new Color(0, 0, 0, 110));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    // Fallback gradient neu khong co anh
                    g2.setPaint(new GradientPaint(0, 0,
                        new Color(15, 52, 96), getWidth(), getHeight(),
                        new Color(26, 82, 118)));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                // Ve hinh sach don gian bang Graphics2D (khong dung emoji)
                int cx = getWidth()/2;
                g2.setColor(new Color(255,255,255,160));
                // Ve 3 cuon sach
                int bx = cx-45, by = 115, bh = 55;
                int[] bw = {22,18,20}; int[] gap = {0,25,46};
                for(int i=0;i<3;i++){
                    g2.setColor(new Color(255,255,255, i==1?200:150));
                    g2.fillRoundRect(bx+gap[i], by, bw[i], bh, 3, 3);
                    g2.setColor(new Color(0,0,0,60));
                    g2.drawRoundRect(bx+gap[i], by, bw[i], bh, 3, 3);
                }
                FontMetrics fm = g2.getFontMetrics();

                // Ten phan mem - bo chu Thu Vien
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                fm = g2.getFontMetrics();
                String line1 = "PHẦN MỀM";
                String line2 = "QUẢN LÝ THƯ VIỆN";
                g2.drawString(line1,
                    (getWidth() - fm.stringWidth(line1)) / 2, 230);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                fm = g2.getFontMetrics();
                g2.drawString(line2,
                    (getWidth() - fm.stringWidth(line2)) / 2, 260);

                // Dong tagline
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                g2.setColor(new Color(255, 255, 255, 180));
                fm = g2.getFontMetrics();
                String tag = "Library Management System";
                g2.drawString(tag,
                    (getWidth() - fm.stringWidth(tag)) / 2, 295);

                // Duong ke trang tri
                g2.setColor(new Color(255, 255, 255, 80));
                g2.setStroke(new BasicStroke(1f));
                int mx = getWidth()/2;
                g2.drawLine(mx-60, 310, mx+60, 310);
            }
        };

        // ── Bên phải: form đăng nhập ─────────────────────────
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(6, 30, 6, 30);
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.gridwidth = 2;

        JLabel lbTitle = new JLabel("Đăng Nhập");
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lbTitle.setForeground(new Color(30, 41, 59));
        g.gridy=0; g.insets=new Insets(20,30,16,30); right.add(lbTitle, g);
        g.insets=new Insets(4,30,4,30);

        JLabel lbUser = new JLabel("Tên đăng nhập");
        lbUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbUser.setForeground(new Color(71, 85, 105));
        g.gridy=1; right.add(lbUser, g);

        txtUser.setPreferredSize(new Dimension(0, 38));
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUser.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập...");
        g.gridy=2; right.add(txtUser, g);

        JLabel lbPass = new JLabel("Mật khẩu");
        lbPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbPass.setForeground(new Color(71, 85, 105));
        g.gridy=3; right.add(lbPass, g);

        txtPass.setPreferredSize(new Dimension(0, 38));
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPass.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu...");
        g.gridy=4; right.add(txtPass, g);

        g.gridy=5; g.insets=new Insets(14,30,6,30);
        JButton btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setBackground(new Color(37, 99, 235));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(0, 42));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnLogin.setBackground(new Color(29, 78, 216));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnLogin.setBackground(new Color(37, 99, 235));
            }
        });
        right.add(btnLogin, g);

        g.insets=new Insets(6,30,4,30);
        JLabel hint = new JLabel("Tài khoản mặc định: admin / admin123", SwingConstants.CENTER);
        hint.setForeground(new Color(148, 163, 184));
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        g.gridy=6; right.add(hint, g);

        btnLogin.addActionListener(e -> login());
        txtPass.addActionListener(e -> login());

        main.add(left);
        main.add(right);
        setContentPane(main);
    }

    private BufferedImage loadLibraryImage() {
        // Thu tai anh thu vien tu internet
        try {
            URL url = new URL("https://images.unsplash.com/photo-1507842217343-583bb7270b66?w=500&q=80");
            return ImageIO.read(url);
        } catch (Exception e1) {
            // Thu anh khac
            try {
                URL url = new URL("https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=500&q=80");
                return ImageIO.read(url);
            } catch (Exception e2) {
                return null; // Dung gradient fallback
            }
        }
    }

    private void login() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());
        if (user.isEmpty() || pass.isEmpty()) { msg("Nhập đầy đủ thông tin!"); return; }

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT HoTen,VaiTro FROM TaiKhoan WHERE TenDN=? AND MatKhau=?")) {
            ps.setString(1, user); ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                dispose();
                new MainFrame(rs.getString("HoTen"), rs.getString("VaiTro")).setVisible(true);
            } else {
                msg("Sai tài khoản hoặc mật khẩu!");
                txtPass.setText(""); txtPass.requestFocus();
            }
        } catch (SQLException ex) {
            msg("Lỗi kết nối database!\n" + ex.getMessage());
        }
    }

    private void msg(String s) { JOptionPane.showMessageDialog(this, s); }
}