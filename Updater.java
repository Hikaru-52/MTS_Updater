import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Updater {
	
	private static String modName;
	
	private static String modFolder;
	
	private static String assetsPath;

	private static ModExtractor me = new ModExtractor();
	
	public boolean updateMod(String mod) {
		modName = mod;
		modFolder = me.tempDir + "\\" + modName;
		assetsPath = modFolder + "\\assets";
		
		boolean success;
		String msg;
		
		// Scan assets directory
		List<String> packs = new ArrayList<>();
		packs = scanAssets();
		
		if (packs.isEmpty()) {
			msg = "No packages found, update failed";
			Main.log.WriteLogs(msg, "WARN");
			System.err.println(msg);
			return false; // If no packs are found fail
		}
		
		// Create assets\mts\models\item directory
		success = createItemDir();
		if (!success) {
			msg = "❌ Unable to create directory assets\\mts\\models\\item";
			Main.log.WriteLogs(msg, "WARN");
			System.err.println(msg);
			return false; // If it was not possible to create the directories then return failure
		}
		
		// Move every file in assets\pack\textures\item sub directories into item
		for (String pack : packs) {
			msg = Main.RESET+" > Processing package " +Main.YELLOW+pack+Main.RESET+"...";
			Main.log.WriteLogs(msg, "INFO");
			System.out.println(msg);
			
			String packDir = assetsPath + "\\" + pack + "\\textures\\item";
			
			// Rename assets\pack\textures\items into item
			success = itemDirFix(packDir);
			
			if (!success) {
				msg = "❌ Unable to find directory " + packDir;
				Main.log.WriteLogs(msg, "WARN");
				System.err.println(msg);
				continue;
			}
			
			// Check for sub directories and moves textures to item
			success = checkDirs(packDir);
			if (!success) {
				msg = "❌ Unable to find or move textures to " + packDir + " from its sub directories";
				Main.log.WriteLogs(msg, "WARN");
				System.err.println(msg);
				continue;
			}			
			
			// Create json files for every texture file
			File [] textures = getTextures(packDir);
			
			for(File texture : textures) {
				String fileName = pack + "." + texture.getName().replace(".png", "") + ".json";
				String filePath = modFolder + "\\assets\\mts\\models\\item\\" + fileName;
				
				Main.log.WriteLogs(filePath, "DEBUG");
				
				File file = new File (filePath);
				
				// Create file
				try {
					file.getAbsoluteFile().createNewFile();
				} catch (Exception e) {
					Main.log.WriteLogs("❌ Error during file creation: " + e.getMessage(), "ERROR");
					return false;
				}
				if (file.exists()) {
					// Write on file
					try (FileWriter writer = new FileWriter(file, true)) { // Append mode
			            writer.write("{ \n" + 
			            		"\"parent\": \"mts:item/basic\",\n" +
			            		"\"textures\": {\n" +
			            			"\"layer0\": \"" + pack + ":item/" + texture.getName().replace(".png", "") + "\"\n" +
			            			"}\n" +
			            		"}");
			        } catch (IOException ex) {
			            Main.log.WriteLogs("❌ Exception occurred while writing on file " + file + ": " + ex, "ERROR");
			        }
				}
				else {
					Main.log.WriteLogs("❌ File not found, unable to write.", "ERROR");
				}
				
			}
			
			msg = " > Processed package " +Main.YELLOW+pack;
			Main.log.WriteLogs(msg, "INFO");
			System.out.println(msg);
		}
		
		return true;
	}
	
	private static File[] getTextures(String path) {
		File item = new File(path);
		
		File[] textureList = item.listFiles((d, name) -> name.endsWith(".png")); // Gets all textures in item
		
		return textureList;
	}
	
	private static boolean checkDirs(String path) {
		String msg;
		File item = new File(path);
		
		File[] subDirsArr = item.listFiles(File::isDirectory); // Gets all sub directories
		
		for (File sd : subDirsArr) {
			if (sd.isDirectory()) {
				checkDirs(sd.getPath());
			}
			if (!moveTextures(sd.getPath(), path)) {
				msg = "❌ Failed to move textures from " + sd.getPath() + " to " + path;
				Main.log.WriteLogs(msg, "WARN");
				//System.err.println(msg);
				return false;
			}
			else {
				msg = "Successfully moved textures from " + sd.getPath() + " to " + path;
				Main.log.WriteLogs(msg, "INFO");
				//System.out.println(msg);
				sd.delete(); // Deletes sub directory after finishing the operation
			}
		}
		
		return true;
	}
	
	private static boolean moveTextures(String source, String target) {
		File s = new File (source);
		File t = new File (target);
		String msg;
		
		File[] textures = s.listFiles((d, name) -> name.endsWith(".png")); // Gets all textures to transfer
		if (textures.length == 0) {
			msg = "❌ No textures found in " + source;
			Main.log.WriteLogs(msg, "WARN");
			//System.err.println(msg);
			return true;
		}
		
		for (File texture : textures) {
			File destFile = new File(t, texture.getName());
            try {
                Files.move(texture.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                msg = "Moved: " + texture.getName();
                //System.out.println(msg);
                Main.log.WriteLogs(msg, "INFO");
            } catch (IOException e) {
            	msg = " ❌ Failed to move: " + texture.getName() + " - " + e.getMessage();
                //System.out.println(msg);
                Main.log.WriteLogs(msg, "WARN");
                return false;
            }
		}
		return true;
	}

	private static boolean itemDirFix(String p) {
		String msg;
		String packDir = p;
		String packDir2;
		File itemDir = new File(packDir);
		
		if (!itemDir.exists()) {
			 packDir2 = packDir + "s";
			 File itemDirToFix = new File(packDir2);
			 if (itemDirToFix.exists()) {
				itemDirToFix.renameTo(itemDir);
				msg = " > Items folder found: renamed it to "+Main.YELLOW+"item"+Main.RESET;
				Main.log.WriteLogs(msg, "INFO");
				System.out.println(msg);
			 }
			 else {
				 return false;
			 }
		}
		else {
			msg = "\uD83D\uDCC4 Directory " + Main.YELLOW+packDir + Main.RESET+" found";
			Main.log.WriteLogs(msg, "INFO");
			System.out.println(msg);
		}
		
		return true;
	}
	
 	private static List<String> scanAssets() {
		File assets = new File(assetsPath);
		
		File[] packList = assets.listFiles(File::isDirectory);
		
		List<String> packs = new ArrayList<>();
		
		for (File pack : packList) {
			packs.add(pack.getName());
		}
		
		return packs;
	}

	private static boolean createItemDir() {
		String itemDirPath =  modFolder + "\\assets\\mts\\models\\item";
		// Create a File object
        File directory = new File(itemDirPath);
        
        // Create directories if they do not exist
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (success) {
                Main.log.WriteLogs("Directories created: " + itemDirPath, "INFO");
                return true;
            } else {
            	Main.log.WriteLogs("Failed to create directories: " + itemDirPath, "WARN");
                return false;
            }
        } else {
        	Main.log.WriteLogs("Directories already exist: " + itemDirPath, "INFO");
            return true;
        }
		
	}
}
