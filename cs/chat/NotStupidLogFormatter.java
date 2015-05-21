package cs.chat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Make the log look pretty
 */
public class NotStupidLogFormatter extends Formatter {
    private static final String SEPARATOR = System.getProperty("line.separator");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder(); // we're going to append a lot
        // use StringBuilder to avoid expensive string copying
        // use the date format so the log looks OK
        sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date(record.getMillis())));
        sb.append(" ");
        sb.append("[");
        sb.append(record.getLoggerName()); // put the name
        sb.append("] ");
        sb.append(record.getLevel().getLocalizedName()); // put the level
        sb.append(": ");
        sb.append(formatMessage(record)); // put the message
        sb.append(SEPARATOR);
        if (record.getThrown() != null) { // if there's an error
            try { // put the error
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }catch (Exception e) {}
        }
        return sb.toString();
    }

}
