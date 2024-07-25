package ch.osiv.document.dms;

import ch.osiv.document.ObjectTypeInfo;
import ch.osiv.helper.XMLHelper;
import ch.osiv.webservices.HttpClientHelper;

/**
 * WMSessionService class
 *
 * @author Arno van der Ende
 */
public class WMObjectService
    extends KineticService
    implements WMObjectServiceInterface {

    private final static String OBJECT_SERVICE_ENDPOINT_ADDRESS = "Kinetic.Services.WMObjectService.svc";
    private final static String OBJECT_SOAP_ACTION_BASE_NAME    = "IWMObjectService";

    private static WMObjectService instance; // Singleton

    /**
     * Private constructor, prevents to be instantiated outside this class
     *
     * @param host The host url
     */
    private WMObjectService(String host) {
        super(host, OBJECT_SERVICE_ENDPOINT_ADDRESS, OBJECT_SOAP_ACTION_BASE_NAME);
    }

    /**
     * Singleton pattern to get the instance
     *
     * @param kineticServicesUrl The kinetic host url
     * @return WMObjectService instance
     */
    public static WMObjectService getInstance(String kineticServicesUrl) {
        if (kineticServicesUrl == null || kineticServicesUrl.isBlank())
            throw new IllegalArgumentException("Parameter 'kineticServicesUrl' cannot be empty!");

        if (instance == null)
            instance = new WMObjectService(kineticServicesUrl);

        return instance;
    }

    /**
     * Initializes a windream session and returns an identification key (sessionId).
     * The SessionId is used for all subsequent calls.
     *
     * @return Windream session identification key
     * @throws Exception
     */
    public String createWMSession() throws Exception {
        return this.createWMSession(WMSessionModule.Default);
    }

    /**
     * Initializes a windream session and returns an identification key (sessionId).
     * The SessionId is used for all subsequent calls.
     *
     * @param wmSessionModule License type
     * @return Windream session identification key
     * @throws Exception
     */
    @Override
    public String createWMSession(WMSessionModule wmSessionModule) throws Exception {
        String soapAction = this.getSoapAction("CreateWMSession");
        String soapBody   = this.getSoapRequestBody(null,
                                                    "<ser:CreateWMSession>" +
                                                          "  <ser:sessionModule>" +
                                                          wmSessionModule + "</ser:sessionModule>" +
                                                          "</ser:CreateWMSession>");

        // execute the soap call
        String responseBody = HttpClientHelper.execute(this.serviceUrl, soapAction, soapBody);

        // extract the result from the response body
        return XMLHelper.getNodeStringValue(responseBody, "//CreateWMSessionResult");
    }

    /**
     * Exports a document from windream to the web service storage and returns an
     * identification key for the file
     * The identification key can be used to download the file using the StreamService web service
     *
     * @param sessionId Windream session identification key
     * @return fileId - Identification key of a file in the web service storage
     * @throws Exception
     */
    @Override
    public String exportDocument(String sessionId) throws Exception {
        String soapAction = this.getSoapAction("ExportDocument");
        String soapBody   = this.getSoapRequestBody(null,
                                                    "<ser:ExportDocument>" + "<ser:sessionId>" +
                                                          sessionId + "</ser:sessionId>" +
                                                          "</ser:ExportDocument>");

        // execute the soap call
        String responseBody = HttpClientHelper.execute(this.serviceUrl, soapAction, soapBody);

        // extract the result from the response body
        return XMLHelper.getNodeStringValue(responseBody, "//ExportDocumentResult");
    }

    /**
     * Returns information such as name, path, creation date, etc. of the selected document or
     * directory
     *
     * @param sessionId Windream session identification key
     * @return WMObjectInfo - Object info
     * @throws Exception
     */
    @Override
    public WMObjectInfo getObjectInfo(String sessionId) throws Exception {
        String soapAction = this.getSoapAction("GetObjectInfo");
        String soapBody   = this.getSoapRequestBody(null,
                                                    "<ser:GetObjectInfo>" + "<ser:sessionId>" +
                                                          sessionId + "</ser:sessionId>" +
                                                          "</ser:GetObjectInfo>");

        // execute the soap call
        String responseBody = HttpClientHelper.execute(this.serviceUrl, soapAction, soapBody);

        // parse the response into an object
        WMObjectInfo objectInfo = parseGetObjectInfoResponse(responseBody);

        return objectInfo;
    }

    /**
     * Loads a document or directory into the session data. If this is a versioned document,
     * the most current version will be loaded
     * The object can then be used by subsequent calls
     *
     * @param sessionId Windream session identification key
     * @param versionId VersionId of a document or directory
     * @throws Exception
     */
    @Override
    public void loadByVersionId(String sessionId,
                                int versionId) throws Exception {
        String soapAction = this.getSoapAction("LoadByVersionId");
        String soapBody   = this.getSoapRequestBody(null,
                                                    "<ser:LoadByVersionId>" + "<ser:sessionId>" +
                                                          sessionId + "</ser:sessionId>" +
                                                          "<ser:versionId>" + versionId +
                                                          "</ser:versionId>" +
                                                          "</ser:LoadByVersionId>");

        // execute the soap call
        HttpClientHelper.execute(this.serviceUrl, soapAction, soapBody);
    }

    /**
     * Parses the response from the GetObjectInfo request
     *
     * @param responseBody The response body as STring
     * @return The object of the response
     * @throws Exception
     */
    private WMObjectInfo parseGetObjectInfoResponse(String responseBody) throws Exception {

        WMObjectInfo wmObjectInfo = new WMObjectInfo();

        String  stringValue;
        Double  numberValue;
        boolean booleanValue;

        // Name
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//Name");
        wmObjectInfo.setName(stringValue);

        // FullName
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//FullName");
        wmObjectInfo.setFullName(stringValue);

        // IsFile
        booleanValue = XMLHelper.getNodeBooleanValue(responseBody, "//IsFile");
        wmObjectInfo.setIsFile(booleanValue);

        // VersionId
        numberValue = XMLHelper.getNodeNumberValue(responseBody, "//VersionId");
        wmObjectInfo.setVersionId(numberValue.intValue());

        // VersionNumber
        numberValue = XMLHelper.getNodeNumberValue(responseBody, "//VersionNumber");
        wmObjectInfo.setVersionNumber(numberValue.intValue());

        // Size
        numberValue = XMLHelper.getNodeNumberValue(responseBody, "//Size");
        wmObjectInfo.setSize(numberValue.longValue());

        // CreationTime
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//CreationTime");
        wmObjectInfo.setCreationTime(stringValue);

        // LastWriteTime
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//LastWriteTime");
        wmObjectInfo.setLastWriteTime(stringValue);

        // Creator
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//Creator");
        wmObjectInfo.setCreator(stringValue);

        // ObjectTypeInfo
        numberValue = XMLHelper.getNodeNumberValue(responseBody, "//ObjectType/Id");
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//ObjectType/Name");
        if ((numberValue != null && !numberValue.isNaN()) ||
            (stringValue != null && !stringValue.isEmpty())) {
            ObjectTypeInfo objectType = new ObjectTypeInfo();
            objectType.setId(numberValue.intValue());
            objectType.setName(stringValue);
            wmObjectInfo.setObjectTypeInfo(objectType);
        }

        return wmObjectInfo;
    }

    /**
     * Parses the response from any endpoint which can throw an Http error where the body
     * contains the 'Fault' object
     *
     * @param responseBody The response body as String
     * @return The fault object from the exception
     * @throws Exception
     */
    public Fault parseResponseExceptionBody(String responseBody) throws Exception {

        Fault fault = new Fault();

        String stringValue;

        if (!XMLHelper.hasNode(responseBody, "//Body/Fault") ||
            !XMLHelper.hasNode(responseBody, "//faultstring")) {
            return null;
        }

        // faultcode
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//faultcode");
        fault.setFaultcode(stringValue);

        // faultstring
        stringValue = XMLHelper.getNodeStringValue(responseBody, "//faultstring");
        fault.setFaultstring(stringValue);

        return fault;
    }

    /**
     * Ends a windream session
     *
     * @param sessionId Windream session identification key
     * @throws Exception
     */
    @Override
    public void terminateWMSession(String sessionId) throws Exception {
        String soapAction = this.getSoapAction("TerminateWMSession");
        String soapBody   = this.getSoapRequestBody(null,
                                                    "<ser:TerminateWMSession>" + "<ser:sessionId>" +
                                                          sessionId + "</ser:sessionId>" +
                                                          "</ser:TerminateWMSession>");

        // execute the soap call
        HttpClientHelper.execute(this.serviceUrl, soapAction, soapBody);
    }
}
