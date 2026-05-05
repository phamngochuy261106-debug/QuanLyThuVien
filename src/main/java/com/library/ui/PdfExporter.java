package com.library.ui;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;

import javax.swing.*;
import java.awt.Desktop;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PdfExporter {

    private static final DeviceRgb C_BLUE  = new DeviceRgb(41,  98,  218);
    private static final DeviceRgb C_DBLUE = new DeviceRgb(31,  56,  100);
    private static final DeviceRgb C_RED   = new DeviceRgb(180, 60,  60);
    private static final DeviceRgb C_GRAY  = new DeviceRgb(245, 247, 250);
    private static final DeviceRgb C_LINE  = new DeviceRgb(200, 210, 225);

    // ── Xuất phiếu mượn ─────────────────────────────────────────────
    public static void xuatPhieuMuon(JComponent parent,
            String soThe, String hoTen, String donVi,
            String maCB, String tenTL, String vitri,
            String kieuMuon, String hanTra, String nguoiChoMuon) {

        String ten = "PhieuMuon_" + soThe + "_" + System.currentTimeMillis() + ".pdf";
        File file = chonThuMuc(parent, ten);
        if (file == null) return;

        try {
            PdfFont fontR = PdfFontFactory.createFont("c:/windows/fonts/times.ttf",
                PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            PdfFont fontB = PdfFontFactory.createFont("c:/windows/fonts/timesbd.ttf",
                PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

            PdfDocument pdf = new PdfDocument(new PdfWriter(file));
            Document doc = new Document(pdf, PageSize.A5);
            doc.setMargins(36, 36, 36, 36);

            String ngayMuon = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

            // Header
            addHeader(doc, fontB, "PHIẾU MƯỢN TÀI LIỆU", C_BLUE);

            // Thông tin độc giả
            addSectionTitle(doc, fontB, "Thông tin độc giả");
            addRow(doc, fontR, fontB, "Số thẻ",        soThe);
            addRow(doc, fontR, fontB, "Họ tên",         hoTen);
            addRow(doc, fontR, fontB, "Đơn vị",         donVi != null ? donVi : "");

            doc.add(new Paragraph("\n").setFontSize(4));
            addSectionTitle(doc, fontB, "Thông tin tài liệu");
            addRow(doc, fontR, fontB, "Mã cá biệt",     maCB);
            addRow(doc, fontR, fontB, "Tên tài liệu",   tenTL);
            addRow(doc, fontR, fontB, "Vị trí lưu trữ", vitri != null ? vitri : "");
            addRow(doc, fontR, fontB, "Hình thức",       kieuMuon);

            doc.add(new Paragraph("\n").setFontSize(4));
            addSectionTitle(doc, fontB, "Thông tin mượn");
            addRow(doc, fontR, fontB, "Ngày mượn",      ngayMuon);
            addRow(doc, fontR, fontB, "Hạn trả",        hanTra);
            addRow(doc, fontR, fontB, "Người cho mượn", nguoiChoMuon);

            // Ghi chú
            doc.add(new Paragraph("\n").setFontSize(6));
            Paragraph note = new Paragraph("⚠ Vui lòng trả đúng hạn. Quá hạn sẽ bị xử phạt theo quy định.")
                .setFont(fontR).setFontSize(9).setFontColor(C_RED)
                .setTextAlignment(TextAlignment.CENTER);
            doc.add(note);

            // Chữ ký
            addChuKy(doc, fontR);

            doc.close();
            moFile(parent, file);
        } catch (Exception e) {
            xuatDuPhong(parent, "Phiếu Mượn", buildPhieuMuonText(soThe,hoTen,donVi,maCB,tenTL,vitri,kieuMuon,hanTra,nguoiChoMuon));
        }
    }

    // ── Xuất phiếu trả ──────────────────────────────────────────────
    public static void xuatPhieuTra(JComponent parent,
            String soThe, String hoTen,
            String maCB, String tenTL,
            String ngayMuon, String hanTra, String tinhTrang) {

        String ten = "PhieuTra_" + soThe + "_" + System.currentTimeMillis() + ".pdf";
        File file = chonThuMuc(parent, ten);
        if (file == null) return;

        boolean quaHan = false;
        try { quaHan = new SimpleDateFormat("dd/MM/yyyy")
            .parse(hanTra).before(new Date()); } catch (Exception ignored) {}
        String ngayTra = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        try {
            PdfFont fontR = PdfFontFactory.createFont("c:/windows/fonts/times.ttf",
                PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            PdfFont fontB = PdfFontFactory.createFont("c:/windows/fonts/timesbd.ttf",
                PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

            PdfDocument pdf = new PdfDocument(new PdfWriter(file));
            Document doc = new Document(pdf, PageSize.A5);
            doc.setMargins(36, 36, 36, 36);

            addHeader(doc, fontB, "PHIẾU TRẢ TÀI LIỆU",
                quaHan ? C_RED : C_BLUE);

            addSectionTitle(doc, fontB, "Thông tin độc giả");
            addRow(doc, fontR, fontB, "Số thẻ",  soThe);
            addRow(doc, fontR, fontB, "Họ tên",   hoTen);

            doc.add(new Paragraph("\n").setFontSize(4));
            addSectionTitle(doc, fontB, "Thông tin tài liệu");
            addRow(doc, fontR, fontB, "Mã cá biệt",   maCB);
            addRow(doc, fontR, fontB, "Tên tài liệu", tenTL);
            addRow(doc, fontR, fontB, "Tình trạng",   tinhTrang != null ? tinhTrang : "");

            doc.add(new Paragraph("\n").setFontSize(4));
            addSectionTitle(doc, fontB, "Thông tin trả");
            addRow(doc, fontR, fontB, "Ngày mượn", ngayMuon);
            addRow(doc, fontR, fontB, "Hạn trả",
                hanTra + (quaHan ? "  ← QUÁ HẠN" : ""));
            addRow(doc, fontR, fontB, "Ngày trả",  ngayTra);

            if (quaHan) {
                doc.add(new Paragraph("\n").setFontSize(4));
                Paragraph warn = new Paragraph(
                    "⚠ Tài liệu trả TRỄ HẠN. Vui lòng liên hệ thủ thư để xử lý phạt.")
                    .setFont(fontR).setFontSize(9).setFontColor(C_RED)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorder(new SolidBorder(C_RED, 1f))
                    .setPadding(6);
                doc.add(warn);
            }

            addChuKy(doc, fontR);
            doc.close();
            moFile(parent, file);
        } catch (Exception e) {
            xuatDuPhong(parent, "Phiếu Trả",
                buildPhieuTraText(soThe,hoTen,maCB,tenTL,ngayMuon,hanTra,tinhTrang,quaHan));
        }
    }

    // ── Xuất phiếu phạt ─────────────────────────────────────────────
    public static void xuatPhieuPhat(JComponent parent,
            String soThe, String hoTen,
            String tenTL, String hanTra,
            int soNgayTre, double soTien, String htXuLy) {

        String ten = "PhieuPhat_" + soThe + "_" + System.currentTimeMillis() + ".pdf";
        File file = chonThuMuc(parent, ten);
        if (file == null) return;

        String ngayPhat = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        try {
            PdfFont fontR = PdfFontFactory.createFont("c:/windows/fonts/times.ttf",
                PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            PdfFont fontB = PdfFontFactory.createFont("c:/windows/fonts/timesbd.ttf",
                PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

            PdfDocument pdf = new PdfDocument(new PdfWriter(file));
            Document doc = new Document(pdf, PageSize.A5);
            doc.setMargins(36, 36, 36, 36);

            addHeader(doc, fontB, "PHIẾU XỬ PHẠT", C_RED);

            addSectionTitle(doc, fontB, "Thông tin độc giả");
            addRow(doc, fontR, fontB, "Số thẻ", soThe);
            addRow(doc, fontR, fontB, "Họ tên",  hoTen);

            doc.add(new Paragraph("\n").setFontSize(4));
            addSectionTitle(doc, fontB, "Thông tin vi phạm");
            addRow(doc, fontR, fontB, "Tên tài liệu",   tenTL);
            addRow(doc, fontR, fontB, "Hạn trả",        hanTra);
            addRow(doc, fontR, fontB, "Số ngày trễ",    soNgayTre + " ngày");
            addRow(doc, fontR, fontB, "Hình thức XL",   htXuLy);

            doc.add(new Paragraph("\n").setFontSize(4));
            addSectionTitle(doc, fontB, "Thông tin phạt");
            addRow(doc, fontR, fontB, "Ngày lập phiếu", ngayPhat);

            // Tiền phạt nổi bật
            Paragraph tien = new Paragraph("Số tiền phạt: " +
                String.format("%,.0f", soTien) + " VNĐ")
                .setFont(fontB).setFontSize(14)
                .setFontColor(C_RED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(8).setMarginBottom(8);
            doc.add(tien);

            Paragraph note = new Paragraph("Vui lòng nộp phạt trong vòng 7 ngày làm việc.")
                .setFont(fontR).setFontSize(9).setFontColor(C_DBLUE)
                .setTextAlignment(TextAlignment.CENTER);
            doc.add(note);

            addChuKy(doc, fontR);
            doc.close();
            moFile(parent, file);
        } catch (Exception e) {
            xuatDuPhong(parent, "Phiếu Phạt",
                buildPhieuPhatText(soThe,hoTen,tenTL,hanTra,soNgayTre,soTien,htXuLy));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private static void addHeader(Document doc, PdfFont fontB, String title, DeviceRgb color) throws Exception {
        // Dòng tên trường
        doc.add(new Paragraph("TRƯỜNG ĐẠI HỌC GIAO THÔNG VẬN TẢI")
            .setFont(fontB).setFontSize(10).setFontColor(C_DBLUE)
            .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
        doc.add(new Paragraph("THƯ VIỆN TRƯỜNG")
            .setFont(fontB).setFontSize(9).setFontColor(C_DBLUE)
            .setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

        // Đường kẻ
        doc.add(new Paragraph("─────────────────────────────────────")
            .setFontSize(8).setFontColor(C_LINE).setTextAlignment(TextAlignment.CENTER).setMarginBottom(8));

        // Tiêu đề phiếu
        doc.add(new Paragraph(title)
            .setFont(fontB).setFontSize(18).setFontColor(color)
            .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));

        doc.add(new Paragraph("─────────────────────────────────────")
            .setFontSize(8).setFontColor(C_LINE).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));
    }

    private static void addSectionTitle(Document doc, PdfFont fontB, String title) throws Exception {
        doc.add(new Paragraph(title)
            .setFont(fontB).setFontSize(10).setFontColor(C_BLUE)
            .setMarginBottom(3));
    }

    private static void addRow(Document doc, PdfFont fontR, PdfFont fontB,
                               String label, String value) throws Exception {
        Table t = new Table(new float[]{120, 230});
        t.setWidth(UnitValue.createPercentValue(100));
        t.setMarginBottom(3);

        Cell cLabel = new Cell().add(new Paragraph(label + ":")
            .setFont(fontB).setFontSize(10).setFontColor(new DeviceRgb(60,60,60)))
            .setBorder(Border.NO_BORDER)
            .setPaddingLeft(8).setPaddingBottom(1).setPaddingTop(1);

        Cell cVal = new Cell().add(new Paragraph(value != null ? value : "")
            .setFont(fontR).setFontSize(10))
            .setBorder(Border.NO_BORDER)
            .setPaddingBottom(1).setPaddingTop(1);

        t.addCell(cLabel); t.addCell(cVal);
        doc.add(t);
    }

    private static void addChuKy(Document doc, PdfFont fontR) throws Exception {
        doc.add(new Paragraph("\n").setFontSize(8));
        Table t = new Table(new float[]{180, 180});
        t.setWidth(UnitValue.createPercentValue(100)).setMarginTop(10);

        Cell c1 = new Cell().add(new Paragraph("Chữ ký độc giả")
            .setFont(fontR).setFontSize(9).setTextAlignment(TextAlignment.CENTER)
            .setFontColor(new DeviceRgb(80,80,80)))
            .setBorder(Border.NO_BORDER);
        Cell c2 = new Cell().add(new Paragraph("Chữ ký thủ thư")
            .setFont(fontR).setFontSize(9).setTextAlignment(TextAlignment.CENTER)
            .setFontColor(new DeviceRgb(80,80,80)))
            .setBorder(Border.NO_BORDER);

        Cell s1 = new Cell().add(new Paragraph("\n\n...............................")
            .setFont(fontR).setFontSize(9).setTextAlignment(TextAlignment.CENTER))
            .setBorder(Border.NO_BORDER);
        Cell s2 = new Cell().add(new Paragraph("\n\n...............................")
            .setFont(fontR).setFontSize(9).setTextAlignment(TextAlignment.CENTER))
            .setBorder(Border.NO_BORDER);

        t.addCell(c1); t.addCell(c2);
        t.addCell(s1); t.addCell(s2);
        doc.add(t);
    }

    private static File chonThuMuc(JComponent parent, String tenFile) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(System.getProperty("user.home")
            + File.separator + "Downloads" + File.separator + tenFile));
        fc.setDialogTitle("Lưu file PDF");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files","pdf"));
        int r = fc.showSaveDialog(parent);
        if (r != JFileChooser.APPROVE_OPTION) return null;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".pdf"))
            f = new File(f.getAbsolutePath() + ".pdf");
        return f;
    }

    private static void moFile(JComponent parent, File file) {
        try {
            Desktop.getDesktop().open(file);
            JOptionPane.showMessageDialog(parent,
                "✓ Xuất PDF thành công!\n" + file.getAbsolutePath(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                "✓ Đã lưu PDF tại:\n" + file.getAbsolutePath(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Fallback nếu thiếu font (in popup) ──────────────────────────
    private static void xuatDuPhong(JComponent parent, String title, String content) {
        PrintHelper.showPrintDialogPublic(parent, title, content);
    }

    private static String buildPhieuMuonText(String soThe, String hoTen, String donVi,
        String maCB, String tenTL, String vitri, String kieuMuon, String hanTra, String nguoiCM) {
        return "PHIẾU MƯỢN TÀI LIỆU\n\n" +
            "Số thẻ: " + soThe + "\nHọ tên: " + hoTen + "\nĐơn vị: " + donVi +
            "\n\nMã CB: " + maCB + "\nTài liệu: " + tenTL + "\nHình thức: " + kieuMuon +
            "\n\nHạn trả: " + hanTra + "\nNgười cho mượn: " + nguoiCM;
    }

    private static String buildPhieuTraText(String soThe, String hoTen,
        String maCB, String tenTL, String ngayMuon, String hanTra, String tinhTrang, boolean quaHan) {
        return "PHIẾU TRẢ TÀI LIỆU\n\n" +
            "Số thẻ: " + soThe + "\nHọ tên: " + hoTen +
            "\n\nMã CB: " + maCB + "\nTài liệu: " + tenTL + "\nTình trạng: " + tinhTrang +
            "\n\nNgày mượn: " + ngayMuon + "\nHạn trả: " + hanTra +
            (quaHan ? " (QUÁ HẠN)" : "") + "\nNgày trả: " +
            new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
    }

    private static String buildPhieuPhatText(String soThe, String hoTen,
        String tenTL, String hanTra, int soNgay, double soTien, String htXuLy) {
        return "PHIẾU XỬ PHẠT\n\n" +
            "Số thẻ: " + soThe + "\nHọ tên: " + hoTen +
            "\n\nTài liệu: " + tenTL + "\nHạn trả: " + hanTra +
            "\nSố ngày trễ: " + soNgay + " ngày\nHình thức: " + htXuLy +
            "\n\nSố tiền phạt: " + String.format("%,.0f", soTien) + " VNĐ";
    }
}