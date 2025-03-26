import java.time.LocalDate;
import java.util.*;
import java.nio.file.*;
import java.io.File;
import java.io.IOException;


public class Main {
	public static final String RESET = "\u001B[0m";
	public static final String RED = "\u001B[31m";
	public static final String ORANGE = "\u001B[38;5;214m";
	public static final String YELLOW = "\u001B[33m";
	public static final String GREEN = "\u001B[32m";
	public static final String BLUE = "\u001B[36m";
	public static final String INDIGO = "\u001B[38;5;54m";
	public static final String VIOLET = "\u001B[35m";
	
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
		
		System.out.println("🔍 Scanning "+ORANGE+"mod"+RESET+" folder...");
		
		List<String> mods = me.scanJars();
		
		String msg;
		
		// Process mods
		for (String mod : mods) {
			// Check if tempDir exists
			me.tempDirExists();
			
			msg = "\n⏳ Processing "+ GREEN + mod +RESET +"...";
			System.out.println(msg);
			log.WriteLogs(msg, "INFO");
			
			// Extraction Process
			boolean success = me.unzipMod(mod);
			
			if (!success) {
				msg = "❌ "+RED+"Failed "+RESET+"to extract"+ORANGE+"mod "+GREEN+ mod;
				System.err.println(msg);
				log.WriteLogs(msg, "WARN");
				clearTemp();
				continue;
			}
			
			msg = "✅ "+ORANGE+mod+RESET + " has been successfully extracted";
			System.out.println(msg);
			log.WriteLogs(msg, "INFO");
			
			// Updating Process
			success = upd.updateMod(mod);
			
			if (!success) {
				msg = "❌ "+RED+"Failed "+RESET+" to update "+ORANGE+"mod "+GREEN+ mod;
				System.err.println(msg);
				log.WriteLogs(msg, "WARN");
				clearTemp();
				continue;
			}
			
			msg = "\uD83D\uDD04 "+ORANGE+mod+RESET + " has been successfully updated";
			System.out.println(msg);
			log.WriteLogs(msg, "INFO");
			
			// Get results
			
			success = me.GetUpdatedMod(mod);
			
			if (!success) {
				msg = "❌ "+RED+"Failed "+RESET+"to create updated"+ORANGE+"mod "+"jar file: "+GREEN+ mod+RESET;
				System.err.println(msg);
				log.WriteLogs(msg, "WARN");
				clearTemp();
				continue;
			}
			
			msg = "Finished processing "+GREEN+mod+RESET;
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
