package itsu.mcbe.webbrowser.core;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.ItemFrameDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Position;
import cn.nukkit.scheduler.TaskHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Display implements Listener {

    private static final int WIDTH = ConfigManager.width;
    private static final int HEIGHT = ConfigManager.height;
    private static final int FRAMERATE = ConfigManager.framerate;

    private List<ItemMap> display;
    private WebView view;

    private TaskHandler task;

    private boolean isSet = true;
    private boolean started;
    private int count = 0;

    public Display() {
        display = new ArrayList<>();

        //for init JavaFx
        new JFXPanel();

        Platform.runLater(() -> {
            view = new WebView();
            view.getEngine().setUserAgent(ConfigManager.userAgent);
            view.setPrefSize(WIDTH * 128, HEIGHT * 128);

            Scene scene = new Scene(view);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("fx");
            stage.setResizable(false);
            stage.show();
        });

        List<Position> list = DisplaySaver.getAll();

        if (list.size() > 0 && list.size() == WIDTH * HEIGHT) {
            for (Position position : list) {
                BlockEntityItemFrame block;

                try {
                    block = (BlockEntityItemFrame) position.getLevel().getBlockEntity(position);
                } catch (NullPointerException | ClassCastException e) {
                    display.clear();
                    break;
                }

                ItemMap map = new ItemMap();
                map.setImage(new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB));

                for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                    map.sendImage(player);
                }

                block.setItem(map);
                block.setItemRotation(4);

                display.add(map);
            }

            if (display.size() == WIDTH * HEIGHT) {
                isSet = true;

                Server.getInstance().getLogger().info("Display set ok.");

                load(ConfigManager.loadURL);
                start();
            }
        }
    }

    public void create(Player player) {
        display.clear();
        if (task != null) task.cancel();

        count = 0;
        isSet = false;
        started = false;
    }

    public void load(String url) {
        Platform.runLater(() -> {
            view.getEngine().load(url);
            view.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State t1) {
                    if (t1 == Worker.State.SUCCEEDED) {
                        Server.getInstance().getLogger().info("Work end.");
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(view.snapshot(null, null), null);
                        setImage(bufferedImage);
                    }
                }
            });
        });
    }

    private void takeImage() {
        Platform.runLater(() -> {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(view.snapshot(null, null), null);
            setImage(bufferedImage);
        });
    }

    public void setImage(BufferedImage image) {
        List<BufferedImage> images = new ArrayList<>();
        int w = 0;
        int h = 0;

        int count = 0;
        for (ItemMap itemMap : display) {
            itemMap.setImage(image.getSubimage(w, h, 128, 128));

            for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                itemMap.sendImage(player);
            }

            count++;
            w += 128;

            if (count % WIDTH == 0) {
                w = 0;
                h += 128;
            }
        }
    }

    @EventHandler
    public void onTap(PlayerInteractEvent e) {
        if (e.getBlock().getId() == Block.ITEM_FRAME_BLOCK) {
            if (!isSet) {
                BlockEntityItemFrame block = (BlockEntityItemFrame) e.getPlayer().getLevel().getBlockEntity(e.getBlock());

                ItemMap map = new ItemMap();
                map.setImage(new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB));

                for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                    map.sendImage(player);
                }

                block.setItem(map);
                block.setItemRotation(3);

                display.add(map);
                DisplaySaver.set(block.getLevel().getName(), block.getX(), block.getY(), block.getZ());

                count++;

                e.getPlayer().sendMessage("Display: " + count);
            }

            if (started) {
                BlockEntityItemFrame block = (BlockEntityItemFrame) e.getPlayer().getLevel().getBlockEntity(e.getBlock());

                if (display.contains(block.getItem())) {
                    e.getPlayer().sendMessage("This is a display!");
                    e.setCancelled();
                }
            }
        }

        if (count == WIDTH * HEIGHT) {
            isSet = true;
            DisplaySaver.save();

            e.getPlayer().sendMessage("Display set ok.");
            load(ConfigManager.loadURL);
            start();
        }

    }

    @EventHandler
    public void onDrop(ItemFrameDropItemEvent e) {
        Item item = e.getItem();

        if (item instanceof ItemMap && display.contains(item)) {
            e.getPlayer().sendMessage("This is a display!");
            e.setCancelled();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        display.forEach(itemMap -> itemMap.sendImage(e.getPlayer()));
    }

    private void start() {
        if (!started) {
            started = true;

            task = Server.getInstance().getScheduler().scheduleRepeatingTask(new Runnable() {
                @Override
                public void run() {
                    takeImage();
                }
            }, FRAMERATE);

        }
    }

    public void breakDisplay() {
        for (ItemMap map : display) {
            map.setImage(new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB));

            for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                map.sendImage(player);
            }
        }

        display.clear();
    }

}
