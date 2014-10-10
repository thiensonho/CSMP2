/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cs.chat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author s506571
 */
public class NotStupidLogFormatter extends Formatter {

    private static final String SEPARATOR = System.getProperty("line.separator");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date(record.getMillis())));
        sb.append(" ");
        sb.append("[");
        sb.append(record.getLoggerName());
        sb.append("] ");
        sb.append(record.getLevel().getLocalizedName());
        sb.append(": ");
        sb.append(formatMessage(record));
        sb.append(SEPARATOR);
        if (record.getThrown() != null) {
            try {
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
