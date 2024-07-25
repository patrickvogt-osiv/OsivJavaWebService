import java.nio.file.Path;

import javax.ws.rs.core.Response;

import ch.osiv.document.ExportDocumentsRequest;
import ch.osiv.document.ExportDocumentsResponse;
import ch.osiv.helper.FileHelper;
import ch.osiv.webservices.DocumentManager;

public class test_ExportDocuments_fromJson {

    public static void main(String[] args) throws Exception {

        // NOTE: disable 'e.printStackTrace()' on line 53 in DocumentManager
        //       to ignore the error of the Aspose Word License

        ExportDocumentsRequest request = new ExportDocumentsRequest();
        request.deserialize(getJsonRequest());

        Response response = new DocumentManager().exportDocuments(request);
        System.out.println("StatusCode: " + response.getStatus());
        if (response.getEntity() instanceof ExportDocumentsResponse) {
            System.out.println("Response  : " +
                               ((ExportDocumentsResponse) response.getEntity()).serialize());
        } else {
            System.out.println("Response  : " + response.getEntity().toString());
        }
    }

    private static String getJsonRequest() {

        Path dmsCachSessionDir = Path.of("//svr-de-windows.de.ivnet.ch/DMSCache/_cheatguid");
        FileHelper.ensureDirectoryExist(dmsCachSessionDir);

        // @formatter:off
        String json = "{"
                    + "    \"kineticSettings\": {"
                    + "        \"kineticServiceUrl\": \"http://svr-de-windrea4.de.ivnet.ch/KineticServices\","
                    + "        \"kineticTempDir\": \"\\\\\\\\svr-de-windows.de.ivnet.ch\\\\DMSCache\\\\Kinetic\\\\\","
                    + "        \"loadDocumentMethod\": \"LOAD_FROM_DISK\""
                    + "    },"
                    + "    \"sessionSettings\": {"
                    + "        \"dmsCacheSessionDir\": \"" + dmsCachSessionDir.toString().replace("\\", "\\\\") + "\","
                    + "        \"targetDirName\": \"IV-315-2IVG_Zohner-Arthur-17.01.24-105916\","
                    + "        \"useThreads\": true"
                    + "    },"
                    + "    \"conversionDetails\": {"
                    + "        \"burnAnnotations\": true,"
                    + "        \"combinePdfFiles\": true,"
                    + "        \"convertMethod\": \"TO_PDF\","
                    + "        \"documentColor\": \"JPEG\","
                    + "        \"documents\": ["
                    + "            { \"versionId\": 58649 },"
                    + "            { \"versionId\": 58647 },"
                    + "            { \"versionId\": 58645 },"
                    + "            { \"versionId\": 53193 },"
                    + "            { \"versionId\": 53195 },"
                    + "            { \"versionId\": 53196 }"
                    + "        ]"
                    + "    }"
                    + "}";
        // @formatter:on
        return json;
    }

}
