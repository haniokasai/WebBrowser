package itsu.mcbe.webbrowser.core;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;

public class Main extends PluginBase implements Listener {

    private Display display;

    @Override
    public void onEnable() {
        ConfigManager.init();

        getServer().getPluginManager().registerEvents(display = new Display(), this);

        getLogger().info("Enabled.");
        getLogger().info("Display width: " + ConfigManager.width);
        getLogger().info("Display height: " + ConfigManager.height);
        getLogger().info("Display frame rate: " + ConfigManager.framerate);
        getLogger().info("First load URL: " + ConfigManager.loadURL);
        getLogger().info("User-Agent: " + ConfigManager.userAgent);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        switch(command.getName()) {
            case "web": {
                if (args.length < 1) {
                    sender.sendMessage("Arguments count must be more than one.");
                    return true;
                }

                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage("Run on the game.");
                    return true;
                }

                Player player = (Player) sender;

                if (!player.isOp()) {
                    sender.sendMessage("You cannot run this command.");
                    return true;
                }

                switch (args[0]) {
                    case "load": {
                        display.load(args[0]);
                        return true;
                    }

                    case "create": {
                        player.sendMessage("Create display mode");

                        display.create(player);
                        return true;
                    }

                    case "break": {
                        player.sendMessage("Break display mode");
                        display.breakDisplay();
                        return true;
                    }
                }

            }
        }
        return false;
    }
}
