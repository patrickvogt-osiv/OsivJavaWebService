package ch.osiv.document.dms;

import ch.osiv.document.ObjectTypeInfo;
import ch.osiv.helper.JsonSerializable;

/**
 * WMObjectInfo class
 *
 * @author Arno van der Ende
 */
public class WMObjectInfo
    extends JsonSerializable {

    private String         name;
    private String         fullName;
    private boolean        isFile;
    private int            versionId;
    private int            versionNumber;
    private long           size;
    private String         creationTime;  // no need to convert it to a Date
    private String         lastWriteTime; // no need to convert it to a Date
    private String         creator;
    private ObjectTypeInfo objectTypeInfo;

    // NOTE: The XML also returns a 'Status' object, but this is not described in the documentation (therefore not implemented)

    /**
     * Getter for name - Name of the file or directory
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for fullName - Full path
     *
     * @return fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Setter for fullName
     *
     * @param fullName The fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Getter for isFile - File or directory
     *
     * @return isFile
     */
    public boolean getIsFile() {
        return isFile;
    }

    /**
     * Setter for isFile
     *
     * @param isFile The isFile to set
     */
    public void setIsFile(boolean isFile) {
        this.isFile = isFile;
    }

    /**
     * Getter for versionId - Unique key
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
     * Getter for versionNumber - Version number (for file only)
     *
     * @return versionNumber
     */
    public int getVersionNumber() {
        return versionNumber;
    }

    /**
     * Setter for versionNumber
     *
     * @param versionNumber The versionNumber to set
     */
    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    /**
     * Getter for size - Size of the file
     *
     * @return size
     */
    public long getSize() {
        return size;
    }

    /**
     * Setter for size
     *
     * @param size The size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Getter for creationTime - Creation time
     *
     * @return creationTime
     */
    public String getCreationTime() {
        return creationTime;
    }

    /**
     * Setter for creationTime
     *
     * @param creationTime The creationTime to set
     */
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Getter for lastWriteTime - Time of last write access
     *
     * @return lastWriteTime
     */
    public String getLastWriteTime() {
        return lastWriteTime;
    }

    /**
     * Setter for lastWriteTime
     *
     * @param lastWriteTime The lastWriteTime to set
     */
    public void setLastWriteTime(String lastWriteTime) {
        this.lastWriteTime = lastWriteTime;
    }

    /**
     * Getter for creator - Full creator name
     *
     * @return creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Setter for creator
     *
     * @param creator The creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Getter for objectTypeInfo - Object type
     *
     * @return objectTypeInfo
     */
    public ObjectTypeInfo getObjectTypeInfo() {
        return objectTypeInfo;
    }

    /**
     * Setter for objectTypeInfo
     *
     * @param objectTypeInfo The objectTypeInfo to set
     */
    public void setObjectTypeInfo(ObjectTypeInfo objectTypeInfo) {
        this.objectTypeInfo = objectTypeInfo;
    }

}
