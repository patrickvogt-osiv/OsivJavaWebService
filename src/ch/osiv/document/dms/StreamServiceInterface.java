package ch.osiv.document.dms;

/**
 * StreamServiceInterface interface
 *
 * @author Arno van der Ende
 */
public interface StreamServiceInterface {

    /**
     * Downloads a file from web server
     * NOTE: downloadFile needs the fileId from exportDocument in order to download it
     *
     * @param fileId - Identification key (fileId from exportDocument)
     * @return Response object
     * @throws Exception
     */
    public DownloadFileResponse downloadFile(String fileId) throws Exception;

}
