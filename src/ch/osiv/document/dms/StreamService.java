package ch.osiv.document.dms;

import java.util.Base64;

import ch.osiv.helper.XMLHelper;
import ch.osiv.webservices.HttpClientHelper;

/**
 * StreamService class
 *
 * @author Arno van der Ende
 */
public class StreamService
    extends KineticService
    implements StreamServiceInterface {

    private final static String STREAM_SERVICE_ENDPOINT_ADDRESS = "Kinetic.Services.StreamService.svc";
    private final static String STREAM_SOAP_ACTION_BASE_NAME    = "StreamService";

    private static StreamService instance; // Singleton

    /**
     * Private constructor, prevents to be instantiated outside this class
     *
     * @param host The host url
     */
    private StreamService(String host) {
        super(host, STREAM_SERVICE_ENDPOINT_ADDRESS, STREAM_SOAP_ACTION_BASE_NAME);
    }

    /**
     * Singleton pattern to get the instance
     *
     * @param kineticServicesUrl The kinetic host url
     * @return StreamService instance
     */
    public static StreamService getInstance(String kineticServicesUrl) {
        if (kineticServicesUrl == null || kineticServicesUrl.isBlank())
            throw new IllegalArgumentException("Parameter 'kineticServicesUrl' cannot be empty!");

        if (instance == null)
            instance = new StreamService(kineticServicesUrl);

        return instance;
    }

    /**
     * Downloads a file from web server
     * NOTE: downloadFile needs the fileId from exportDocument in order to download it
     *
     * @param fileId - Identification key (fileId from exportDocument)
     * @return Response object
     * @throws Exception
     */
    @Override
    public DownloadFileResponse downloadFile(String fileId) throws Exception {
        String soapAction = this.getSoapAction("DownloadFile");
        String soapBody   = this.getSoapRequestBody("<ser:FileId>" + fileId + "</ser:FileId>",
                                                    null);

        // execute the soap call
        String responseBody = HttpClientHelper.execute(this.serviceUrl, soapAction, soapBody);

        // parse the response into an object
        DownloadFileResponse downloadFileResponse = parseDownloadFileResponse(responseBody);

        // downloadFile can return a HTTP 200, but still have errors
        // therefore handle it manually
        String errorCode = downloadFileResponse.getErrorCode();
        if (errorCode != null && !errorCode.isEmpty() && !errorCode.equals("0"))
            // TODO: Should be an own Exception (DownloadFileException)
            throw new RuntimeException("Error downloading document '" + fileId + "': [" +
                                       errorCode + "] " +
                                       downloadFileResponse.getErrorDescription());

        return downloadFileResponse;
    }

    /**
     * Parses the response from the DownloadFile request
     *
     * @param responeBody The response body as String
     * @return The object of the response
     * @throws Exception
     */
    private DownloadFileResponse parseDownloadFileResponse(String responseBody) throws Exception {

        DownloadFileResponse downloadFileResponse = new DownloadFileResponse();

        String stringValue;
        Double numberValue;

        // ErrorCode
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//Header/ErrorCode");
        downloadFileResponse.setErrorCode(stringValue);

        // ErrorDescription
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//Header/ErrorDescription");
        downloadFileResponse.setErrorDescription(stringValue);

        // FileName
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//Header/FileName");
        downloadFileResponse.setFileName(stringValue);

        // Length
        numberValue = XMLHelper.getNodeNumberValue(responseBody, "//Header/Length");
        downloadFileResponse.setLength(numberValue.longValue());

        // Content
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//Content");
        if (stringValue == null)
            stringValue = "";
        downloadFileResponse.setContent(Base64.getDecoder().decode(stringValue));

        return downloadFileResponse;
    }

}
