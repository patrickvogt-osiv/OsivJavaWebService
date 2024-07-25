package ch.osiv.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.OrientationRequested;

public class DocPrint {

    public static void main(String[] args) throws IOException {

        /********************************************************************************/
        /* Initialisierung */
        /********************************************************************************/

        String  string       = null;
        String  fileName     = null;
        String  printName    = null;
        String  pipeName     = null;
        boolean landscape    = false;
        int     copies       = 1;
        String  printJobName = null;

        File         f      = null;
        OutputStream output = null;

        /********************************************************************************/
        /* Probiert, um eine Pipe-Datei (Ergebnisse des Prozess) zu kreieren */
        /********************************************************************************/

        try {
            pipeName = args[2];
            f        = new File(pipeName);
            output   = Files.newOutputStream(f.toPath(), StandardOpenOption.CREATE_NEW);
        } catch (Exception e) {
            string = ("Fehler beim Lesen des Pipe-Datei: " + e.getLocalizedMessage());
            output.close();
            System.exit(-1);
        }

        /********************************************************************************/
        /* Prüft, ob die obligatorische Parametern angegeben sind */
        /* Param1 = Dateiname zu drucken */
        /* Param2 = umfassende oder partiell Druckername */
        /********************************************************************************/

        try {
            fileName  = args[0].replace("\\", "/");
            printName = args[1];
        } catch (Exception e) {
            string = "Falsche Parametern an DocPrint gegeben.";

            output.write(string.getBytes());
            output.close();
            System.exit(-1);
        }

        /********************************************************************************/
        /* Optionale Parametern */
        /* Param3 = Farbe ja/nein */
        /* Param4 = Anzahl der Kopien */
        /********************************************************************************/
        try {
            landscape = Boolean.valueOf(args[3]);
        } catch (Exception e) {}
        try {
            copies = Integer.parseInt(args[4]);
        } catch (Exception e) {}
        try {
            printJobName = args[5];
        } catch (Exception e) {}

        /********************************************************************************/
        /* Probiert, um die gedruckte Datei zu lesen */
        /********************************************************************************/

        FileInputStream psStream = null;

        try {
            psStream = new FileInputStream(fileName);
        } catch (Exception ffne) {
            string = ("Datei nicht gefunden: " + ffne.getMessage() + "\0");
            output.write(string.getBytes());
            output.close();
            psStream.close();
            System.exit(-1);
        }

        /********************************************************************************/
        /* Vorbereite den Job-Prozess */
        /********************************************************************************/

        DocFlavor psInFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;

        Doc myDoc = new SimpleDoc(psStream, psInFormat, null);

        HashPrintRequestAttributeSet jobAttrSet = new HashPrintRequestAttributeSet();

        JobName jName = null;
        if (printJobName.isEmpty()) {
            jName = new JobName(fileName, null);
        } else {
            jName = new JobName(printJobName, null);
        }

        jobAttrSet.add(jName);

        if (copies > 1) {
            jobAttrSet.add(new Copies(copies));
        }

        if (landscape) {
            jobAttrSet.add(OrientationRequested.LANDSCAPE);
        } else {
            jobAttrSet.add(OrientationRequested.PORTRAIT);
        }
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, jobAttrSet);

        PrintService myPrinter = null;

        /********************************************************************************/
        /* Prüft, ob den Drucker existiert */
        /********************************************************************************/

        for (int i = 0; i < services.length; i++) {

            String svcName = services[i].getName();

            if (svcName.contains(printName)) {
                myPrinter = services[i];
                break;
            }
        }

        /********************************************************************************/
        /* Start den Druck-Job */
        /********************************************************************************/

        if (myPrinter != null) {
            try {
                DocPrintJob job = myPrinter.createPrintJob();
                job.print(myDoc, jobAttrSet);
            } catch (Exception pe) {
                string = ("Fehler beim Ausdruck des Dokument: " + pe.getMessage() + "\0");
                output.write(string.getBytes());
                psStream.close();
                output.close();
                System.exit(-1);
            }

        } else {
            string = ("Drucker nicht gefunden" + "\0");
            output.write(string.getBytes());
            psStream.close();
            output.close();
            System.exit(-1);
        }

        /********************************************************************************/
        /* Wenn alles klappt, dann die Ergebnis muss "OK\0" enthalten */
        /********************************************************************************/

        string = ("OK\0");

        output.write(string.getBytes());
        psStream.close();
        output.close();
    }
}
