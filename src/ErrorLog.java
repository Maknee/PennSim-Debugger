import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.util.Date;
import java.util.Calendar;
import java.io.PrintWriter;

// 
// Decompiled by Procyon v0.5.30
// 

public class ErrorLog
{
    private static PrintWriter log;
    private static String logDelim;
    private static boolean logOpen;
    
    private static void logTimeStamp() {
        if (!ErrorLog.logOpen) {
            logInit();
        }
        if (ErrorLog.log == null) {
            return;
        }
        ErrorLog.log.write(new Date(Calendar.getInstance().getTimeInMillis()).toString() + ": ");
    }
    
    public static void logError(final String s) {
        if (!ErrorLog.logOpen) {
            logInit();
        }
        if (ErrorLog.log == null) {
            return;
        }
        logTimeStamp();
        ErrorLog.log.write(s);
        ErrorLog.log.write(ErrorLog.logDelim);
    }
    
    public static void logError(final Exception ex) {
        if (!ErrorLog.logOpen) {
            logInit();
        }
        if (ErrorLog.log == null) {
            return;
        }
        logTimeStamp();
        ex.printStackTrace(ErrorLog.log);
        ErrorLog.log.write(ErrorLog.logDelim);
    }
    
    private static void logInit() {
        if (!ErrorLog.logOpen) {
            try {
                ErrorLog.log = new PrintWriter(new FileWriter("pennsim_errorlog.txt"), true);
            }
            catch (IOException ex) {
                ErrorLog.log = null;
            }
        }
    }
    
    public static void logClose() {
        if (ErrorLog.log == null) {
            return;
        }
        ErrorLog.log.close();
    }
    
    static {
        ErrorLog.logDelim = "\n-----\n";
        ErrorLog.logOpen = false;
    }
}
