package ch.osiv.document;

import ch.osiv.helper.JsonSerializable;

/**
 * SessionSettings class
 *
 * @author Arno van der Ende
 */
public class SessionSettings
    extends JsonSerializable {

    private String  dmsCacheSessionDir;
    private String  targetDirName;
    private boolean useThreads;

    /**
     * Constructor
     */
    public SessionSettings() {
        this.useThreads = true; // default value
    }

    /**
     * Constructor with mandatory dmsCacheSessionDir parameter
     *
     * @param dmsCacheSessionDir Dms cache session dir
     */
    public SessionSettings(String dmsCacheSessionDir) {
        this();
        this.dmsCacheSessionDir = dmsCacheSessionDir;
    }

    /**
     * @return the dmsCacheSessionDir
     */
    public String getDmsCacheSessionDir() {
        return dmsCacheSessionDir;
    }

    /**
     * @param dmsCacheSessionDir the dmsCacheSessionDir to set
     */
    public void setDmsCacheSessionDir(String dmsCacheSessionDir) {
        this.dmsCacheSessionDir = dmsCacheSessionDir;
    }

    /**
     * @return the targetDirName
     */
    public String getTargetDirName() {
        return (targetDirName == null || targetDirName.isBlank()) ? ""
                                                                  : targetDirName;
    }

    /**
     * @param targetDirName the targetDirName to set
     */
    public void setTargetDirName(String targetDirName) {
        this.targetDirName = targetDirName;
    }

    /**
     * @return the useThreads
     */
    public boolean getUseThreads() {
        return useThreads;
    }

    /**
     * @param useThreads the useThreads to set
     */
    public void setUseThreads(boolean useThreads) {
        this.useThreads = useThreads;
    }

}
