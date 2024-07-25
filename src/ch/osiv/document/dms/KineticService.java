package ch.osiv.document.dms;

/**
 * KineticService class
 *
 * @author Arno van der Ende
 */
public class KineticService {

    private final static String SOAP_NAMESPACE         = "http://services.kinetic.ch";
    private final static String SOAP_HEADER_SUBSTITUTE = "[header]";
    private final static String SOAP_BODY_SUBSTITUTE   = "[body]";
    // @formatter:off
    private final static String SOAP_REQUEST_TEMPLATE  = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + SOAP_NAMESPACE + "/\">"
                                                       + "<soapenv:Header>" + SOAP_HEADER_SUBSTITUTE + "</soapenv:Header>"
                                                       + "<soapenv:Body>" + SOAP_BODY_SUBSTITUTE + "</soapenv:Body>"
                                                       + "</soapenv:Envelope>";
    // @formatter:on

    private String   serviceHost;    // i.e. http://svr-de-windrea4.de.ivnet.ch/KineticServices
    protected String serviceUrl;     // i.e. http://svr-de-windrea4.de.ivnet.ch/KineticServices/Kinetic.Services.WMObjectService.svc
    private String   soapActionBase; // i.e. http://services.kinetic.ch/IWMObjectService

    /**
     * Constructor which sets the serviceEndpoint
     *
     * @param serviceHost            The service host (i.e.
     *                               http://svr-de-windrea4.de.ivnet.ch/KineticServices)
     * @param serviceEndpointAddress The service endpoint address (i.e.
     *                               Kinetic.Services.WMObjectService.svc)
     * @param soapActionBaseName     The soap action base name (i.e. IWMObjectService)
     */
    protected KineticService(String serviceHost,
                             String serviceEndpointAddress,
                             String soapActionBaseName) {
        this.serviceHost    = serviceHost;
        this.serviceUrl     = serviceHost + "/" + serviceEndpointAddress;
        this.soapActionBase = SOAP_NAMESPACE + "/" + soapActionBaseName;
    }

    /**
     * Gets the service host
     *
     * @return Service host
     */
    public String getServiceHost() {
        return this.serviceHost;
    }

    /**
     * Gets the full soap action name
     *
     * @param action The action name
     * @return The full soap action name
     */
    protected String getSoapAction(String action) {
        return this.soapActionBase + "/" + action;
    }

    /**
     * Gets the full soap request xml
     *
     * @param headerValue To be inserted into the header section
     * @param bodyValue   To be insterted into the body section
     * @return The soap request xml
     */
    protected String getSoapRequestBody(String headerValue,
                                        String bodyValue) {

        return SOAP_REQUEST_TEMPLATE.replace(SOAP_HEADER_SUBSTITUTE,
                                             (headerValue != null) ? headerValue
                                                                   : "")
                                    .replace(SOAP_BODY_SUBSTITUTE,
                                             (bodyValue != null) ? bodyValue
                                                                 : "");

    }

}
