package itsu.mcbe.webbrowser.core;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private static Config config;
    private static File configFile;

    public static int width;
    public static int height;
    public static int framerate;
    public static String loadURL;
    public static String userAgent;

    public static void init() {
        config = new Config();
        configFile = new File("./plugins/WebBrowser.yml");

        if (!configFile.exists()) {
            try {
                Utils.writeFile(configFile, ConfigManager.class.getClassLoader().getResourceAsStream("WebBrowser.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config.load(configFile.getPath());

        width = config.getInt("display_width");
        height = config.getInt("display_height");
        framerate = config.getInt("display_framerate");
        loadURL = config.getString("first_load_url");
        userAgent = config.getString("user_agent");
    }
}
