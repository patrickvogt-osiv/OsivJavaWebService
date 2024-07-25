package ch.osiv.webservices;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.Response;

/**
 * HttpClientHelper class
 *
 * @author Arno van der Ende
 */
public class HttpClientHelper {

    /**
     * Executes a Soap action with the HttpClient
     *
     * @param serviceUrl  Webservice url
     * @param soapAction  SOAP action
     * @param requestBody The body to send
     * @return The response its body
     * @throws Exception
     */
    public static String execute(String serviceUrl,
                                 String soapAction,
                                 String requestBody) throws Exception {

        // Create an HttpClient
        HttpClient httpClient = HttpClient.newHttpClient();

        // Create a URI for the SOAP service endpoint
        URI uri = new URI(serviceUrl);

        // Create the HttpRequest
        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder(uri)
                                         .header("Content-Type", "text/xml; charset=utf-8")
                                         .header("SOAPAction", soapAction)
                                         .header("Accept-Encoding", "gzip,deflate")
                                         .header("Accept", "text/xml")
                                         .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                                         .build();
        // @formatter:on

        HttpResponse<String> response = httpClient.send(request,
                                                        HttpResponse.BodyHandlers.ofString());

        //System.out.println(response.statusCode() + " [" + soapAction + "] HTTP response body: " + response.body());

        if (response.statusCode() != Response.Status.OK.getStatusCode() &&
            response.statusCode() != Response.Status.ACCEPTED.getStatusCode()) {
            throw new HttpClientException(requestBody, soapAction, response);
        }

        return response.body();
    }

}
