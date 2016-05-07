package me.hitoyu.hitocore;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class hitocore extends JavaPlugin {
	private String mcversion = null;
	private String mcversionDec = null;
	Map<String, String> dirs = new HashMap<String, String>();
	static Map<String, String> hooks = new HashMap<String, String>();

	public void onEnable() {
		// Determine version of server
		String tVersion = Bukkit.getVersion();
		String[] mc_version = tVersion.split("\\)");
		mc_version = mc_version[0].split("MC: ");
		mcversionDec = mc_version[1];
		mcversion = mc_version[1].replace(".", "");
		
		getLogger().info("Running on Minecraft " + getServerVersion(true));
		getLogger().info("HitoCore API library enabled.");
	}

	public void onDisable() {
		getLogger().info("HitoCore API library disabled.");
	}
	
	void mapNewDir(String input, String dir) {
		dirs.put(input, dir);
	}
	
	String checkForDir(String input) {
		String output = null;
		if(dirs.containsKey(input)) {
			output = (String)dirs.get(input);
		}
		
		if(output == null) {
			File pluginsDir = new File("plugins/");
			for(File file : pluginsDir.listFiles()) {
				if(file.isDirectory()) {
					String dirTest = file.getName();
					if(dirTest.equalsIgnoreCase(input)) {
						output = dirTest;
						mapNewDir(input, output);
					}
				}
			}
		}
		
		if(output == null) {
			output = input;
		}
		return output;
	}
	
	static String getClassName() {
	    StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
	    String classPath = stacktrace[4].getClassName();
	    String className = classPath.substring(classPath.lastIndexOf(".")+1, classPath.length());
	    return className;
	}
	
	static String getMethodName()	{
	    StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
	    StackTraceElement e = stacktrace[4];
	    String methodName = e.getMethodName();
	    return methodName;
	}

	static int getLineNumber()	{
	    StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
	    int e = stacktrace[4].getLineNumber();
	    return e;
	}
	
	@SuppressWarnings("deprecation")
	static String getPluginName() {
		StackTraceElement st = Thread.currentThread().getStackTrace()[3];

		Class<?> kl = null;
		try {
			kl = Class.forName(st.getClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        URL location = kl.getResource('/' + kl.getName().replace(".", "/") + ".class");
        String jarPath = location.getPath();
        String jar = jarPath.substring("file:".length(), jarPath.lastIndexOf("!"));

		String pluginName = "";
	
		JarFile jarF = null;
		try {
			jarF = new JarFile(jar);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JarEntry et = jarF.getJarEntry("plugin.yml");
		
		FileConfiguration yml = null;
		try {
			yml = YamlConfiguration.loadConfiguration(jarF.getInputStream(et)); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		pluginName = yml.getString("name");
		try {
			jarF.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return pluginName;
	}
	
	static String getLocationOfDebugCall() {
		String dcl = "";
		dcl = getMethodName() + "(" + getClassName() + ":" + getLineNumber() + ")";

		return dcl;
	}

	/**
	 * Prints a debug message to console which can contain some essential
	 * variables for finding errors, however only if the debug mode for the calling plugin is enabled.
	 * 
	 * @param pluginName String
	 * @param className String
	 * @param method String
	 * @param action String
	 * @param message T (variable type)
	 * @param advanced Boolean
	 */	
	@SuppressWarnings("unchecked")
	public <T> void debugCall(String action, T message, boolean advanced) {
		String pluginName = getPluginName();
		
		// CASE SENSITIVE ON LINUX
		FileConfiguration data = YamlConfiguration.loadConfiguration(new File("plugins/" + pluginName, "config.yml"));
		String debug = data.getString("debug");
		
		if(pluginName.equalsIgnoreCase("HitoCore")) {
			debug = "2";
		}
		
		if(debug == null) {
			return;
		}

		int debugLevel = Integer.parseInt(debug);
		if (debugLevel < 1 || (advanced == true && debugLevel < 2)) {
			return;
		}

		String debugPrefix = "[" + pluginName + "] ";
		if (advanced == true) {
			debugPrefix = debugPrefix + "Adv";
		}
		debugPrefix = debugPrefix + "Debug | ";
		
		if (message == null) {
			message = (T) "<N/A>".toString();
		}

		Bukkit.getLogger().info(debugPrefix + "At: " + getLocationOfDebugCall() + " | Action: " + action + " | Message: " + message);
	}

	/**Returns the server version, either as a single unbroken number of as is displayed on a client. 172 or 1.7.2.
	 * 
	 * @param
	 * */
	public String getServerVersion(boolean decimal) {
		if (decimal == true) {
			return mcversionDec;
		} else {
			return mcversion;
		}
	}

	public int returnDebugValue(String pluginName) {
		FileConfiguration data = YamlConfiguration.loadConfiguration(new File("plugins/" + pluginName, "config.yml"));
		return Integer.parseInt(data.getString("debug"));
	}
	/**Returns true if the server is using UUIDs.
	 * 
	 * */
	public boolean usingUUIDs() {
		if (Integer.parseInt(getServerVersion(false)) > 169) {
			return true;
		}
		return false;
	}

	/**
	 * Returns either the UUID or the current username of a player, depending on
	 * the version of Minecraft that the server is running.
	 * 
	 * @param name (String) Player name
	 */
	@SuppressWarnings("deprecation")
	public String returnName(String name) {
		// gets player name
		Player player = Bukkit.getPlayer(name);

		// If null, try get based on UUID
		if (player == null) {
			try {
				player = Bukkit.getPlayer(UUID.fromString(name));
			} catch (Exception ignore) {
			}

			if (player == null) {
				return null;
			}
		}

		if (usingUUIDs() == true) {
			return player.getUniqueId().toString();
		}
		return player.getName();
	}

	/*public void saveDefaultConfig(String pluginName) {
        if (!new File("plugins/" + checkForDir(pluginName), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
    }*/
	
	public int getNumberOnline() {
		int x = 0;
		for (@SuppressWarnings("unused")
		Player p : Bukkit.getOnlinePlayers()) {
			x++;
		}
		return x;
	}
}
