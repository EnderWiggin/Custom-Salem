package haven.error;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingOutputStream extends ByteArrayOutputStream {
    private Logger logger;
    private Level level;
    private String lineSeparator;

    public LoggingOutputStream(Logger logger, Level level) {
	super(); 
	this.logger = logger;
	this.level = level;
	lineSeparator = System.getProperty("line.separator"); 
    } 

    /*\* 
     \* upon flush() write the existing contents of the OutputStream
     \* to the logger as a log record. 
     \* @throws java.io.IOException in case of error 
     \*/ 
    public void flush() throws IOException {

	String record; 
	synchronized(this) {
	    super.flush(); 
	    record = this.toString(); 
	    super.reset(); 

	    if (record.length() == 0 || record.equals(lineSeparator)) {
		// avoid empty records 
		return; 
	    } 

	    logger.logp(level, "", "", record);
	}
    }
}
