package net.elytrapvp.elytratournament.players;

import net.elytrapvp.elytratournament.ElytraTournament;
import net.elytrapvp.elytratournament.event.kit.Kit;
import net.elytrapvp.elytratournament.utils.chat.ChatUtils;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores all duels data about a player.
 */
public class CustomPlayer {
    private final ElytraTournament plugin;
    private final UUID uuid;

    private final Map<String, Map<Integer, Integer>> kitEditor = new HashMap<>();

    // Data
    int tournamentsHosted = 0;
    int goldMedals = 0;
    int silverMedals = 0;
    int bronzeMedals = 0;

    // Settings
    private boolean showScoreboard;

    /**
     * Creates the CustomPlayer object.
     * @param plugin Plugin instance.
     * @param uuid UUID of player.
     */
    public CustomPlayer(ElytraTournament plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;

        // Run everything async to prevent lag.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                for(Kit kit : plugin.kitManager().getKits()) {
                    kitEditor.put(kit.getName(), new HashMap<>());
                }

                PreparedStatement statement = plugin.mySQL().getConnection().prepareStatement("SELECT * FROM tournament_statistics WHERE uuid = ?");
                statement.setString(1, uuid.toString());
                ResultSet results = statement.executeQuery();

                if(results.next()) {
                    tournamentsHosted = results.getInt(2);
                    goldMedals = results.getInt(3);
                    silverMedals = results.getInt(4);
                    bronzeMedals = results.getInt(5);
                }
                else {
                    PreparedStatement statement2 = plugin.mySQL().getConnection().prepareStatement("INSERT INTO tournament_statistics (uuid) VALUES (?)");
                    statement2.setString(1, uuid.toString());
                    statement2.executeUpdate();
                }

                PreparedStatement statement3 = plugin.mySQL().getConnection().prepareStatement("SELECT * FROM tournament_kit_editor WHERE uuid = ?");
                statement3.setString(1, uuid.toString());
                ResultSet results3 = statement3.executeQuery();

                while(results3.next()) {
                    kitEditor.get(results3.getString(2)).put(results3.getInt(3), results3.getInt(4));
                }

                PreparedStatement statement4 = plugin.mySQL().getConnection().prepareStatement("SELECT * FROM tournament_settings WHERE uuid = ?");
                statement4.setString(1, uuid.toString());
                ResultSet results4 = statement4.executeQuery();
                if(results4.next()) {
                    showScoreboard = results4.getBoolean(2);
                }
                else {
                    PreparedStatement statement5 = plugin.mySQL().getConnection().prepareStatement("INSERT INTO tournament_settings (uuid) VALUES (?)");
                    statement5.setString(1, uuid.toString());
                    statement5.executeUpdate();

                    showScoreboard = true;
                }
            }
            catch (SQLException exception) {
                ChatUtils.chat(Bukkit.getPlayer(uuid), "&cError &8» &cSomething went wrong loading your data! Please reconnect or your data could be lost.");
                exception.printStackTrace();
            }
        });
    }

    /**
     * Add 1 to the tournaments hosted counter.
     */
    public void addTournamentHosted() {
        setTournamentsHosted(tournamentsHosted + 1);
    }

    /**
     * Clear the kit editor of a kit.
     * @param kit Kit to clear.
     */
    private void cleanKitEditor(String kit) {
        try {
            PreparedStatement statement3 = plugin.mySQL().getConnection().prepareStatement("DELETE FROM tournament_kit_editor WHERE uuid = ? AND kit = ?");
            statement3.setString(1, uuid.toString());
            statement3.setString(2, kit);
            statement3.executeUpdate();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Get the number of bronze medals the player has.
     * @return Bronze medals count.
     */
    public int getBronzeMedals() {
        return bronzeMedals;
    }

    /**
     * Get the number of gold medlas the player has.
     * @return Gold medals count.
     */
    public  int getGoldMedals() {
        return goldMedals;
    }

    /**
     * Get the modified kit.
     * @param kit Kit to get modifications of.
     * @return Modifications
     */
    public Map<Integer, Integer> getKitEditor(String kit) {
        return kitEditor.get(kit);
    }

    /**
     * Get if the scoreboard should be shown.
     * @return if the scoreboard should be shown.
     */
    public boolean getShowScoreboard() {
        return showScoreboard;
    }

    /**
     * Get the current number of silver medals.
     * @return Silver medals count.
     */
    public int getSilverMedals() {
        return silverMedals;
    }

    /**
     * Get the number of tournaments hosted.
     * @return Tournaments hosted.
     */
    public int getTournamentsHosted() {
        return tournamentsHosted;
    }

    /**
     * Set if the scoreboard should be shown.
     * @param showScoreboard Whether or not the scoreboard should be shown.
     */
    public void setShowScoreboard(boolean showScoreboard) {
        this.showScoreboard = showScoreboard;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement statement = plugin.mySQL().getConnection().prepareStatement("UPDATE tournament_settings SET showScoreboard = ? WHERE uuid = ?");
                statement.setBoolean(1, showScoreboard);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
            }
            catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Set the amount of tournaments this person has hosted.
     * @param tournamentsHosted Number of tournaments hosted.
     */
    public void setTournamentsHosted(int tournamentsHosted) {
        this.tournamentsHosted = tournamentsHosted;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement statement = plugin.mySQL().getConnection().prepareStatement("UPDATE tournament_statistics SET hosted = ? WHERE uuid = ?");
                statement.setInt(1, tournamentsHosted);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
            }
            catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Update the kit editor.
     * @param kit Kit to update.
     */
    public void updateKitEditor(String kit) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            cleanKitEditor(kit);

            try {
                Map<Integer, Integer> map = getKitEditor(kit);

                for(int item : map.keySet()) {
                    int slot = map.get(item);

                    PreparedStatement statement = plugin.mySQL().getConnection().prepareStatement("INSERT INTO tournament_kit_editor (uuid,kit,item,slot) VALUES (?,?,?,?)");
                    statement.setString(1, uuid.toString());
                    statement.setString(2, kit);
                    statement.setInt(3, item);
                    statement.setInt(4, slot);
                    statement.executeUpdate();
                }
            }
            catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }
}