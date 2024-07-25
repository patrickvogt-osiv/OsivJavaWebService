package ch.osiv.document.dms;

/**
 * WMSessionServiceInterface interface
 *
 * @author Arno van der Ende
 */
public interface WMObjectServiceInterface {

    /**
     * Initializes a windream session and returns an identification key (sessionId)
     * The SessionId is used for all subsequent calls
     *
     * @param wmSessionModule License type
     * @return sessionId - Windream session identification key
     * @throws Exception
     */
    public String createWMSession(WMSessionModule wmSessionModule) throws Exception;

    /**
     * Exports a document from windream to the web service storage and returns an
     * identification key for the file
     * The identification key can be used to download the file using the StreamService web service
     *
     * @param sessionId Windream session identification key
     * @return fileId - Identification key of a file in the web service storage
     * @throws Exception
     */
    public String exportDocument(String sessionId) throws Exception;

    /**
     * Returns information such as name, path, creation date, etc. of the selected document or
     * directory
     *
     * @param sessionId Windream session identification key
     * @return WMObjectInfo - Object info
     * @throws Exception
     */
    public WMObjectInfo getObjectInfo(String sessionId) throws Exception;

    /**
     * Loads a document or directory into the session data. If this is a versioned document,
     * the most current version will be loaded
     * The object can then be used by subsequent calls
     *
     * @param sessionId Windream session identification key
     * @param versionId VersionId of a document or directory
     * @throws Exception
     */
    public void loadByVersionId(String sessionId,
                                int versionId) throws Exception;

    /**
     * Ends a windream session
     *
     * @param sessionId Windream session identification key
     * @throws Exception
     */
    public void terminateWMSession(String sessionId) throws Exception;

}
