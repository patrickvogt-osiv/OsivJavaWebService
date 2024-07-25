package ch.osiv.document.convert;

import java.util.List;

import ch.osiv.document.dms.DmsDocument;
import ch.osiv.helper.JsonSerializable;

/**
 * ConversionDetails class
 *
 * @author Arno van der Ende
 */
public class ConversionDetails
    extends JsonSerializable {

    private boolean           burnAnnotations;
    private boolean           combinePdfFiles;
    private ConvertMethod     convertMethod;
    private DocumentColor     documentColor;
    private List<DmsDocument> documents;

    /**
     * Getter for burnAnnotations
     *
     * @return burnAnnotations
     */
    public boolean getBurnAnnotations() {
        return burnAnnotations;
    }

    /**
     * Setter for burnAnnotations
     *
     * @param burnAnnotations The burnAnnotations to set
     */
    public void setBurnAnnotations(boolean burnAnnotations) {
        this.burnAnnotations = burnAnnotations;
    }

    /**
     * Getter for combinePdfFiles
     *
     * @return combinePdfFiles
     */
    public boolean getCombinePdfFiles() {
        return combinePdfFiles;
    }

    /**
     * Setter for combinePdfFiles
     *
     * @param combinePdfFiles The combinePdfFiles to set
     */
    public void setCombinePdfFiles(boolean combinePdfFiles) {
        this.combinePdfFiles = combinePdfFiles;
    }

    /**
     * Getter for convertMethod
     *
     * @return convertMethod
     */
    public ConvertMethod getConvertMethod() {
        return convertMethod;
    }

    /**
     * Setter for convertMethod
     *
     * @param convertMethod The convertMethod to set
     */
    public void setConvertMethod(ConvertMethod convertMethod) {
        this.convertMethod = convertMethod;
    }

    /**
     * @return the documentColor
     */
    public DocumentColor getDocumentColor() {
        return documentColor;
    }

    /**
     * @param documentColor the documentColor to set
     */
    public void setDocumentColor(DocumentColor documentColor) {
        this.documentColor = documentColor;
    }

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

}
