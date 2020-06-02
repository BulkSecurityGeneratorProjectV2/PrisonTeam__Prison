package tech.mcprison.prison.spigot.gui.rank;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.modules.Module;
import tech.mcprison.prison.modules.ModuleManager;
import tech.mcprison.prison.ranks.PrisonRanks;
import tech.mcprison.prison.ranks.data.RankLadder;
import tech.mcprison.prison.ranks.managers.LadderManager;
import tech.mcprison.prison.spigot.SpigotPrison;
import tech.mcprison.prison.spigot.gui.SpigotGUIComponents;

/**
 * @author GABRYCA
 */
public class SpigotLaddersGUI extends SpigotGUIComponents {

    private final Player p;

    public SpigotLaddersGUI(Player p){
        this.p = p;
    }

    public void open(){

        // Init the ItemStack
        ItemStack itemladder;

        // Check if Ranks are enabled
        if (!(checkRanks(p))){
            return;
        }

        // Init variable
        LadderManager lm = PrisonRanks.getInstance().getLadderManager();

        // Get the dimensions and if needed increases them
        int dimension = (int) Math.ceil(lm.getLadders().size() / 9D) * 9;

        // Load config
        Configuration GuiConfig = SpigotPrison.getGuiConfig();

        // If the inventory is empty
        if (dimension == 0){
            p.sendMessage(SpigotPrison.format(GuiConfig.getString("Gui.Message.EmptyGui")));
            if (p.getOpenInventory() != null){
                p.closeInventory();
                return;
            }
            return;
        }

        // If the dimension's too big, don't open the GUI
        if (dimension > 54){
            p.sendMessage(SpigotPrison.format(GuiConfig.getString("Gui.Message.TooManyLadders")));
            p.closeInventory();
            return;
        }

        // Create the inventory and set up the owner, dimensions or number of slots, and title
        Inventory inv = Bukkit.createInventory(null, dimension, SpigotPrison.format("&3RanksManager -> Ladders"));

        // Make for every ladder a button
        for (RankLadder ladder : lm.getLadders()){

            // Init the lore array with default values for ladders
            List<String> ladderslore = createLore(
                    GuiConfig.getString("Gui.Lore.ClickToOpen"),
                    GuiConfig.getString("Gui.Lore.ShiftAndRightClickToDelete"));

            // Create the button
            itemladder = createButton(Material.LADDER, 1, ladderslore, SpigotPrison.format("&3" + ladder.name));

            // Add the button to the inventory
            inv.addItem(itemladder);
        }

        // Open the inventory
        this.p.openInventory(inv);
    }

}
