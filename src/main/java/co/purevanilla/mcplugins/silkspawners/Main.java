package co.purevanilla.mcplugins.silkspawners;

import co.purevanilla.mcplugins.silkspawners.events.Spawner;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public static Plugin plugin;

    @Override
    public void onEnable() {
        super.onEnable();
        plugin=this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new Spawner(), this);
    }
}
