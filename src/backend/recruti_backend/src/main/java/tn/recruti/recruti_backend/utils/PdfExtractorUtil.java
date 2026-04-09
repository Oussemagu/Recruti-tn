package tn.recruti.recruti_backend.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tn.recruti.recruti_backend.Exception.PdfParsingException;
import java.io.IOException;

@Component
public class PdfExtractorUtil {
    public String extractText(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new PdfParsingException("failed to parse PDF " + e.getMessage());
        }
    }
}
