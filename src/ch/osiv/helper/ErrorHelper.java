package ch.osiv.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;

import ch.osiv.webservices.DocumentManager;

/**
 * ErrorHelper
 */
public class ErrorHelper {

    private static final Logger logger = LoggerFactory.getLogger(DocumentManager.class);

    /**
     * @return JsonPrintJobResponse
     */
    public static JsonPrintJobResponse getLastKernelError() {

        JsonPrintJobResponse pjAnswer = new JsonPrintJobResponse();

        int error = Kernel32.INSTANCE.GetLastError();

        logger.info("OS error #" + error);
        logger.info(Kernel32Util.formatMessageFromLastErrorCode(error));

        pjAnswer.setMessage("Error while calling the printing process. Error no." + error + ": " +
                            Kernel32Util.formatMessageFromLastErrorCode(error));
        pjAnswer.setPrinted(false);
        System.gc();
        return pjAnswer;
    }

}
