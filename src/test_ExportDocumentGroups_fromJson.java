import java.nio.file.Path;

import javax.ws.rs.core.Response;

import ch.osiv.document.ExportDocumentsRequest;
import ch.osiv.document.ExportDocumentsResponse;
import ch.osiv.helper.FileHelper;
import ch.osiv.webservices.DocumentManager;

public class test_ExportDocumentGroups_fromJson {

    public static void main(String[] args) throws Exception {

        // NOTE: disable 'e.printStackTrace()' on line 53 in DocumentManager
        //       to ignore the error of the Aspose Word License
        //       or copy 'Aspose.Total.Java.lic' to <project>\target\classes\ch\osiv\helper

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
                    + "        \"burnAnnotations\": false,"
                    + "        \"combinePdfFiles\": false,"
                    + "        \"convertMethod\": \"MERGE_TIFF\","
                    + "        \"documentColor\": \"BLACK_AND_WHITE\","
                    + "        \"documents\": ["
                    + "            { \"versionId\": 58649, \"groupId\": 1 },"
                    + "            { \"versionId\": 58651, \"groupId\": 2 },"
                    + "            { \"versionId\": 58647, \"groupId\": 1 },"
                    + "            { \"versionId\": 58653, \"groupId\": 2 }"
                    + "        ]"
                    + "    }"
                    + "}";
        // @formatter:on
        return json;
    }

}

/* Group 1:
 * 58649 - 100019219-1.TIF - 4 pages
 * 58647 - 100019217-1.TIF - 13 pages
 * Group 2:
 * 58651 - 100019221-1.TIF - 16 pages
 * 58653 - 100019223-1.TIF - 13 pages */
