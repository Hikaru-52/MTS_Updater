import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class Logger {
	
	private File logFile;
	
	public void SetLogFile(String path) {
		logFile = new File(path);
		// check if file exists 
		if (!logFile.exists()) {
			CreateLogFile();
		} else {
			ClearLogFile();
		}
	}
	
	private void CreateLogFile() {
		try {
            if (logFile.createNewFile()) {
                //System.out.println("Log file created: " + logFile.getAbsolutePath());
            } else {
                //System.out.println("Log file already exists.");
            }
        } catch (IOException ex) {
            System.out.println("Exception occurred while creating log file: " + ex);
        }
	}
	
    private void ClearLogFile() {
    	try (FileWriter writer = new FileWriter(logFile, false)) { // Overwrites file
            writer.write(""); // Clears the content
            //System.out.println("Log file cleared: " + logFile.getAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Exception while clearing log file: " + ex);
        }
    }
	
	public void WriteLogs(String message, String level) {
		if (logFile == null) {
            System.out.println("Log file is not set. Call SetLogFile first.");
            return;
        }
		
		// create instance for time
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		
        try (FileWriter writer = new FileWriter(logFile, true)) { // Append mode
            writer.write(date + " | " + time + " | " + level + ": " + message + "\n");
        } catch (IOException ex) {
            System.out.println("Exception occurred while writing logs: " + ex);
        }
	}
}
