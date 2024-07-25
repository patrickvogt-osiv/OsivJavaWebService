package ch.osiv.document.dms;

import ch.osiv.helper.JsonSerializable;

/**
 * KineticSerivceSettings class
 *
 * @author Arno van der Ende
 */
public class KineticServiceSettings
    extends JsonSerializable {

    private String             kineticServiceUrl;
    private String             kineticTempDir;
    private LoadDocumentMethod loadDocumentMethod;

    /**
     * Getter for kineticServiceUrl
     *
     * @return kineticServiceUrl
     */
    public String getKineticServiceUrl() {
        return kineticServiceUrl;
    }

    /**
     * Setter for kineticServiceUrl
     *
     * @param kineticServiceUrl The kineticServiceUrl to set
     */
    public void setKineticServiceUrl(String kineticServiceUrl) {
        this.kineticServiceUrl = kineticServiceUrl;
    }

    /**
     * Getter for kineticTempDir
     *
     * @return kineticTempDir
     */
    public String getKineticTempDir() {
        return kineticTempDir;
    }

    /**
     * Setter for kineticTempDir
     *
     * @param kineticTempDir The kineticTempDir to set
     */
    public void setKineticTempDir(String kineticTempDir) {
        this.kineticTempDir = kineticTempDir;
    }

    /**
     * Getter for loadDocumentMethod
     *
     * @return loadDocumentMethod
     */
    public LoadDocumentMethod getLoadDocumentMethod() {
        return loadDocumentMethod;
    }

    /**
     * Setter for loadDocumentMethod
     *
     * @param loadDocumentMethod The loadDocumentMethod to set
     */
    public void setLoadDocumentMethod(LoadDocumentMethod loadDocumentMethod) {
        this.loadDocumentMethod = loadDocumentMethod;
    }

}
