package haven.error;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ErrorLogFormatter extends Formatter {

    private final Date dat = new Date();
    private long last = 0;
    
    @Override
    public synchronized String format(LogRecord record) {
	long now = record.getMillis();
        dat.setTime(now);
        String message = formatMessage(record)+"\n";
        if(now - last > 1000 ){
            message = String.format("%s\n%s", dat.toString(), message);
        } 
        last = now;
	return message;
    }

}
