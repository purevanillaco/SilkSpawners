package co.purevanilla.mcplugins.silkspawners.events;

import co.purevanilla.mcplugins.silkspawners.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Spawner implements Listener {

    @EventHandler
    public void onSpawnerPlace(final BlockPlaceEvent e){
        if(e.getBlock().getType()==Material.SPAWNER && e.getItemInHand().getType()==Material.SPAWNER){

            ItemMeta meta = e.getItemInHand().getItemMeta();
            List<String> lore = meta.getLore();
            EntityType spawnType = EntityType.PIG;

            if(lore!=null){
                for (String loreLine:lore) {
                    if(loreLine.startsWith("type:")){
                        spawnType = EntityType.valueOf(loreLine.split(":")[1]);
                    }
                }
            }
            final EntityType finalSpawnType = spawnType;
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
                @Override
                public void run() {
                    final Block block = e.getBlock().getLocation().getWorld().getBlockAt(e.getBlock().getLocation());
                    if(block.getType()==Material.SPAWNER){

                        CreatureSpawner cs = (CreatureSpawner) block.getState();
                        cs.setSpawnedType(finalSpawnType);
                        cs.update();

                        CreatureSpawner updateCs = (CreatureSpawner) block.getState();
                        if(updateCs.getSpawnedType()==finalSpawnType){
                            block.getWorld().spawnParticle(Particle.TOTEM,block.getLocation(),30);
                        } else {

                            block.getWorld().spawnParticle(Particle.CLOUD,block.getLocation(),30);
                            block.setType(Material.AIR);

                            ItemStack item = new ItemStack(Material.SPAWNER,1);
                            ItemMeta itemMeta = item.getItemMeta();
                            List<String> lore = new ArrayList<>();
                            lore.add("type:"+finalSpawnType.name());
                            itemMeta.setLore(lore);
                            item.setItemMeta(itemMeta);

                            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK,1,1);
                            e.getPlayer().sendMessage(ChatColor.RED+"Please, ask an admin to place this spawner for you");

                            block.getWorld().dropItemNaturally(block.getLocation(),item);
                        }

                    }
                }
            },1);

        }
    }

    @EventHandler
    public void onSpawnerBreak(final BlockBreakEvent e){

        final Block block = e.getBlock();
        final BlockState blockState = block.getState();

        if(block.getType()==Material.SPAWNER){
            boolean drop = false;
            try{

                ItemStack brokenWith = e.getPlayer().getInventory().getItemInMainHand();
                Map<Enchantment,Integer> enchantments = brokenWith.getEnchantments();
                if(enchantments.containsKey(Enchantment.SILK_TOUCH)){
                    if(e.getPlayer().hasPermission("silkspawners.silk")){
                        drop=true;
                    }
                } else {
                    throw new Exception("No need to check silk permission");
                }

            } catch (Exception ex) {
                if(e.getPlayer().hasPermission("silkspawners.nosilk")){
                    drop=true;
                }
            }

            if(drop){
                e.setExpToDrop(0);
            }

            // ensure that block has been broken
            final boolean finalDrop = drop;
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
                public void run() {
                    if(e.getBlock().getLocation().getWorld().getBlockAt(e.getBlock().getLocation()).getType()==Material.AIR){
                        if(finalDrop){

                            e.getBlock().getDrops().clear();

                            if(blockState instanceof CreatureSpawner){

                                CreatureSpawner cs = (CreatureSpawner) blockState;
                                ItemStack spawner = new ItemStack(Material.SPAWNER, 1);
                                BlockStateMeta blockMeta = (BlockStateMeta) spawner.getItemMeta();
                                blockMeta.setBlockState(cs);
                                List<String> lore = new ArrayList<>();
                                lore.add("type:"+cs.getSpawnedType().name());
                                blockMeta.setLore(lore);
                                spawner.setItemMeta(blockMeta);

                                e.getBlock().getLocation().getWorld().dropItemNaturally(e.getBlock().getLocation(),spawner);

                            } else {
                                System.out.println(block.getState().getType());
                            }

                        }
                    }
                }
            },1/2);

        }
    }

}
