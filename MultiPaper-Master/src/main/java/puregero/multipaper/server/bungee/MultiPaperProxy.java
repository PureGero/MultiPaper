package puregero.multipaper.server.bungee;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import puregero.multipaper.server.MultiPaperServer;
import puregero.multipaper.server.ServerConnection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiPaperProxy extends Plugin implements Listener {

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);

        File config = new File(getDataFolder(), "config.yml");

        if (!config.isFile()) {
            config.getParentFile().mkdirs();
            try {
                config.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);

            if (!configuration.contains("port")) {
                configuration.set("port", MultiPaperServer.DEFAULT_PORT);
            }

            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getDataFolder(), "config.yml"));

            try {
                new MultiPaperServer(configuration.getInt("port")).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if (event.getPlayer().getServer() == null || !isMultiPaperServer(event.getPlayer().getServer().getInfo().getName())) {
            if (isMultiPaperServer(event.getTarget().getName())) {
                // They are connecting to a multipaper server from a non-multipaper server

                List<ServerInfo> servers = new ArrayList<>(getProxy().getServers().values());
                Collections.shuffle(servers);

                // Send them to the multipaper server with the lowest tick time
                ServerInfo bestServer = null;
                long lowestTickTime = Long.MAX_VALUE;

                for (ServerInfo info : servers) {
                    ServerConnection connection = ServerConnection.getConnection(info.getName());
                    if (connection != null && ServerConnection.isAlive(info.getName()) && connection.getTimer().averageInMillis() < lowestTickTime) {
                        lowestTickTime = connection.getTimer().averageInMillis();
                        bestServer = info;
                    }
                }

                if (bestServer != null) {
                    event.setTarget(bestServer);
                }
            }
        }
    }
    
    @EventHandler
    public void onServerKick(ServerKickEvent event) {
        String kickReason = BaseComponent.toPlainText(event.getKickReasonComponent());
        if (kickReason.startsWith("sendto:")) {
            String sendTo = kickReason.substring("sendto:".length());
            getLogger().info(event.getKickReason() + " - Sending " + event.getPlayer().getName() + " to " + sendTo + " (" + getProxy().getServerInfo(sendTo) + ")");
            event.setCancelServer(getProxy().getServerInfo(sendTo));
            event.setCancelled(true);
        }
    }

    private boolean isMultiPaperServer(String name) {
        return ServerConnection.getConnection(name) != null;
    }

}
