/*
 * MCDocsPlus by RTRD (Originally by Tazzernator)
 * (RTRD ~ http://rtrd.no)
 * 
 * THIS PLUGIN IS LICENSED UNDER THE WTFPL - (Do What The Fuck You Want To Public License)
 * 
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 * 
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *   
 * 0. You just DO WHAT THE FUCK YOU WANT TO.
 *   
 * */

/*
 * 
 * MCDocsPlus by RTRD
 * (RTRD ~ http://rtrd.no)
 *    
 */

package no.rtrd.bukkit.mcdocsplus;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.config.Configuration;

//import com.nijikokun.register.payment.Methods;

//Listener Class
public class Listener extends PlayerListener {
	
	public Permission permissions;
	
	private Main plugin;
	Configuration config;
	static String tFormat;
	static Time time = new Time();
	public static final Logger log = Logger.getLogger("Minecraft");
	private ArrayList<String> fixedLines = new ArrayList<String>();
	private ArrayList<Commands> records = new ArrayList<Commands>();
	
	//Config Defaults.
	public String headerFormat = "&c%commandname - Page %current of %count &f| &7%command <page>";
	public String onlinePlayersFormat = "%name";
	public String newsFile = "news.txt";
	public int newsLines = 1;
	public boolean motdEnabled = true;
	public static boolean commandLogEnabled = true;
	public List<String> commandsList = new ArrayList<String>();
	public int cacheTime = 5;
	public String timeFormat = "hh:mm a";
	
	
	/*
	 * -- Constructor for MCDocsPlusListener --
	 * All we do here is import the instance.
	 */
	
	public Listener (Main instance) {
      this.plugin = instance;
  }

	/*
	 * -- Configuration Methods --
	 * We check for config.yml, if it doesn't exists we create a default (defaultCofig), then we load (loadConfig). 
	 */
	
	public void setupConfig(Configuration config){
		
		this.config = config;
		
		if (!(new File(plugin.getDataFolder(), "config.yml")).exists()){
			defaultConfig();
		}
      loadConfig();
	}
	
	private void defaultConfig(){
		//default commands and files
		commandsList.add("/motd:motd.txt");
		commandsList.add("/rules:rules.txt");
		commandsList.add("/news:news.txt");
		commandsList.add("/reg:reg.txt");
		commandsList.add("/about:http://tazzernator.com/files/bukkit/plugins/MCDocs/about.txt");
		
		config.setProperty("header-format", headerFormat);
		config.setProperty("online-players-format", onlinePlayersFormat);
		config.setProperty("commands-list", commandsList);
		config.setProperty("motd-enabled", true);
		config.setProperty("command-log-enabled", true);
		config.setProperty("news-file", newsFile);
		config.setProperty("news-lines", 1);
		config.setProperty("time-format", "hh:mm a");
		config.setProperty("cache-time", cacheTime);
		saveConfig();
	}
	
	public void loadConfig(){
		config.load();

		
		//7 - 7.1 update check - add motd-enabled if it doesn't exist.
		Object val = config.getProperty("motd-enabled");
		if (val == null){
			config.setProperty("motd-enabled", true);
			log.info("[MCDocs+] motd-enabled added to config.yml with default true.");
		}
		
		//8 - 9 update check - add news-file if it doesn't exist.
		Object val2 = config.getProperty("news-file");
		if (val2 == null){
			config.setProperty("news-file", newsFile);
			config.setProperty("news-lines", 1);
			log.info("[MCDocs+] news-file added to config.yml with default news.txt.");
			log.info("[MCDocs+] news-lines added to config.yml with default 1.");
		}
		
		//9.x - 10 update check - add cache-time if it doesn't exist.
		Object val3 = config.getProperty("cache-time");
		if (val3 == null){
			config.setProperty("cache-time", 5);
			log.info("[MCDocs+] cache-time added to config.yml with default 5.");
		}
		
		headerFormat = config.getString("header-format", headerFormat);
		onlinePlayersFormat = config.getString("online-players-format", onlinePlayersFormat);
		commandsList = config.getStringList("commands-list", commandsList);	
		motdEnabled = config.getBoolean("motd-enabled", motdEnabled);
		commandLogEnabled = config.getBoolean("command-log-enabled", commandLogEnabled);
		newsFile = config.getString("news-file", newsFile);
		newsLines = config.getInt("news-lines", newsLines);
		cacheTime = config.getInt("cache-time", cacheTime);
		tFormat = config.getString("time-format", "hh:mm a");

		//Update our Commands
      Commands record = null;
      records.clear();
		File folder = plugin.getDataFolder();
	    String folderName = folder.getParent();
      
      for (String c : commandsList){
      	try{
      		//I can't be arsed with regex.
      		c = c.replace("http:", "http~colon~");
      		String[] parts = c.split(":");
      		
      		if(parts.length == 3){
      			if(parts[1].contains("http")){
      				record = new Commands(parts[0], parts[1].replace("http~colon~", "http:"), parts[2]);
          		}
      			else{
      				record = new Commands(parts[0], folderName + "/MCDocsPlus/" + parts[1], parts[2]);
      			}
      		}
      		else if(parts.length == 2){
      			if(parts[1].contains("http")){
      				record = new Commands(parts[0], parts[1].replace("http~colon~", "http:"), "null");
          		}
      			else{
      				record = new Commands(parts[0], folderName + "/MCDocsPlus/" + parts[1], "null");
      			}
         		}
      		records.add(record);
      	}
      	catch (Exception e) {
      		log.info("[MCDocs+]: Error reading the commandsList. config.yml incorrect.");
      	}
      }
      //Finally re-write what we've loaded just in case of an update.
      saveConfig();
	}
	
	private void saveConfig(){
	    //Since version 9.3, we're going to manually save the config, as to support commenting.
		PrintWriter stream = null;
		File folder = plugin.getDataFolder();
		if (folder != null) {
			folder.mkdirs();
      }
		String folderName = folder.getParent();
		PluginDescriptionFile pdfFile = this.plugin.getDescription();
		
		try {
			stream = new PrintWriter(folderName + "/MCDocsPlus/config.yml");
			//Let's write our goods ;)
				stream.println("#MCDocsPlus v" + pdfFile.getVersion() + " by RTRD");
				stream.println("#Configuration File.");
				stream.println("#For detailed assistance please visit: http://wiki.rtrd.no/index.php?title=MCDocsPlus");
				stream.println();
				stream.println("#Here we determine which command will show which file. ");
				stream.println("commands-list:");
				for (String c : commandsList){
					stream.println("- " + c);
				}
				stream.println();
				stream.println("#This changes the pagination header that is added to MCDocs automatically when there is > 10 lines of text.");
				stream.println("header-format: '" + config.getString("header-format", headerFormat) + "'");
				stream.println();
				stream.println("#Format to use when using %online or %online_group.");
				stream.println("online-players-format: '" + config.getString("online-players-format", onlinePlayersFormat) + "'");
				stream.println();
				stream.println("#The file to displayed when using %news.");
				stream.println("news-file: " + config.getString("news-file", newsFile));
				stream.println();
				stream.println("#How many lines to show when using %news.");
				stream.println("news-lines: " + config.getInt("news-lines", newsLines));
				stream.println();
				stream.println("#How long, in minutes, do you want online files to be cached locally? 0 = disable");
				stream.println("cache-time: " + config.getInt("cache-time", cacheTime));
				stream.println();
				stream.println("#Time format");
				stream.println("time-Format: " + config.getString("time-Format", tFormat));
				stream.println();
				stream.println("#Show a MOTD at login? Yes: true | No: false");
				stream.println("motd-enabled: " + config.getBoolean("motd-enabled", motdEnabled));
				stream.println();
				stream.println("#Inform the console when a player uses a command from the commands-list.");
				stream.println("command-log-enabled: " + config.getBoolean("command-log-enabled", commandLogEnabled));
				stream.close();
		} catch (FileNotFoundException e) {
			log.info("[MCDocs+]: Error saving the config.yml.");
		}
	}

	
		 public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		//List of lines we read our first file into.
				ArrayList<String> lines = new ArrayList<String>();
				
				//Find the current Player, Message
				String[] split = event.getMessage().split(" ");
		        Player player = event.getPlayer();
		        
		        //Here we are going to support spaces in commands.
				int count = split.length;
				int lastInput = count - 1;
				String playerCommand = "";
				if(checkIfNumber(split[lastInput])){
				for (int i=0; i<lastInput; i++){
						playerCommand = playerCommand + split[i] + " ";
					}
				}
				else {
					for (int i=0; i<count; i++){
						playerCommand = playerCommand + split[i] + " ";
					}
				}
				
				//Cut off the final space.
				playerCommand = playerCommand.trim();
							
		        
				for (Commands r : records){
		        	lines.clear();
		        	fixedLines.clear();
		        	String command = r.getCommand();
		        	int page = 0;
		        	String permission = "allow";
		        	
	
		         	if (playerCommand.equalsIgnoreCase(command)){
		        		  
		        		
		        		
		        		//Permissions check - Hopefully should default to allow if it isn't installed.
		        		if(player.hasPermission("mcdocsplus.read." + command)){
		        			permission = "allow";
		        			//Log our user using the command.
	                		if (commandLogEnabled == true){
	                			log.info("MCDocsPlus: " + player.getName() + ": " + event.getMessage());
	                			
	                		}
	                		else {
	                			permission = "deny";
	                		}
	                		
	                		}
		        		if (permission == "allow"){
		        			String fileName = r.getFile(); 
		        			
		        			//Online file use
		        			if(fileName.contains("http")){
		        				ArrayList<String> onlineLines = new ArrayList<String>();
		        				onlineLines = onlineFile(fileName);
		        				for(String o : onlineLines){
		        					lines.add(o);
		        				}
		        			}
		        			else{
				        		try {		
				        			//Regular files
				        			//Add out lines to the list "lines"
					                    FileInputStream fis = new FileInputStream(fileName);
					                    Scanner scanner = new Scanner(fis, "UTF-8");
					    	                while (scanner.hasNextLine()) {
					    	                	try{
					    	                		lines.add(scanner.nextLine() + " ");
					    	                	}
					    	                	catch(Exception ex){
					    	                		lines.add(" ");
					    	                	}
					    	                }
					                    scanner.close();
					                    fis.close();
				        		}	        		
				                catch (Exception ex) {
				                	 	player.sendMessage("Error: " + ex);
				                }
		        			}
			                 //If split[lastInput] does not exist, or has a letter, page = 1
		                    try{
		                        page = Integer.parseInt(split[lastInput]);
		                    }
		                    catch(Exception ex){
		                    	page = 1;
		                    }
	                    
		                    //Finally - Process our lines!
		                    variableSwap(player, lines);
		                    linesProcess(player, command, page);
		        		}
		                event.setCancelled(true);
		        	}    
		        }
			}

	
	private int variableSwap(Player player, ArrayList<String> lines){
		
		
		//Swaping out some variables with their respective replacement.
		for(String l : lines){
			
			//Basics
			String fixedLine = l.replace("%name", player.getDisplayName());
			fixedLine = fixedLine.replace("%time", time.getTime());
        	fixedLine = fixedLine.replace("%onlinenow", onlineCount());
        	fixedLine = fixedLine.replace("%world", player.getWorld().getName());
        	fixedLine = fixedLine.replace("%ip", player.getAddress().getAddress().getHostAddress());
        	
        	//Permissions related variables
        	/*if (player.hasPermission("mcdocsplus.player")){
        		String group = Permission.getGroups(player.getWorld().getName(), player.getName());
        		if(fixedLine.contains("%online_")){
        			String tempString = fixedLine.trim();
        			String[] firstSplit = tempString.split(" ");
        			for(String s : firstSplit){
        				if(s.contains("%online_")){
        					String[] secondSplit = s.split("_");
        					String groupName = secondSplit[1].toLowerCase();
        					fixedLine = fixedLine.replace("%online_" + secondSplit[1], onlineGroup(groupName));
        				}
        			}
        		}
        		fixedLine = fixedLine.replace("%group", group);
        		try{
	        		fixedLine = fixedLine.replace("%prefix", Permission.getGroups(player.getWorld().getName(), group));
	        		fixedLine = fixedLine.replace("%suffix", Permission.getGroups(player.getWorld().getName(), group));
        		}
        		catch (Exception e){
        			fixedLine = fixedLine.replace("%prefix", "");
            		fixedLine = fixedLine.replace("%suffix", "");
        		}
        	} */
        	
        	
        	//Economy
        	/*if (this.plugin.getServer().getPluginManager().getPlugin("Register") != null) {
            	try{        
            		Methods.getMethod().getAccount(player.getName()).balance();
	                fixedLine = fixedLine.replaceAll("%balance", balance.toString());
	            }
            	catch(NoClassDefFoundError e){
            		fixedLine = fixedLine.replaceAll("%balance", "Please update iConomy to v5 or higher");
            	}
              }
            */
        	
            //More Basics
        	fixedLine = fixedLine.replace("%online", onlineNames());
        	fixedLine = colourSwap(fixedLine);
        	fixedLine = fixedLine.replace("&#!", "&");
        	
        	//Size
        	/*if (MCDocsPlus.PermissionsProvider != null){
        		String number = MCDocsPlus.PermissionsProvider.getGroup(player.getWorld().getName(), player.getName());
        		if(fixedLine.contains("%size_")){
        			String tempString = fixedLine.trim();
        			String[] firstSplit = tempString.split(" ");
        			for(String s : firstSplit){
        				if(s.contains("%size_")){
        					String[] secondSplit = s.split("_");
        					String groupNumber = secondSplit[1].toLowerCase();
        					fixedLine = fixedLine.replace("%size_" + secondSplit[1], onlineGroup(groupNumber));
        				}
        			}
        		}
        		fixedLine = fixedLine.replace("%size", number);
        	//} */
        	
        	fixedLine = fixedLine.replace("%size", onlineCount());
        	
        	//If the line currently in the for loop has "%include", we find out which file to load in by splitting the line up intesively.
        	ArrayList<String> files = new ArrayList<String>();
        	       	
			if (l.contains("%include") || l.contains("%news")){
				if (l.contains("%include")){
					String tempString = l.trim();
	    			String[] firstSplit = tempString.split(" ");
	    			for(String s : firstSplit){
	    				if(s.contains("%include")){
	    					s = " " + s;
	    					String[] secondSplit = s.split("%include_");
	    					s = s.replace(" ", "");
	    					fixedLine = fixedLine.replace(s, "");
	    					files.add(secondSplit[1]);
	    				}
	    			}
	    			if(!fixedLine.equals(" ")){
	    				fixedLines.add(fixedLine);
	    			}
	    			for(String f : files){
	    				includeAdd(f, player);
	    			}
				}
				if (l.contains("%news")){
					fixedLine = fixedLine.replace("%news", "");	
					if(!fixedLine.equals(" ")){
	    				fixedLines.add(fixedLine);
	    			}
					newsLine(player);
				}
        	}
			else{
				fixedLines.add(fixedLine);
			}
		}
		return cacheTime;
	}

	private void linesProcess(Player player, String command, int page){        
        //Define our page numbers
        int size = fixedLines.size();
        int pages;
        
        if(size % 9 == 0){
        	pages = size / 9;
        }
        else{
        	pages = size / 9 + 1;
        }
        
        //This here grabs the specified 9 lines, or if it's the last page, the left over amount of lines.
        String commandName = command.replace("/", "");
        commandName = commandName.toUpperCase();
        String header = null;
        
        if(pages != 1){
        	//Custom Header
			header = headerFormat;
            
            //Replace variables.
            header = colourSwap(header);
            header = header.replace("%commandname", commandName);
            header = header.replace("%current", Integer.toString(page));
            header = header.replace("%count", Integer.toString(pages));
            header = header.replace("%command", command);
            header = header + " ";
            
            player.sendMessage(header);
        }
        //Some Maths.
        int highNum = (page * 9);
        int lowNum = (page - 1) * 9;
        for (int number = lowNum; ((number < highNum) && (number < size)); number++){
        	player.sendMessage(fixedLines.get(number));
        }
	}
	
	private ArrayList<String> onlineFile(String url){
		
		//some variables for the method
		OnlineFiles file = null;
		ArrayList<String> onlineLines = new ArrayList<String>();
		ArrayList<OnlineFiles> onlineFiles = new ArrayList<OnlineFiles>();
		
		URL u;
	    InputStream is = null;
	    DataInputStream dis;
	    Date now = new Date();
	    long nowTime = now.getTime();
	    int foundFile = 0;
	    
	    //Check if a cache file has been previously created... only attempt to load it if it exists.
	    if (!(new File(plugin.getDataFolder(), "cache/onlinefiles.data")).exists()){
			log.info("[MCDocs+] No cache file found... Will make a new one.");
		}
	    else{
	    	ArrayList<String> tmpList = new ArrayList<String>();
	        try {
	        	File folder = plugin.getDataFolder();
	    	    String folderName = folder.getParent();
				FileInputStream fis = new FileInputStream(folderName + "/MCDocsPlus/cache/onlinefiles.data");
                Scanner scanner = new Scanner(fis, "UTF-8");
	                while (scanner.hasNextLine()) {
	                	try{
	                		tmpList.add(scanner.nextLine());
	                	}
	                	catch(Exception ex){
	                	}
	                }
                scanner.close();
                fis.close();
	            for(String l : tmpList){
	            	String[] split = l.split("~!!~");
	            	file = new OnlineFiles(Long.parseLong(split[0]), split[1]);
	            	onlineFiles.add(file);
	            }
	        }
	        catch(IOException e){
	        	log.info("[MCDocs+] error reading the cache file.");
	        }
	    }
	    
	    //create a new list to store are wanted objects
	    ArrayList<OnlineFiles> newOnlineFiles = new ArrayList<OnlineFiles>();
	    
	    //go through all the existing online files found in the cache, and check if they are still under the cache limit.
	    //delete all files, and entries, that are old.
	    for(OnlineFiles o : onlineFiles){
	    	long fileTimeMs = o.getMs();
			long timeDif = nowTime - fileTimeMs;
	    	int cacheTimeMs = cacheTime * 60 * 1000;
	    		    	
	    	if(timeDif > cacheTimeMs){
	    		File f = new File(plugin.getDataFolder(), "cache/" + fileTimeMs + ".txt");
	    		f.delete();
	    	}
	    	else{
	    		//add the objects we wish to keep.
	    		newOnlineFiles.add(o);
	    	}
	    }
	    
	    //clear out the old, and replace them with the objects we wanted to keep.
	    onlineFiles.clear();
	    for(OnlineFiles n : newOnlineFiles){
	    	onlineFiles.add(n);
	    }

	    //now sinply go through our cache files and check if our url has been cached before...
	    for(OnlineFiles o : onlineFiles){
	    	if(o.getURL().equalsIgnoreCase(url)){
	    		File folder = plugin.getDataFolder();
	    	    String folderName = folder.getParent();
	    		String fileName = folderName + "/MCDocsPlus/cache/" + Long.toString(o.getMs()) + ".txt";
	    		try{
	    		FileInputStream fis = new FileInputStream(fileName);
                Scanner scanner = new Scanner(fis, "UTF-8");
	                while (scanner.hasNextLine()) {
	                	try{
	                		onlineLines.add(scanner.nextLine() + " ");
	                	}
	                	catch(Exception ex){
	                		onlineLines.add(" ");
	                	}
	                }
                scanner.close();
                fis.close();
	    		}
	    		catch (Exception ex) {
            	 	log.info("[MCDocs+] Oops! - The cache is broken :s - File Not Found.");
	    		}
	    		foundFile = 1;
	    	}
	    }
	    
	    //finally if there was no cache, or the url was not in the cache, download the online file and cache it.
	    if(foundFile == 0){
	    	try{
	    		//import our online file
				u = new URL(url);
				is = u.openStream();  
				dis = new DataInputStream(new BufferedInputStream(is));
				Scanner scanner = new Scanner(dis, "UTF-8");
				while (scanner.hasNextLine()) {
                	try{
                		onlineLines.add(scanner.nextLine() + " ");
                	}
                	catch(Exception ex){
                		onlineLines.add(" ");
                	}
                }
				
				//Add our new file to the cache
				file = new OnlineFiles(nowTime, url);
            	onlineFiles.add(file);
            	
            	//save our new file to the dir
            	PrintWriter stream = null;
            	File folder = plugin.getDataFolder();
        		String folderName = folder.getParent();		
        		try {
        			new File(plugin.getDataFolder() + "/cache/").mkdir();
        			stream = new PrintWriter(folderName + "/MCDocsPlus/cache/" + nowTime + ".txt");
        			for(String l : onlineLines){
        				stream.println(l);
                	}
        			stream.close();
        		} catch (FileNotFoundException e) {
        			log.info("[MCDocs+]: Error saving " + nowTime + ".txt");
        		}
            	
			}
			catch (MalformedURLException mue) {
				log.info("[MCDocs+] Ouch - a MalformedURLException happened.");
	        }
			catch (IOException ioe) {
				log.info("[MCDocs+] Oops - an IOException happened.");
			}
			finally {
		         try {
		            is.close();
		         } 
		         catch (IOException ioe) {
	         	}
			}
	    }
	    
	    //save our updated cache file
	    PrintWriter stream = null;
		File folder = plugin.getDataFolder();
		String folderName = folder.getParent();		
		try {
			stream = new PrintWriter(folderName + "/MCDocsPlus/cache/onlinefiles.data");
				for(OnlineFiles o : onlineFiles){
					stream.println(o.getMs() + "~!!~" + o.getURL());
				}
				stream.close();
		} catch (FileNotFoundException e) {
			log.info("[MCDocs+]: Error saving the onlinefiles.data.");
		}
		//and finally return what we have found.
		return onlineLines;
	}
	
	/*
	 * -- Variable Methods --
	 * The following methods are used for various %variables in the txt files.
	 * 
	 * includeAdd: Is used to insert more lines into the current working doc.
	 * onlineNames: Finds the current online players, and using online-players-format, applies some permissions variables.
	 * onlineGroup: Finds the current online players, check if they're in the group specified, and using online-players-format, applies some permissions variables.
	 * onlineCount: Returns the current amount of users online.
	 * newsLine: Is used to insert the most recent lines (# defined in config.yml) from the defined news file (defined in the config.yml)
	 * checkIfNumber: simple try catch to determine if a space is in a command. Example: /help iconomy 2
	 * colorSwap: Uses the API to color swap instead of manually doing it.
	 */
	
	private void includeAdd(String fileName, Player player){
		//Define some variables
		ArrayList<String> tempLines = new ArrayList<String>();
		File folder = plugin.getDataFolder();
		String folderName = folder.getParent();
		
		//Ok, let's import our new file, and then send them into another variableSwap [ I   N   C   E   P   T   I   O   N ]
		try {
			//Online file use
			if(fileName.contains("http")){
				ArrayList<String> onlineLines = new ArrayList<String>();
				onlineLines = onlineFile(fileName);
				for(String o : onlineLines){
					tempLines.add(o);
				}
			}
			
			//Regular files
			else{
            FileInputStream fis = new FileInputStream(folderName + "/MCDocsPlus/" + fileName);
            Scanner scanner = new Scanner(fis, "UTF-8");
                while (scanner.hasNextLine()) {
                	try{
                		tempLines.add(scanner.nextLine() + " ");
                	}
                	catch(Exception ex){
                		tempLines.add(" ");
                	}
                }
            scanner.close();
            fis.close();
			}
            //Methods inside of methods!
            variableSwap(player, tempLines);
         }	        		
         catch (Exception ex) {
        	 log.info("[MCDocs+] Included file " + fileName + " not found!");
         }
	}
	
	private String onlineNames(){
		Player online[] = plugin.getServer().getOnlinePlayers();
        String onlineNames = null;
        String nameFinal = null;
        for (Player o : online){
        	nameFinal = onlinePlayersFormat.replace("%name", o.getDisplayName());
        	/*if (player.hasPermission("mcdocsplus.player")){
	        		String group = Permission.getGroups(o.getWorld().getName(), o.getName());
	        		nameFinal = nameFinal.replace("%group", group);
	        		nameFinal = nameFinal.replace("%prefix", getDisplayName);
	        		nameFinal = nameFinal.replace("%suffix", getDisplayName);
        		}
        	else{
        			log.info("[MCDocs+] ERROR! One of the following is not found: %group %prefix %suffix for player " + o.getName());
        		}
        	}*/
        	nameFinal = colourSwap(nameFinal);
        	if (onlineNames == null){
        		onlineNames = nameFinal;
        	}
        	else{
        		onlineNames = onlineNames.trim() + "&f, " + nameFinal;
        	}
        }
        return onlineNames;
	}

	/*private String onlineGroup(String group){
		Player online[] = plugin.getServer().getOnlinePlayers();
        String onlineGroup = null;
        String nameFinal = null;
        for (Player o : online){
        	String oGroup = Permission.getGroups(o.getWorld().getName(), o.getName());
        	oGroup = oGroup.toLowerCase();
	    	if (oGroup.equals(group)){
	    		try{
	        		nameFinal = onlinePlayersFormat.replace("%name", o.getDisplayName());
	        		nameFinal = nameFinal.replace("%group", oGroup);
	        		nameFinal = nameFinal.replace("%prefix", getDisplayName);
	        		nameFinal = nameFinal.replace("%suffix", getDisplayName);
	            	nameFinal = colourSwap(nameFinal);
	    		}
	        	catch(Exception ex){
	    			log.info("[MCDocs+] ERROR! One of the following is not found: %group %prefix %suffix for player " + o.getName());
	    		}
	        	if (onlineGroup == null){
	        		onlineGroup = nameFinal;
	        	}
	        	else{
	        		onlineGroup = onlineGroup + "&f, " + nameFinal;
	        	}
	    	}
	    }
	    if (onlineGroup == null){
	    	onlineGroup = "";
	    }
	    return onlineGroup;
	}*/
	
	private String onlineCount(){
		Player online[] = plugin.getServer().getOnlinePlayers();
        int onlineCount = 0;
        for (@SuppressWarnings("unused") Player o : online){
        	onlineCount++;
        }
        return Integer.toString(onlineCount);
	}
	
	/*private String onlineMax(){
		int Player = plugin.getServer().getMaxPlayers();
        int onlineMax = 0;
        for (@SuppressWarnings("unused") Player o : online){
        	onlineMax++;
        }
        return Integer.toString(onlineMax);
	}*/
	
	private void newsLine(Player player){
		
		ArrayList<String> newsLinesList = new ArrayList<String>();
		File folder = plugin.getDataFolder();
		String folderName = folder.getParent();
		int current = 0;
		
		try {
            FileInputStream fis = new FileInputStream(folderName + "/MCDocsPlus/" + newsFile);
            Scanner scanner = new Scanner(fis, "UTF-8");
                while (current < newsLines) {
                	try{
                		newsLinesList.add(scanner.nextLine() + " ");
                	}
                	catch(Exception ex){
                		newsLinesList.add(" ");
                	}
                	current++;
                }
            scanner.close();
            fis.close();  
            variableSwap(player, newsLinesList);
		}	        		
		catch (Exception ex) {
    	 log.info("[MCDocs+] news-file was not found.");
		}
	}
		
    private boolean checkIfNumber(String in) {
        
        try {

            Integer.parseInt(in);
        
        } catch (NumberFormatException ex) {
            return false;
        }
        
        return true;
    }
	
    private String colourSwap(String line){
    	String[] Colours = { 	"&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7",
						        "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f",
						      };
    	ChatColor[] cCode = {	ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY,
						        ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE,
						      };
    	
    	for (int x = 0; x < Colours.length; x++) {
    		CharSequence cChk = null;
            String temp = null;

            cChk = Colours[x];
            if (line.contains(cChk)) {
                temp = line.replace(cChk, cCode[x].toString());
                line = temp;
            }
        }
        return line;
    }
    
    /*
	 * -- MOTD On Login -- 
	 * We try to find a group motd file, and if that fails, we try and find a normal motd file, and if that fails we give up.
	 */
	
    public void onPlayerJoin(PlayerJoinEvent event){
    	
    	if (motdEnabled == true){
    		Player player = event.getPlayer();
    		if (player.hasPermission("mcdocsplus.motd")){
    			
    			standardMotd(event);
    		}
    	}
    }

	/*public void groupMotd(PlayerJoinEvent event){
		ArrayList<String> lines = new ArrayList<String>();
		Player player = event.getPlayer();
		File folder = plugin.getDataFolder();
	   String folderName = folder.getParent();
		String group = Permission.getGroups(player.getWorld().getName(), player.getName());
		group = group.toLowerCase();
		lines.clear();
		fixedLines.clear();
		try {
	        FileInputStream fis = new FileInputStream(folderName + "/MCDocsPlus/motd-" + group + ".txt");
	        Scanner scanner = new Scanner(fis, "UTF-8");
	            while (scanner.hasNextLine()) {
	            	try{
	            		lines.add(scanner.nextLine() + " ");
	            	}
	            	catch(Exception ex){
	            		lines.add(" ");
	           	}
	           }
	       scanner.close();
	       fis.close();
	       
	       variableSwap(player, lines);
	        linesProcess(player, "/motd", 1);
	        }
		catch (Exception ex) {
			standardMotd(event);
	 	}
	}*/

	public void standardMotd(PlayerJoinEvent event){
		ArrayList<String> lines = new ArrayList<String>();
		Player player = event.getPlayer();
		File folder = plugin.getDataFolder();
	    String folderName = folder.getParent();
		lines.clear();
    	fixedLines.clear();	
    	
    	try{
    		FileInputStream fis = new FileInputStream(folderName + "/MCDocsPlus/motd.txt");
            Scanner scanner = new Scanner(fis, "UTF-8");
                while (scanner.hasNextLine()) {
                	try{
                		lines.add(scanner.nextLine() + " ");
                	}
                	catch(Exception ex1){
                		lines.add(" ");
                	}
                }
            scanner.close();
            fis.close();
          
            variableSwap(player, lines);
            linesProcess(player, "/motd", 1);
    	}
    	catch (Exception ex) {
     	}
				
	}

}
