package ch.osiv.helper;

import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

class OsivPrintJobListener
    implements PrintJobListener {

    private static final boolean showMessages = false;

    @Override
    public void printDataTransferCompleted(final PrintJobEvent printJobEvent) {
        if (showMessages) {
            System.out.println("printDataTransferCompleted=" + printJobEvent);
        }
    }

    @Override
    public void printJobCompleted(final PrintJobEvent printJobEvent) {
        if (showMessages) {
            System.out.println("printJobCompleted=" + printJobEvent);
        }
    }

    @Override
    public void printJobFailed(final PrintJobEvent printJobEvent) {
        if (showMessages) {
            System.out.println("printJobEvent=" + printJobEvent);
        }
    }

    @Override
    public void printJobCanceled(final PrintJobEvent printJobEvent) {
        if (showMessages) {
            System.out.println("printJobFailed=" + printJobEvent);
        }
    }

    @Override
    public void printJobNoMoreEvents(final PrintJobEvent printJobEvent) {
        if (showMessages) {
            System.out.println("printJobNoMoreEvents=" + printJobEvent);
        }
    }

    @Override
    public void printJobRequiresAttention(final PrintJobEvent printJobEvent) {
        if (showMessages) {
            System.out.println("printJobRequiresAttention=" + printJobEvent);
        }
    }
}
