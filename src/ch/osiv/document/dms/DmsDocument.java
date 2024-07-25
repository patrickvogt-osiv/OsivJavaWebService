package ch.osiv.document.dms;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ch.osiv.helper.JsonSerializable;
import ch.osiv.helper.PrimitiveIntSerializer;

/**
 * DmsDocument class
 *
 * @author Arno van der Ende
 */
public class DmsDocument
    extends JsonSerializable {

    private String documentName;
    @JsonSerialize(using = PrimitiveIntSerializer.class)
    private int    groupId;
    private int    nrPages;
    @JsonSerialize(using = PrimitiveIntSerializer.class)
    private int    versionId;
    private String bookmarkName;

    /**
     * Constructor
     */
    public DmsDocument() {
        super();
    }

    /**
     * Constructor
     *
     * @param versionId The versionId
     */
    public DmsDocument(int versionId) {
        super();
        this.versionId = versionId;
    }

    /**
     * Getter for documentName
     *
     * @return documentName
     */
    public String getDocumentName() {
        return documentName;
    }

    /**
     * Setter for documentName
     *
     * @param documentName The documentName to set
     */
    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    /**
     * Getter for groupId
     *
     * @return the groupId
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * Setter for groupId
     *
     * @param groupId the groupId to set
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /**
     * Getter for nrPages
     *
     * @return nrPages
     */
    public int getNrPages() {
        return nrPages;
    }

    /**
     * Setter for nrPages
     *
     * @param nrPages The nrPages to set
     */
    public void setNrPages(int nrPages) {
        this.nrPages = nrPages;
    }

    /**
     * Getter for versionId
     *
     * @return versionId
     */
    public int getVersionId() {
        return versionId;
    }

    /**
     * Setter for versionId
     *
     * @param versionId The versionId to set
     */
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    /**
     * Getter for bookmarkName
     * 
     * @return bookmarkName
     */
    public String getBookmarkName() {
        return bookmarkName;
    }

    /**
     * Setter for bookmarkName
     * 
     * @param bookmarkName
     */
    public void setBookmarkName(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

}
