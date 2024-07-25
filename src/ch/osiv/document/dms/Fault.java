package ch.osiv.document.dms;

import ch.osiv.helper.JsonSerializable;

/**
 * Fault class
 *
 * @author Arno van der Ende
 */
public class Fault
    extends JsonSerializable {

    private String faultcode;
    private String faultstring;

    /**
     * @return the faultcode
     */
    public String getFaultcode() {
        return faultcode;
    }

    /**
     * @param faultcode the faultcode to set
     */
    public void setFaultcode(String faultcode) {
        this.faultcode = faultcode;
    }

    /**
     * @return the faultstring
     */
    public String getFaultstring() {
        return faultstring;
    }

    /**
     * @param faultstring the faultstring to set
     */
    public void setFaultstring(String faultstring) {
        this.faultstring = faultstring;
    }

}
