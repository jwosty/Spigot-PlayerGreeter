package com.wostenberg.minecraft.playergreeter;

import java.util.Map;
import java.util.function.*;
import java.util.regex.Pattern;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerGreeterPlugin extends JavaPlugin implements Listener {
    FileConfiguration config;
    
    Map<Pattern, BiFunction<Server,Player,String>> substitutions = Map.of(
        (Pattern.compile("${player_name}")), ((server,player) -> player.getName()),
        (Pattern.compile("${player_count}")), ((server,player) -> String.valueOf(server.getOnlinePlayers().size()))
    );

    static final String gmsgPath = "greetingMessage";
    static final String initialGmsgPath = "initialGreetingMessage";

    @Override
    public void onEnable() {
        this.loadConfig();
        var ex = new CommandReload(getLogger(), () -> loadConfig());
        this.getCommand("reloadpg").setExecutor(ex);
        this.saveConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
    }
    
    public void loadConfig() {
        super.reloadConfig();
        config = this.getConfig();
        config.addDefault(initialGmsgPath, "§6Welcome, §3%player_name%§6!");
        config.addDefault(gmsgPath, "§6Welcome back, §3%player_name%§6!");
        config.options().copyDefaults(true);
    }

    public String substituteMessageVariables(Player player, String srcMessage) {
        String msg = srcMessage;
        for (var entry : substitutions.entrySet()) {
            var repl = entry.getValue().apply(getServer(), player);
            msg = entry.getKey().matcher(msg).replaceAll(repl);    
        }
        return msg;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Boolean hasPlayedBefore = player.hasPlayedBefore() || getServer().getOfflinePlayer(player.getUniqueId()).hasPlayedBefore();
        String gmsg = config.getString((hasPlayedBefore || !config.contains(initialGmsgPath)) ? gmsgPath : initialGmsgPath);
        gmsg = substituteMessageVariables(player, gmsg);
        player.sendMessage(gmsg);
    }
}
