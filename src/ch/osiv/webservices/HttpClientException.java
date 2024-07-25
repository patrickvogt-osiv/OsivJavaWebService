package ch.osiv.webservices;

import java.net.http.HttpResponse;

/**
 * HttpClientException class
 *
 * @author Arno van der Ende
 */
public class HttpClientException
    extends Exception {

    private static final long serialVersionUID = 1L;

    private String requestBody;
    private String soapAction;
    private int    statusCode;
    private String responseBody;
    private String errorMessage;

    /**
     * Constructor
     *
     * @param requestBody Request body
     * @param soapAction  SOAP action
     * @param response    Response object
     */
    public HttpClientException(String requestBody,
                               String soapAction,
                               HttpResponse<String> response) {
        this.requestBody  = requestBody;
        this.soapAction   = soapAction;
        this.statusCode   = response.statusCode();
        this.responseBody = response.body();
    }

    /**
     * Constructor
     *
     * @param requestBody Request body
     * @param soapAction  SOAP action
     * @param response    Response object
     */
    public HttpClientException(String requestBody,
                               String soapAction,
                               HttpResponse<String> response,
                               String errorMessage) {
        this(requestBody, soapAction, response);
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the message
     *
     * @return Error message
     */
    @Override
    public String getMessage() {
        return (errorMessage != null && !errorMessage.isBlank()) ? errorMessage
                                                                 : String.format("Unable to execute '" +
                                                                                 soapAction + "'");
    }

    /**
     * @return the requestBody
     */
    public String getRequestBody() {
        return requestBody;
    }

    /**
     * @return the soapAction
     */
    public String getSoapAction() {
        return soapAction;
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the responseBody
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Override toString
     *
     * @return Exception message
     */
    @Override
    public String toString() {
        // @formatter:off
        return super.toString() + " [statusCode=" + statusCode
                                + ", requestBody='" + requestBody
                                + "', responseBody='" + responseBody
                                + "']";
        // @formatter:on
    }

}
