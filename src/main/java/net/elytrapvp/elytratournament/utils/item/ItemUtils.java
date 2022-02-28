package net.elytrapvp.elytratournament.utils.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {
    public static String convertToInvisibleString(String s) {
        String hidden = "";
        for (char c : s.toCharArray()) hidden += ChatColor.COLOR_CHAR+""+c;
        return hidden;
    }

    public static void giveLobbyItems(Player player) {
        player.getInventory().clear();

        ItemStack settingsItem = new ItemBuilder(Material.REDSTONE_COMPARATOR)
                .setDisplayName("&a&lSettings")
                .build();
        player.getInventory().setItem(8, settingsItem);

        if(player.hasPermission("tournament.use")) {
            ItemStack createTournamentItem = new ItemBuilder(Material.NETHER_STAR)
                    .setDisplayName("&a&lCreate Tournament")
                    .build();
            player.getInventory().setItem(0, createTournamentItem);
        }
    }
}