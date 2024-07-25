package ch.osiv.document;

import java.util.List;

import ch.osiv.document.dms.DmsDocument;
import ch.osiv.helper.JsonSerializable;

/**
 * ExportDocumentsResponse class
 *
 * @author Arno van der Ende
 */
public class ExportDocumentsResponse
    extends JsonSerializable {

    private List<DmsDocument> documents;
    private String            errorMessage;
    private String            resultFile;
    private String            resultPath;

    /**
     * Getter for documents
     *
     * @return documents
     */
    public List<DmsDocument> getDocuments() {
        return documents;
    }

    /**
     * Setter for documents
     *
     * @param documents The documents to set
     */
    public void setDocuments(List<DmsDocument> documents) {
        this.documents = documents;
    }

    /**
     * Getter for errorMessage
     *
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Setter for errorMessage
     *
     * @param errorMessage The errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Getter for resultFile
     *
     * @return resultFile
     */
    public String getResultFile() {
        return resultFile;
    }

    /**
     * Setter for resultFile
     *
     * @param resultFile The resultFile to set
     */
    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    /**
     * Getter for resultPath
     *
     * @return resultPath
     */
    public String getResultPath() {
        return resultPath;
    }

    /**
     * Setter for resultPath
     *
     * @param resultPath The resultPath to set
     */
    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

}
