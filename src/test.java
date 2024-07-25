import java.util.Date;

import ch.osiv.helper.FileConverterHelper;
import ch.osiv.helper.FileType;
import ch.osiv.webservices.DocumentManager;

public class test {

    //	Printing resolution issue	https://forum.aspose.com/t/poor-print-quality-when-printing-pdf-files/109184/4
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        DocumentManager oDocumentManager = new DocumentManager();
        /* test code for testing on SVR-OSC-03.ivent.ch\\PR63_MOS04_SIMPLEX
         * oDocumentManager.pdfPrint("c:\\temp\\mypdftest.pdf",
         * "\\\\SVR-OSC-03.ivnet.ch\\PR63_MOS04_SIMPLEX", false, (short) 1, "Test set"); */

        /* Local test on default windows Print to PDF */
        // oDocumentManager.pdfPrint("c:\\temp\\mypdftest.pdf", "\\\\SVR-OSC-03.ivnet.ch\\PR62_MOS03_DUPLEX", false, (short) 1, "Test set", true);
        // oDocumentManager.pdfPrint("c:\\temp\\mypdftest.pdf", "Microsoft Print to PDF", false, (short) 1, "Test set", true);
        // oDocumentManager.docToTiff("//svr-de-windows.de.ivnet.ch/DMSCache//test-alon154.docx")
        
        
        Date date1 = new Date();
        System.out.println(FileConverterHelper.convertFileTo("//svr-de-windows.de.ivnet.ch/DMSCache/A4CA09ECF39B22FE2C312DE16B06C0E88F00CC2176B2/100020089-1.TIF",
                                                             FileType.pdf));
        Date date2 = new Date();
        System.out.println(date2.getTime() - date1.getTime());
    }
}
