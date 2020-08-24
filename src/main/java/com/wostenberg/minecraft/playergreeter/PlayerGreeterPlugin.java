package com.wostenberg.minecraft.playergreeter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.logging.Level;
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
    
    List<Pair<Pattern, BiFunction<Server,Player,String>>> messageVars = new ArrayList<>();

    static final String gmsgPath = "greetingMessage";
    static final String initialGmsgPath = "initialGreetingMessage";

    @Override
    public void onEnable() {
        this.loadConfig();
        this.saveConfig();
        this.initMessageVars();
        this.getServer().getPluginManager().registerEvents(this, this);
        var ex = new CommandReload(getLogger(), () -> loadConfig());
        this.getCommand("reload").setExecutor(ex);
    }

    private void initMessageVars() {
        List<Pair<String,BiFunction<Server,Player,String>>> baseVars = Arrays.asList(
            new Pair<String,BiFunction<Server,Player,String>>("player_name", ((server,player) -> player.getName())),
            new Pair<String,BiFunction<Server,Player,String>>("player_count", ((server,player) -> String.valueOf(server.getOnlinePlayers().size())))
        );
        for (var var : baseVars) {
            String pstr = "\\$\\{" + var.getKey() + "\\}";
            this.messageVars.add(new Pair<>(Pattern.compile(pstr), var.getValue()));
        }
    }
    
    public void loadConfig() {
        super.reloadConfig();
        config = this.getConfig();
        config.addDefault(initialGmsgPath, "§6Welcome, §3${player_name}§6!");
        config.addDefault(gmsgPath, "§6Welcome back, §3${player_name}§6!");
        config.options().copyDefaults(true);
    }

    public String substituteMessageVariables(Player player, String srcMessage) {
        String msg = srcMessage;
        for (var var : messageVars) {
            var repl = var.getValue().apply(getServer(), player);
            msg = var.getKey().matcher(msg).replaceAll(repl);    
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
