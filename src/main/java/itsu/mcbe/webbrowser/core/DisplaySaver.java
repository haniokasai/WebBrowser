package itsu.mcbe.webbrowser.core;

import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisplaySaver {

    private static List<String> displayData = new ArrayList<>();

    public static void set(String world, double x, double y, double z) {
        displayData.add(world + ":" + x + ":" + y + ":" + z);
    }

    public static void save() {
        try {
            StringBuffer buf = new StringBuffer();
            for (String data : displayData) {
                buf.append("\n" + data);
            }

            Utils.writeFile("./plugins/WebBrowser_Display.dat", buf.toString().substring(1));

            displayData.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Position> getAll() {
        try {
            List<Position> result = new ArrayList<>();
            String data = Utils.readFile("./plugins/WebBrowser_Display.dat");
            String[] temp = data.split("\n");

            for (String s : temp) {
                String raw[] = s.split(":");
                result.add(new Position(Double.parseDouble(raw[1]), Double.parseDouble(raw[2]), Double.parseDouble(raw[3]), Server.getInstance().getLevelByName(raw[0])));
            }

            return result;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
