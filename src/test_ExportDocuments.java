import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import ch.osiv.document.ExportDocumentsRequest;
import ch.osiv.document.ExportDocumentsResponse;
import ch.osiv.document.SessionSettings;
import ch.osiv.document.convert.ConversionDetails;
import ch.osiv.document.convert.ConvertMethod;
import ch.osiv.document.dms.DmsDocument;
import ch.osiv.document.dms.KineticServiceSettings;
import ch.osiv.document.dms.LoadDocumentMethod;
import ch.osiv.webservices.DocumentManager;

public class test_ExportDocuments {

    public static void main(String[] args) throws Exception {

        // NOTE: disable 'e.printStackTrace()' on line 53 in DocumentManager
        //       to ignore the error of the Aspose Word License

        ExportDocumentsRequest request           = new ExportDocumentsRequest();
        KineticServiceSettings kineticSettings   = new KineticServiceSettings();
        SessionSettings        sessionSettings   = new SessionSettings("\\\\svr-de-windows.de.ivnet.ch\\DMSCache\\_cheatguid");
        ConversionDetails      conversionDetails = new ConversionDetails();
        List<DmsDocument>      documents         = new ArrayList<DmsDocument>();

        kineticSettings.setKineticServiceUrl("http://svr-de-windrea4.de.ivnet.ch/KineticServices");
        kineticSettings.setKineticTempDir("\\\\svr-de-windows.de.ivnet.ch\\DMSCache\\Kinetic");
        kineticSettings.setLoadDocumentMethod(LoadDocumentMethod.LOAD_FROM_DISK);
        //kineticSettings.setLoadDocumentMethod(LoadDocumentMethod.LOAD_FROM_STREAM);

        sessionSettings.setTargetDirName("TargetDir");
        //sessionSettings.setUseThreads(false);

        conversionDetails.setDocuments(documents);
        conversionDetails.setBurnAnnotations(true);
        conversionDetails.setCombinePdfFiles(true);
        conversionDetails.setConvertMethod(ConvertMethod.TO_PDF);
        //conversionDetails.setConvertMethod(ConvertMethod.SPLIT_TIFF);

        // For the annotations, see <projectRoot>\test_annotations files and copy them to the dmsCacheSessionDir
        documents.add(new DmsDocument(58649)); // 100019219-1, 4 pages
        documents.add(new DmsDocument(46499)); // 100018421-1, 1 page
        documents.add(new DmsDocument(53196)); // 100018468-1, 2 pages
        documents.add(new DmsDocument(53198)); // 100018469-1, 2 pages
        documents.add(new DmsDocument(183303)); // 100020124-2, landscape doc with V2
        documents.add(new DmsDocument(69828)); // 100019752-1, 13 pages in trash-can
        documents.add(new DmsDocument(230517)); // 100021001-1, annotations
        documents.add(new DmsDocument(349782)); // 100021438-1, 3 pages color
        documents.add(new DmsDocument(350251)); // 100021781-1, 1 page color
        documents.add(new DmsDocument(350253)); // 100021783-1, 3 pages color 1-portrait, 2-landscape
        //documents.add(new DmsDocument(350781));  // 17 pages!

        request.setKineticSettings(kineticSettings);
        request.setSessionSettings(sessionSettings);
        request.setConversionDetails(conversionDetails);

        Response response = new DocumentManager().exportDocuments(request);
        System.out.println("StatusCode: " + response.getStatus());
        if (response.getEntity() instanceof ExportDocumentsResponse) {
            System.out.println("Response  : " +
                               ((ExportDocumentsResponse) response.getEntity()).serialize());
        } else {
            System.out.println("Response  : " + response.getEntity().toString());
        }
    }

}
