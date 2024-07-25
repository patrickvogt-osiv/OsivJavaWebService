package ch.osiv.document;

import ch.osiv.document.convert.ConversionDetails;
import ch.osiv.document.dms.KineticServiceSettings;
import ch.osiv.helper.JsonSerializable;

/**
 * ExportDocumentsRequest class
 *
 * @author Arno van der Ende
 */
public class ExportDocumentsRequest
    extends JsonSerializable {

    private KineticServiceSettings kineticSettings;
    private SessionSettings        sessionSettings;
    private ConversionDetails      conversionDetails;

    /**
     * Getter for kineticSettings
     *
     * @return kineticSettings
     */
    public KineticServiceSettings getKineticSettings() {
        return kineticSettings;
    }

    /**
     * Setter for kineticSettings
     *
     * @param kineticSettings The kineticSettings to set
     */
    public void setKineticSettings(KineticServiceSettings kineticSettings) {
        this.kineticSettings = kineticSettings;
    }

    /**
     * @return the sessionSettings
     */
    public SessionSettings getSessionSettings() {
        return sessionSettings;
    }

    /**
     * @param sessionSettings the sessionSettings to set
     */
    public void setSessionSettings(SessionSettings sessionSettings) {
        this.sessionSettings = sessionSettings;
    }

    /**
     * Getter for conversionDetails
     *
     * @return conversionDetails
     */
    public ConversionDetails getConversionDetails() {
        return conversionDetails;
    }

    /**
     * Setter for conversionDetails
     *
     * @param conversionDetails The conversionDetails to set
     */
    public void setConversionDetails(ConversionDetails conversionDetails) {
        this.conversionDetails = conversionDetails;
    }

}
