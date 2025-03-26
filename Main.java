import java.time.LocalDate;
import java.util.*;
import java.nio.file.*;
import java.io.File;
import java.io.IOException;


public class Main {
	
	public static Logger log = new Logger();
	private static ModExtractor me = new ModExtractor();
	private static Updater upd = new Updater();
	
	public static void main(String[] args) {
		
		// Set log file path
		
		LocalDate date = LocalDate.now();
		
		String logFilePath = "logs\\" + date + "_log.txt";
		
		log.SetLogFile(logFilePath);
		
		// Program Started
		
		log.WriteLogs("Program Started", "INFO");
		
		// Clear temp folder
		clearTemp();
		
		System.out.println("Scanning mod folder...");
		
		List<String> mods = me.scanJars();
		
		String msg;
		
		// Process mods
		for (String mod : mods) {
			// Check if tempDir exists
			me.tempDirExists();
			
			msg = "Processing " + mod + "...";
			System.out.println(msg);
			log.WriteLogs(msg, "INFO");
			
			// Extraction Process
			boolean success = me.unzipMod(mod);
			
			if (!success) {
				msg = "Failed to extract mod " + mod;
				System.err.println(msg);
				log.WriteLogs(msg, "WARN");
				clearTemp();
				continue;
			}
			
			msg = mod + " has been successfully extracted";
			System.out.println(msg);
			log.WriteLogs(msg, "INFO");
			
			// Updating Process
			success = upd.updateMod(mod);
			
			if (!success) {
				msg = "Failed to update mod " + mod;
				System.err.println(msg);
				log.WriteLogs(msg, "WARN");
				clearTemp();
				continue;
			}
			
			msg = mod + " has been successfully updated";
			System.out.println(msg);
			log.WriteLogs(msg, "INFO");
			
			// Get results
			
			success = me.GetUpdatedMod(mod);
			
			if (!success) {
				msg = "Failed to create updated mod jar file: " + mod;
				System.err.println(msg);
				log.WriteLogs(msg, "WARN");
				clearTemp();
				continue;
			}
			
			msg = "Finished processing " + mod;
			System.out.println(msg);
			log.WriteLogs(msg, "INFO");
		}
		
	}
	
	public static void clearTemp() {
		try {
	        Path tempPath = me.tempDir.toPath();
	        Files.walk(tempPath)
	             .sorted(Comparator.reverseOrder()) // Delete files before the folder
	             .map(Path::toFile)
	             .forEach(File::delete);

	        me.tempDir.mkdir(); // Recreate the folder
	        log.WriteLogs("Successfully cleared temp folder", "INFO");

	    } catch (IOException e) {
	        log.WriteLogs("Failed to clear temp folder: " + e.getMessage(), "ERROR");
	    }
		return;
	}


}
