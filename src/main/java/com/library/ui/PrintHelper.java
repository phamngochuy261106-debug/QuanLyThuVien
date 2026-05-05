package com.library.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PrintHelper {

    // ── In phiếu mượn ────────────────────────────────────────────────
    public static void inPhieuMuon(JFrame parent,
            String soThe, String hoTen, String donVi,
            String maCB, String tenTL, String vitri,
            String kieuMuon, String hanTra, String nguoiChoMuon) {

        String ngayMuon = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        String content =
            "           THƯ VIỆN TRƯỜNG ĐẠI HỌC\n" +
            "        PHẦN MỀM QUẢN LÝ THƯ VIỆN\n" +
            "  ==========================================\n\n" +
            "             PHIẾU MƯỢN TÀI LIỆU\n\n" +
            "  Số thẻ       : " + soThe       + "\n" +
            "  Họ tên       : " + hoTen       + "\n" +
            "  Đơn vị       : " + (donVi!=null?donVi:"") + "\n\n" +
            "  ------------------------------------------\n" +
            "  Mã cá biệt   : " + maCB        + "\n" +
            "  Tên tài liệu : " + tenTL       + "\n" +
            "  Vị trí lưu   : " + (vitri!=null?vitri:"") + "\n" +
            "  Hình thức    : " + kieuMuon    + "\n" +
            "  ------------------------------------------\n\n" +
            "  Ngày mượn    : " + ngayMuon    + "\n" +
            "  Hạn trả      : " + hanTra      + "\n" +
            "  Người cho mượn: " + nguoiChoMuon + "\n\n" +
            "  Lưu ý: Vui lòng trả đúng hạn.\n" +
            "         Quá hạn sẽ bị xử phạt.\n\n" +
            "  ==========================================\n" +
            "  Chữ ký độc giả        Chữ ký thủ thư\n\n\n" +
            "  ......................  ....................\n";

        showPrintDialog(parent, "Phiếu Mượn Tài Liệu", content);
    }

    // ── In phiếu trả ────────────────────────────────────────────────
    public static void inPhieuTra(JFrame parent,
            String soThe, String hoTen,
            String maCB, String tenTL,
            String ngayMuon, String hanTra, String tinhTrang) {

        String ngayTra = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        boolean quaHan = false;
        try {
            Date han = new SimpleDateFormat("dd/MM/yyyy").parse(hanTra);
            quaHan = han.before(new Date());
        } catch (Exception ignored) {}

        String content =
            "           THƯ VIỆN TRƯỜNG ĐẠI HỌC\n" +
            "        PHẦN MỀM QUẢN LÝ THƯ VIỆN\n" +
            "  ==========================================\n\n" +
            "             PHIẾU TRẢ TÀI LIỆU\n\n" +
            "  Số thẻ       : " + soThe       + "\n" +
            "  Họ tên       : " + hoTen       + "\n\n" +
            "  ------------------------------------------\n" +
            "  Mã cá biệt   : " + maCB        + "\n" +
            "  Tên tài liệu : " + tenTL       + "\n" +
            "  Tình trạng   : " + (tinhTrang!=null?tinhTrang:"") + "\n" +
            "  ------------------------------------------\n\n" +
            "  Ngày mượn    : " + ngayMuon    + "\n" +
            "  Hạn trả      : " + hanTra      + (quaHan?" (QUÁ HẠN)":"") + "\n" +
            "  Ngày trả     : " + ngayTra     + "\n\n" +
            (quaHan ?
            "  *** Tài liệu trả TRỄ HẠN. Vui lòng\n" +
            "  *** liên hệ thủ thư để xử lý phạt.\n\n" : "") +
            "  ==========================================\n" +
            "  Chữ ký độc giả        Chữ ký thủ thư\n\n\n" +
            "  ......................  ....................\n";

        showPrintDialog(parent, "Phiếu Trả Tài Liệu", content);
    }

    // ── In phiếu phạt ────────────────────────────────────────────────
    public static void inPhieuPhat(JFrame parent,
            String soThe, String hoTen,
            String tenTL, String hanTra,
            int soNgayTre, double soTien, String htXuLy) {

        String ngayPhat = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        String content =
            "           THƯ VIỆN TRƯỜNG ĐẠI HỌC\n" +
            "        PHẦN MỀM QUẢN LÝ THƯ VIỆN\n" +
            "  ==========================================\n\n" +
            "              PHIẾU XỬ PHẠT\n\n" +
            "  Số thẻ       : " + soThe    + "\n" +
            "  Họ tên       : " + hoTen    + "\n\n" +
            "  ------------------------------------------\n" +
            "  Tên tài liệu : " + tenTL    + "\n" +
            "  Hạn trả      : " + hanTra   + "\n" +
            "  Số ngày trễ  : " + soNgayTre + " ngày\n" +
            "  ------------------------------------------\n\n" +
            "  Hình thức XL : " + htXuLy   + "\n" +
            "  Số tiền phạt : " + String.format("%,.0f", soTien) + " VNĐ\n" +
            "  Ngày lập     : " + ngayPhat  + "\n\n" +
            "  Lưu ý: Nộp phạt trong vòng 7 ngày.\n\n" +
            "  ==========================================\n" +
            "  Chữ ký độc giả        Chữ ký thủ thư\n\n\n" +
            "  ......................  ....................\n";

        showPrintDialog(parent, "Phiếu Xử Phạt", content);
    }

    // ── Hiển thị dialog preview + in ────────────────────────────────
    public static void showPrintDialogPublic(JComponent parent, String title, String content) {
        showPrintDialog(null, title, content);
    }

    private static void showPrintDialog(JFrame parent, String title, String content) {
        JDialog dlg = new JDialog(parent, title, true);
        dlg.setSize(480, 580);
        dlg.setLocationRelativeTo(parent);

        // Text preview
        JTextArea ta = new JTextArea(content);
        ta.setFont(new Font("Courier New", Font.PLAIN, 13));
        ta.setEditable(false);
        ta.setBackground(Color.WHITE);
        ta.setMargin(new Insets(16, 20, 16, 20));

        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(null);

        // Buttons
        JButton bIn    = new JButton("In ngay");
        JButton bDong  = new JButton("Đóng");

        bIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bIn.setBackground(new Color(41, 98, 218));
        bIn.setForeground(Color.WHITE);
        bIn.setFocusPainted(false);
        bIn.setBorderPainted(false);
        bIn.setPreferredSize(new Dimension(120, 36));
        bIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        bDong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bDong.setPreferredSize(new Dimension(90, 36));

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnP.setBackground(new Color(245, 247, 250));
        btnP.setBorder(javax.swing.BorderFactory.createMatteBorder(1,0,0,0,
            new Color(218,225,237)));
        btnP.add(bDong); btnP.add(bIn);

        dlg.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 98, 218));
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbTitle.setForeground(Color.WHITE);
        header.add(lbTitle, BorderLayout.WEST);

        dlg.add(header, BorderLayout.NORTH);
        dlg.add(sp,     BorderLayout.CENTER);
        dlg.add(btnP,   BorderLayout.SOUTH);

        // In
        bIn.addActionListener(e -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName(title);
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                graphics.translate(
                    (int) pageFormat.getImageableX(),
                    (int) pageFormat.getImageableY()
                );
                // Scale cho vừa trang
                double scale = Math.min(
                    pageFormat.getImageableWidth()  / ta.getWidth(),
                    pageFormat.getImageableHeight() / ta.getHeight()
                );
                ((Graphics2D) graphics).scale(scale, scale);
                ta.printAll(graphics);
                return Printable.PAGE_EXISTS;
            });
            if (job.printDialog()) {
                try { job.print(); }
                catch (PrinterException ex) {
                    JOptionPane.showMessageDialog(dlg, "Lỗi in: " + ex.getMessage());
                }
            }
        });

        bDong.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }
}