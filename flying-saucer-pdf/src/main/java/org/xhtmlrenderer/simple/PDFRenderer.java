package org.xhtmlrenderer.simple;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.newOutputStream;

/**
 * <p>
 * PDFRenderer supports headless rendering of XHTML documents, outputting
 * to PDF format. There are two static utility methods, one for rendering
 * a {@link java.net.URL}, {@link #renderToPDF(String, String)} and one
 * for rendering a {@link File}, {@link #renderToPDF(File, String)}</p>
 * <p>You can use this utility from the command line by passing in
 * the URL or file location as first parameter, and PDF path as second
 * parameter:
 * <pre>
 * java -cp %classpath% org.xhtmlrenderer.simple.PDFRenderer <url> <pdf>
 * </pre>
 *
 * @author Pete Brant
 * @author Patrick Wright
 */
@ParametersAreNonnullByDefault
public class PDFRenderer {
    private static final Map<String, Character> versionMap = new HashMap<>();

    static {
        versionMap.put("1.2", PdfWriter.VERSION_1_2);
        versionMap.put("1.3", PdfWriter.VERSION_1_3);
        versionMap.put("1.4", PdfWriter.VERSION_1_4);
        versionMap.put("1.5", PdfWriter.VERSION_1_5);
        versionMap.put("1.6", PdfWriter.VERSION_1_6);
        versionMap.put("1.7", PdfWriter.VERSION_1_7);
    }
    /**
     * Renders the XML file at the given URL as a PDF file
     * at the target location.
     *
     * @param url url for the XML file to render
     * @param pdf path to the PDF file to create
     * @throws IOException       if the URL or PDF location is
     *                           invalid
     * @throws DocumentException if an error occurred
     *                           while building the Document.
     */
    public static void renderToPDF(String url, String pdf) throws IOException, DocumentException {
        renderToPDF(url, pdf, null);
    }
    /**
     * Renders the XML file at the given URL as a PDF file
     * at the target location.
     *
     * @param url url for the XML file to render
     * @param pdf path to the PDF file to create
     * @param pdfVersion version of PDF to output; null uses default version
     * @throws IOException       if the URL or PDF location is
     *                           invalid
     * @throws DocumentException if an error occurred
     *                           while building the Document.
     */
    public static void renderToPDF(String url, String pdf, @Nullable Character pdfVersion)
            throws IOException, DocumentException {

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(url);
        if (pdfVersion != null) renderer.setPDFVersion(pdfVersion);
        doRenderToPDF(renderer, pdf);
    }

    /**
     * Renders the XML file as a PDF file at the target location.
     *
     * @param file XML file to render
     * @param pdf  path to the PDF file to create
     * @throws IOException       if the file or PDF location is
     *                           invalid
     * @throws DocumentException if an error occurred
     *                           while building the Document.
     */
    public static void renderToPDF(File file, String pdf) throws IOException, DocumentException {
        renderToPDF(file, pdf, null);
    }

    /**
     * Renders the XML file as a PDF file at the target location.
     *
     * @param file XML file to render
     * @param pdf  path to the PDF file to create
     * @param pdfVersion version of PDF to output; null uses default version
     * @throws IOException       if the file or PDF location is
     *                           invalid
     * @throws DocumentException if an error occurred
     *                           while building the Document.
     */
    public static void renderToPDF(File file, String pdf, @Nullable Character pdfVersion)
            throws IOException, DocumentException {

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(file);
        if (pdfVersion != null) renderer.setPDFVersion(pdfVersion);
        doRenderToPDF(renderer, pdf);
    }

    /**
     * Internal use, runs the render process
     */
    private static void doRenderToPDF(ITextRenderer renderer, String pdf) throws IOException, DocumentException {
        try (OutputStream os = newOutputStream(Paths.get(pdf))){
            renderer.layout();
            renderer.createPDF(os);
        }
    }

    /**
     * Renders a file or URL to a PDF. Command line use: first
     * argument is URL or file path, second
     * argument is path to PDF file to generate.
     *
     * @param args see desc
     * @throws IOException if source could not be read, or if
     * PDF path is invalid
     * @throws DocumentException if an error occurs while building
     * the document
     */
    public static void main(String[] args) throws IOException, DocumentException {
        if (args.length < 2) {
            usage("Incorrect argument list.");
        }
        Character pdfVersion = null;
        if (args.length == 3) {
            pdfVersion = checkVersion(args[2]);
        }
        String url = args[0];
        if (!url.contains("://")) {
            // maybe it's a file
            File f = new File(url);
            if (f.exists()) {
                PDFRenderer.renderToPDF(f, args[1], pdfVersion);
            } else {
                usage("File to render is not found: " + url);
            }
        } else {
            PDFRenderer.renderToPDF(url, args[1], pdfVersion);
        }
    }

    private static Character checkVersion(String version) {
        final Character val = versionMap.get(version.trim());
        if (val == null) {
            usage("Invalid PDF version number; use 1.2 through 1.7");
        }
        return val;
    }

    /** prints out usage information, with optional error message
     */
    private static void usage(@Nullable String err) {
        if (err != null && !err.isEmpty()) {
            System.err.println("==>" + err);
        }
        System.err.println("Usage: ... url pdf [version]");
        System.err.println("   where version (optional) is between 1.2 and 1.7");
        System.exit(1);
    }
}
