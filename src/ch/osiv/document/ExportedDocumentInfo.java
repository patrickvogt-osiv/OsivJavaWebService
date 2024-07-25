package ch.osiv.document;

import java.nio.file.Path;

import ch.osiv.document.dms.WMObjectInfo;
import ch.osiv.helper.JsonSerializable;

/**
 * ExportedDocumentInfo class
 *
 * @author Arno van der Ende
 */
public class ExportedDocumentInfo
    extends JsonSerializable {

    private Path         documentFile;
    private String       fileId;
    private WMObjectInfo objectInfo;

    /**
     * Constructor
     * 
     * @param fileId
     */
    public ExportedDocumentInfo(String fileId) {
        this.fileId = fileId;
    }

    /**
     * @return the documentFile
     */
    public Path getDocumentFile() {
        return documentFile;
    }

    /**
     * @param documentFile the documentFile to set
     */
    public void setDocumentFile(Path documentFile) {
        this.documentFile = documentFile;
    }

    /**
     * Getter for fileId
     *
     * @return fileId
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Setter for fileId
     *
     * @param fileId The fileId to set
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Getter for objectInfo
     *
     * @return objectInfo
     */
    public WMObjectInfo getObjectInfo() {
        return objectInfo;
    }

    /**
     * Setter for objectInfo
     *
     * @param objectInfo The objectInfo to set
     */
    public void setObjectInfo(WMObjectInfo objectInfo) {
        this.objectInfo = objectInfo;
    }

}
