import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.nio.file.*;

public class ModExtractor {
	
	// Set Workspace Folder
	private static String mainFolder = "MTS_Updater_Workspace";
	
	private static String modFolder = mainFolder + "\\mods";
	private static String tempFolder = mainFolder + "\\temp";
	private static String outputFolder = mainFolder + "\\output";
	
	public File tempDir = new File (tempFolder);
	
	public boolean GetUpdatedMod(String mod) {
		String modPath = tempFolder + "\\" + mod;
		
		String zippedMod = outputFolder + "\\" + mod + ".zip";
		
		// Zip mod
	    try {
	        zipFile(modPath, zippedMod);

	    } catch (IOException e) {
	    	String msg = "Error during zipping for mod " + mod + ": " + e.getMessage();
	        Main.log.WriteLogs(msg, "ERROR");;
	        return false;  // Indicate failure
	    }
	    
	    // Get .jar file from .zip file
	    boolean success = convertZipToJar(zippedMod);
		
		if (!success) {
			return false;
		}
	    
		return true;
	}
	
	public boolean convertZipToJar(String zipFilePath) {
	    String msg;
		// Ensure the file exists
	    File zipFile = new File(zipFilePath);
	    if (!zipFile.exists() || !zipFilePath.endsWith(".zip")) {
	    	msg = "Error: ZIP file does not exist or is invalid.";
	        System.out.println(msg);
	        Main.log.WriteLogs(msg, "ERROR");
	        return false;
	    }

	    // Change the extension from .zip to .jar
	    String jarFilePath = zipFilePath.substring(0, zipFilePath.length() - 4) + ".jar";
	    File jarFile = new File(jarFilePath);
	    
	    // Delete existing JAR file if it exists
	    if (jarFile.exists()) {
	        if (!jarFile.delete()) {
	            msg = "Error: Could not delete existing JAR file.";
	            System.out.println(msg);
	            Main.log.WriteLogs(msg, "ERROR");
	            return false;
	        }
	    }
	    
	    // Rename the file
	    if (zipFile.renameTo(jarFile)) {
	    	msg = "Successfully converted ZIP to JAR: " + jarFilePath;
	        System.out.println(msg);
	        Main.log.WriteLogs(msg, "INFO");
	        return true;
	    } else {
	    	msg = "Error: Could not convert ZIP to JAR.";
	        System.out.println(msg);
	        Main.log.WriteLogs(msg, "ERROR");
	        return false;
	    }
	}
	
	public static void zipFile(String sourceDirPath, String zipFilePath) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            Path sourceDir = Paths.get(sourceDirPath);
            
            Files.walk(sourceDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString().replace("\\", "/"));
                        zipOut.putNextEntry(zipEntry);
                        Files.copy(path, zipOut);
                        zipOut.closeEntry();
                    } catch (IOException e) {
                    	String msg = "Error zipping the file: " + e.getMessage();
                        System.err.println(msg);
                        Main.log.WriteLogs(msg, "ERROR");
                    }
                });
        }
    }
	
	public List<String> scanJars() {
        List<String> extractedDirectories = new ArrayList<>();
        File dir = new File(modFolder);
        
        // Check if modFolder exists
        if (!dir.exists() || !dir.isDirectory()) {
        	String msg = "Invalid directory: " + modFolder;
            System.out.println(msg);
            Main.log.WriteLogs(msg, "WARN");
            return extractedDirectories;
        }
        
        // Add jar files to an array and check if there are any
        File[] jarFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
        	String msg = "No JAR files found in the directory: " + modFolder;
            System.out.println(msg);
            Main.log.WriteLogs(msg, "WARN");
            return extractedDirectories;
        }
        
        for (File jarFile : jarFiles) {
            String DirName = jarFile.getName().replace(".jar", "");
            extractedDirectories.add(DirName);
            
        }
        
        String msg = extractedDirectories.toArray().length + " mods found";
        System.out.println(msg);
        Main.log.WriteLogs(msg, "INFO");
                
        // return list
        return extractedDirectories;
	}
	
	public boolean unzipMod(String path) {
		
		String sourcePath = modFolder + "\\" + path + ".jar";
		File fileToUnzip = new File(sourcePath);
		String extractedDirName = tempFolder + "\\" + path;
        File extractedDir = new File(extractedDirName);
        
        if (!extractedDir.exists() && extractedDir.mkdirs()) {
        	unzipJar(fileToUnzip, extractedDir);
        }
        
        if (extractedDir.exists() && extractedDir.isDirectory()) {
        	if (!isDirEmpty(extractedDir)) {
        		return true; // If extractedDir is exists, is a directory and is not empty then extraction was successful
        	}
        	else {
        		return false; // If extractedDir exists, is a directory but is empty the extraction failed
        	}
        }
        else {
        	return false; // If extractedDir either does not exist or is not a directory extraction failed
        }
	}
	
	private static void unzipJar(File jarFile, File outputDir) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(outputDir, entry.getName());
                
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
        	String msg = "Error extracting JAR file: " + jarFile.getName() + ". Error: " + e.getMessage();
            System.err.println(msg);
            Main.log.WriteLogs(msg,"ERROR");
        }
    }
		
	public void tempDirExists() {
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
	}

	private static boolean isDirEmpty(File dir) {
		String[] files = dir.list();
        if (files != null && files.length == 0) {
        	return true; // Empty
        } else {
        	return false; // Not Empty
        }
	}
}
