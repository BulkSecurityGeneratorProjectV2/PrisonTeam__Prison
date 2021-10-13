package tech.mcprison.prison.spigot.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.cryptomorin.xseries.XMaterial;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.autofeatures.AutoFeaturesFileConfig;
import tech.mcprison.prison.autofeatures.AutoFeaturesFileConfig.AutoFeatures;
import tech.mcprison.prison.autofeatures.AutoFeaturesWrapper;
import tech.mcprison.prison.mines.PrisonMines;
import tech.mcprison.prison.mines.data.Mine;
import tech.mcprison.prison.modules.Module;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.output.Output.DebugTarget;
import tech.mcprison.prison.ranks.PrisonRanks;
import tech.mcprison.prison.ranks.data.PlayerRank;
import tech.mcprison.prison.ranks.data.Rank;
import tech.mcprison.prison.ranks.data.RankLadder;
import tech.mcprison.prison.ranks.data.RankPlayer;
import tech.mcprison.prison.spigot.SpigotPrison;
import tech.mcprison.prison.spigot.SpigotUtil;
import tech.mcprison.prison.spigot.backpacks.BackpacksUtil;
import tech.mcprison.prison.spigot.compat.Compatibility;
import tech.mcprison.prison.spigot.compat.SpigotCompatibility;
import tech.mcprison.prison.spigot.configs.MessagesConfig;
import tech.mcprison.prison.spigot.game.SpigotPlayer;
import tech.mcprison.prison.spigot.gui.autofeatures.SpigotAutoBlockGUI;
import tech.mcprison.prison.spigot.gui.autofeatures.SpigotAutoFeaturesGUI;
import tech.mcprison.prison.spigot.gui.autofeatures.SpigotAutoPickupGUI;
import tech.mcprison.prison.spigot.gui.autofeatures.SpigotAutoSmeltGUI;
import tech.mcprison.prison.spigot.gui.backpacks.BackpacksAdminGUI;
import tech.mcprison.prison.spigot.gui.backpacks.BackpacksAdminListGUI;
import tech.mcprison.prison.spigot.gui.backpacks.BackpacksAdminPlayerListGUI;
import tech.mcprison.prison.spigot.gui.guiutility.SpigotGUIComponents;
import tech.mcprison.prison.spigot.gui.mine.SpigotBlocksListGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotBlocksMineListGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMineBlockPercentageGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMineInfoGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMineNotificationRadiusGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMineNotificationsGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMineResetTimeGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMinesBlocksGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMinesConfirmGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMinesGUI;
import tech.mcprison.prison.spigot.gui.rank.SpigotLaddersGUI;
import tech.mcprison.prison.spigot.gui.rank.SpigotRankManagerGUI;
import tech.mcprison.prison.spigot.gui.rank.SpigotRankPriceGUI;
import tech.mcprison.prison.spigot.gui.rank.SpigotRankUPCommandsGUI;
import tech.mcprison.prison.spigot.gui.rank.SpigotRanksGUI;
import tech.mcprison.prison.spigot.gui.sellall.SellAllAdminAutoSellGUI;
import tech.mcprison.prison.spigot.gui.sellall.SellAllAdminBlocksGUI;
import tech.mcprison.prison.spigot.gui.sellall.SellAllAdminGUI;
import tech.mcprison.prison.spigot.gui.sellall.SellAllDelayGUI;
import tech.mcprison.prison.spigot.gui.sellall.SellAllPlayerGUI;
import tech.mcprison.prison.spigot.gui.sellall.SellAllPrestigesMultiplierGUI;
import tech.mcprison.prison.spigot.gui.sellall.SellAllPrestigesSetMultiplierGUI;
import tech.mcprison.prison.spigot.gui.sellall.SellAllPriceGUI;
import tech.mcprison.prison.spigot.sellall.SellAllUtil;

/**
 * @author GABRYCA
 * @author RoyalBlueRanger
 */
public class ListenersPrisonManager implements Listener {

    private static ListenersPrisonManager instance;
    public static List<Player> activeGui = new ArrayList<>();
    public static List<String> chatEventPlayer = new ArrayList<>();
    public boolean isChatEventActive = false;
    private int id;
    private String tempChatVariable;
    private final Configuration config = SpigotPrison.getInstance().getConfig();
    private final Configuration guiConfig = SpigotPrison.getInstance().getGuiConfig();
    private final MessagesConfig messages = SpigotPrison.getInstance().getMessagesConfig();
    boolean guiNotEnabled = !getBoolean(config.getString("prison-gui-enabled"));
    private Optional<RankLadder> ladder;
    public ChatMode mode;

    public enum ChatMode{
        RankName,
        MineName,
        Prestige,
        SellAll_Currency
    }

    public ListenersPrisonManager(){}

    /**
     * Get Listeners instance.
     * */
    public static ListenersPrisonManager get() {
        if (instance == null) {
            instance = new ListenersPrisonManager();
        }
        return instance;
    }

    public void chatEventActivator(){
        isChatEventActive = true;
    }

    public void removeMode(){
        mode = null;
    }

    /**
     * Add a player to the ChatEventPlayer arrayList.
     * */
    public void addChatEventPlayer(Player p){

        if (!isChatEventActive){
            return;
        }

        if (!chatEventPlayer.contains(p.getName())){
            chatEventPlayer.add(p.getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignEditing(SignChangeEvent e){

        if (!SpigotPrison.getInstance().isSellAllEnabled()){
            return;
        }

        SellAllUtil sellAllUtil = SpigotPrison.getInstance().getSellAllUtil();

        // Check if the feature's enabled
        if (!sellAllUtil.isSellAllSignEnabled){
            return;
        }

        Player p = e.getPlayer();
        String signTag = sellAllUtil.sellAllSignTag;;
        if (signTag == null){
            signTag = "&7[&3SellAll&7]";
        }

        try {
            if (e.getLine(0) == null){
                return;
            }
            if (e.getLine(0).equalsIgnoreCase("[SellAll]") || e.getLine(0).equalsIgnoreCase(signTag)) {
                if (p.hasPermission("prison.sign")){
                    e.setLine(0, SpigotPrison.format(signTag));
                } else {
                    e.setLine(0, SpigotPrison.format("&cNo perm"));
                    e.setLine(1, SpigotPrison.format("prison.sign"));
                }
            }
        } catch (IndexOutOfBoundsException ignored){}
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEvent(PlayerInteractEvent e){

        if (!SpigotPrison.getInstance().isSellAllEnabled()){
            return;
        }

        SellAllUtil sellAllUtil = SpigotPrison.getInstance().getSellAllUtil();

        if (sellAllUtil != null) {
            // Check if SellAll Shift + Right Click is enabled.
            if (sellAllUtil.isSellAllItemTriggerEnabled) {
                // Check if the action if Shift + Right Click.
                Player p = e.getPlayer();
                if (e.getAction().equals(Action.RIGHT_CLICK_AIR) && p.isSneaking()) {

                    // Check if a permission's required.
                    if (sellAllUtil.isSellAllItemTriggerPermissionEnabled) {
                        String permission = sellAllUtil.permissionItemTrigger;
                        if (permission != null && !p.hasPermission(permission)) {
                            return;
                        }
                    }

                    // Get the Items config section
                    ArrayList<XMaterial> items = sellAllUtil.getItemTriggerXMaterials();

                    if (items != null && items.size() != 0) {
                        XMaterial inHandXMaterial = null;
                        if (e.getItem() != null) {
                            inHandXMaterial = SpigotUtil.getXMaterial(e.getItem().getType());
                        }
                        if (inHandXMaterial != null) {
                            for (XMaterial xMaterialConf : items) {
                                if (xMaterialConf == inHandXMaterial) {
                                    sellAllUtil.sellAllSell(p, false, false, true, false, false, true);
                                    return;
                                } else if (xMaterialConf == SpigotUtil.getXMaterial(p.getInventory().getItemInMainHand().getType())) {
                                    sellAllUtil.sellAllSell(p, false, false, true, false, false, true);
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            // Check if the feature's enabled.
            if (sellAllUtil.isSellAllSignEnabled) {

                // Get clicked block.
                Material clickedBlock = null;
                if (e.getClickedBlock() != null) {
                    clickedBlock = e.getClickedBlock().getType();
                }

                // Check if the clicked block's a sign
                if (clickedBlock == Material.SIGN || clickedBlock == Material.WALL_SIGN) {

                    // Get the player
                    Player p = e.getPlayer();
                    String signTag = sellAllUtil.sellAllSignTag;
                    if (signTag == null) {
                        signTag = "&7[&3SellAll&7]";
                    }

                    // Get the action
                    Action action = e.getAction();

                    // Check if the action's a click
                    if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {

                        // Get sign
                        Sign sign = (Sign) e.getClickedBlock().getState();

                        // If there aren't lines in the sign this will void an error
                        try {

                            // Check if the first like of the sign have the right tag
                            if (sign.getLine(0).equalsIgnoreCase(SpigotPrison.format(signTag))) {
                                String permissionUseSign = sellAllUtil.permissionUseSign;
                                if (sellAllUtil.isSellAllSignPermissionToUseEnabled && !p.hasPermission(permissionUseSign)) {
                                    Output.get().sendWarn(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_missing_permission) + " [&3" + permissionUseSign + "&7]");
                                    return;
                                }

                                if (sellAllUtil.isSellAllSignNotifyEnabled) {
                                    Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_sellall_sell_sign_notify));
                                }

                                sellAllUtil.sellAllSell(p, true, false, true, false, false, true);
                            }
                        } catch (IndexOutOfBoundsException ignored) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove Player from the chatEventPlayer arrayList.
     * */
    public void removeChatEventPlayer(Player p){
        chatEventPlayer.remove(p.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    public void onGuiClosing(InventoryCloseEvent e){

        // If the GUI's disabled then return
        if (guiNotEnabled){
            return;
        }

        // Get the player and remove him from the list
        Player p = (Player) e.getPlayer();

        activeGui.remove(p);
    }

    /**
     * Add a Player to the GuiBlocker, so he won't be able to pickup items or do things in that Inventory.
     * */
    public void addToGUIBlocker(Player p){

        // If the GUI's disabled then return
        if (guiNotEnabled){
            return;
        }

        // If the player isn't already added to the list, then add him
        if(!activeGui.contains(p)) {
            activeGui.add(p);
        }
    }

    // On chat event to rename the a Rank Tag
    @EventHandler (priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {

        // Check if the boolean is true, this's set manually
        if (isChatEventActive) {

            // Get the player
            Player p = e.getPlayer();
            // Check if the player's in the list to not use another one for mistake/conflicting
            if (chatEventPlayer.contains(p.getName())){
                chatActions(e, p);
            }
        }
    }

    private void chatActions(AsyncPlayerChatEvent e, Player p) {
        // Get the chat message
        String message = e.getMessage();
        // Cancel the task, this has been added before manually
        Bukkit.getScheduler().cancelTask(id);
        modeAction(e, p, message);
        removeChatEventPlayer(p);
        removeMode();
    }

    /**
     * Enable chatEvent to do things with chat, requires ChatMode to recognise the active mode and action to use, and
     * the Player.
     * */
    public void chatInteractData(Player p, ChatMode activeMode) {
        isChatEventActive = true;
        mode = activeMode;
        addChatEventPlayer(p);
        id = Bukkit.getScheduler().scheduleSyncDelayedTask(SpigotPrison.getInstance(), () -> {
            if (isChatEventActive) {
                removeChatEventPlayer(p);
                Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_chat_event_time_end));
                isChatEventActive = false;
            }
            mode = null;
        }, 20L * 30);
    }

    /**
     * Java getBoolean's broken so I made my own.
     * */
    public boolean getBoolean(String string){
        return string != null && string.equalsIgnoreCase("true");
    }

    // Cancel the events of the active GUI opened from the player.
    private void activeGuiEventCanceller(Player p, InventoryClickEvent e){
        if(activeGui.contains(p)) {
            e.setCancelled(true);
        }
    }

    // InventoryClickEvent.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e){

        // Check if GUIs are enabled.
        boolean prisonGuiEnabled = getBoolean(SpigotPrison.getInstance().getConfig().getString("prison-gui-enabled"));
        if (!prisonGuiEnabled){
            return;
        }

        // Get the player.
        Player p = (Player) e.getWhoClicked();

        if (activeGui.contains(p)) {

            BackpacksUtil bUtil = BackpacksUtil.get();
            if (bUtil != null && bUtil.getEditedBackpack().contains(p)){
                return;
            }

            // GUIs must have the good conditions to work.
            if (guiConditions(e, p)) return;

            Compatibility compat = SpigotCompatibility.getInstance();

            String buttonNameMain;
            String[] parts;
            Module module;
            String title;

            try {
                // Get parameters.
                buttonNameMain = SpigotPrison.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                parts = buttonNameMain.split(" ");
                module = Prison.get().getModuleManager().getModule(PrisonRanks.MODULE_NAME).orElse(null);
                title = compat.getGUITitle(e).substring(2);
            } catch (ArrayIndexOutOfBoundsException ex){
                Output.get().sendWarn(new SpigotPlayer(p), "An error occurred while using the GUI, please check logs.");
                ex.printStackTrace();
                return;
            }

            // Close GUI button globally.
            if (buttonNameMain.equalsIgnoreCase("Close")) {
                Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_gui_close_success));
                p.closeInventory();
                return;
            }

            String playerRanksTitle = guiConfig.getString("Options.Titles.PlayerRanksGUI").substring(2);
            String playerPrestigeTitle = guiConfig.getString("Options.Titles.PlayerPrestigesGUI").substring(2);
            String minesPlayerTitle = guiConfig.getString("Options.Titles.PlayerMinesGUI").substring(2);

            // Check if the GUI have the right title and do the actions.
            switch (title) {

                // Check the title and do the actions.
                case "PrisonManager":

                    // Call the method.
                    prisonManagerGUI(e, p, buttonNameMain);

                    break;

                // Check the title.
                case "RanksManager -> Ladders": {

                    // Call the method.
                    laddersGUI(e, p, buttonNameMain, module, parts);

                    break;
                }

                // Check the title of the inventory and do the actions.
                case "Ladders -> Ranks": {

                    // Call the method.
                    ranksGUI(e, p, buttonNameMain, parts);

                    break;
                }
                // Check the title and do the actions.
                case "Prestige -> Confirmation": {

                    // Call the method.
                    prestigeConfirmationGUI(e, p, buttonNameMain);

                    break;
                }
                // Check the title of the inventory and do things.
                case "Ranks -> RankManager": {

                    // Call the method.
                    rankManagerGUI(e, p, parts);

                    break;
                }
                // Check the title and do the actions.
                case "RankManager -> RankUPCommands": {

                    // Call the method.
                    rankUPCommandsGUI(e, p, buttonNameMain);

                    break;
                }
                // Check the inventory name and do the actions.
                case "RankManager -> RankPrice": {

                    // Call the method.
                    rankPriceGUI(e, p, parts);

                    break;
                }
                // Check the title and do the actions.
                case "MinesManager -> Mines": {

                    // Call the method.
                    minesGUI(e, p, buttonNameMain, parts);

                    break;
                }
                case "Mines -> MineInfo": {

                    // Call the method.
                    mineInfoGUI(e, p, parts);

                    break;
                }

                // Check the title of the inventory and do the actions.
                case "Mines -> Delete": {

                    // Call the method.
                    minesDeleteGUI(p, parts);

                    break;
                }

                // Check the title of the inventory and do the actions.
                case "MineInfo -> Blocks": {

                    // Call the method.
                    blocksGUI(e, p, parts);

                    break;
                }

                // Check the inventory name and do the actions.
                case "Mines -> BlocksList": {

                    blocksListGUI(e, p, parts);

                    break;
                }

                // Check the inventory name and do the actions.
                case "MineInfo -> ResetTime": {

                    // Call the method.
                    resetTimeGUI(e, p, parts);

                    break;
                }

                // Check the inventory title and do the actions.
                case "MineInfo -> MineNotifications": {

                    // Call the method.
                    mineNotificationsGUI(e, p, parts);

                    break;
                }

                case "MineInfo -> BlockPercentage": {

                    mineBlockPercentage(e, p, parts);

                    break;
                }

                // Check the inventory title and do the actions.
                case "MineNotifications -> Radius": {

                    // Call the method
                    radiusGUI(e, p, parts);

                    break;
                }
                // Check the inventory title and do the actions.
                case "PrisonManager -> AutoFeatures": {

                    // Call the method
                    autoFeaturesGUI(e, p, parts);

                    break;
                }

                // Check the title and do the actions.
                case "AutoFeatures -> AutoPickup": {

                    // Call the method
                    autoPickupGUI(e, p, parts);

                    break;
                }

                // Check the title and do the actions.
                case "AutoFeatures -> AutoSmelt": {

                    // Call the method
                    autoSmeltGUI(e, p, parts);

                    break;
                }

                // Check the title and do the actions.
                case "AutoFeatures -> AutoBlock": {

                    // Call the method
                    autoBlockGUI(e, p, parts);

                    break;
                }

                // Check the title and do the actions.
                case "SellAll -> Blocks": {

                    sellAllAdminBlocksGUI(e, p, parts);

                    break;
                }

                // Check the title and do the actions.
                case "Prison -> SellAll-Admin": {

                    sellAllAdminGUI(e, p, buttonNameMain);

                    break;
                }

                // Check the title and do the actions.
                case "SellAll -> AutoSell": {

                    sellAllAutoSellAdminGUI(e, p, buttonNameMain);

                    break;
                }

                // Check the title and do the actions.
                case "SellAll -> ItemValue": {

                    sellAllItemValue(e, p, parts);

                    break;
                }

                // Check the title and do the actions.
                case "SellAll -> Delay": {

                    sellAllDelayGUI(e, p, parts);

                    break;
                }

                // Check the title and do the actions.
                case "SellAll -> Multipliers": {

                    sellAllMultipliersGUI(e, p, buttonNameMain, parts);

                    break;
                }

                case "Edit -> Multiplier": {

                    setSellAllPrestigeMultiplier(e, p, parts);

                    break;
                }

                case "Select -> ShowBlock":{

                    showBlock(e, p, parts);

                    break;
                }

                // Check the title and do the actions.
                case "Prison -> SellAll-Player": {

                    sellAllPlayerGUI(e, p, parts);

                    break;
                }
                // Check the title and do the actions.
                case "Prison Setup -> Confirmation": {

                    prisonSetupConfirmGUI(e, p, parts);

                    break;
                }
                // Check the title and do the actions.
                case "Backpacks-Admin":{

                    backpacksAdmin(e, p, buttonNameMain);

                    break;
                }
                // Check the title and do the actions.
                case "Backpacks-Admin-Players":{

                    BackpacksAdminListGUI gui = new BackpacksAdminListGUI(p, parts[1]);
                    gui.open();

                    break;
                }
                // Check the title and do the actions.
                case "Backpacks-Admin-List":{

                    if (parts[0].equalsIgnoreCase("Backpack")){
                        if (e.isRightClick() && e.isShiftClick()){
                            if (parts[2].equalsIgnoreCase("default")){
                                Bukkit.dispatchCommand(p, "backpack delete " + parts[1]);
                            } else {
                                Bukkit.dispatchCommand(p, "backpack delete " + parts[1] + " " + parts[2]);
                            }
                            p.closeInventory();
                            BackpacksAdminListGUI gui = new BackpacksAdminListGUI(p, parts[1]);
                            gui.open();
                        }
                    }

                    break;
                }
                default:{

                    break;
                }
            }

            // Customizable title GUIs.
            if (title.equalsIgnoreCase(playerRanksTitle)){

                // Call the method.
                playerRanksGUI(e, p, buttonNameMain);
            } else if (title.equalsIgnoreCase(playerPrestigeTitle)){

                // Call the method.
                playerPrestigesGUI(e, p, buttonNameMain);
            } else if (title.equalsIgnoreCase(minesPlayerTitle)){

                // Call the method
                playerMinesGUI(p, e);
            } else if (title.equalsIgnoreCase(p.getName() + " -> Backpacks")){

                backpacksList(p, buttonNameMain, parts);
            }
        }
    }

    private void sellAllPlayerGUI(InventoryClickEvent e, Player p, String[] parts) {
        if (parts[0].equalsIgnoreCase("Prior")){

            SellAllPlayerGUI gui = new SellAllPlayerGUI(p, Integer.parseInt(parts[1]));
            gui.open();

            e.setCancelled(true);
            return;
        } else if (parts[0].equalsIgnoreCase("Next")){

            SellAllPlayerGUI gui = new SellAllPlayerGUI(p, Integer.parseInt(parts[1]));
            gui.open();

            e.setCancelled(true);
            return;
        }

        p.closeInventory();
        e.setCancelled(true);
    }

    private void backpacksAdmin(InventoryClickEvent e, Player p, String buttonNameMain) {
        if(buttonNameMain.equalsIgnoreCase("Backpacks-List")){

            BackpacksAdminPlayerListGUI gui = new BackpacksAdminPlayerListGUI(p);
            gui.open();

        } else if(buttonNameMain.equalsIgnoreCase("Backpack-Settings")){
            p.closeInventory();
            Output.get().sendInfo(new SpigotPlayer(p), "&6Coming Soon...");
        }

        e.setCancelled(true);
    }

    private void backpacksList(Player p, String buttonNameMain, String[] parts) {

        BackpacksUtil bUtil = BackpacksUtil.get();

        if (bUtil == null){
            Output.get().sendWarn(new SpigotPlayer(p), "Can't get Backpacks Util, maybe they're disabled?");
            p.closeInventory();
            return;
        }

        if (parts[0].equalsIgnoreCase("New")){

            // Create Backpack method can avoid this.
            boolean success = bUtil.createBackpack(p);

            if (success) {
                Bukkit.dispatchCommand(p, "gui backpack " + bUtil.getLatestBackpackID(p));
            }
        } else {
            bUtil.openBackpack(p, Integer.parseInt(buttonNameMain.substring(9)));
        }
    }

    private void showBlock(InventoryClickEvent e, Player p, String[] parts) {
        String positionStr = ( parts.length > 2 ? parts[2] : "0" );
        int position = 0;
        try {
            position = Integer.parseInt( positionStr );
        }
        catch(NumberFormatException ignored) {}

        if (parts[0].equalsIgnoreCase("Next") || parts[0].equalsIgnoreCase("Prior")){

            SpigotBlocksMineListGUI gui = new SpigotBlocksMineListGUI(p, parts[1], position);
            p.closeInventory();
            gui.open();

        } else {

            try {
                File sellAllFile = new File(SpigotPrison.getInstance().getDataFolder() + "/GuiConfig.yml");
                FileConfiguration conf = YamlConfiguration.loadConfiguration(sellAllFile);
                conf.set("Options.Mines.MaterialType." + parts[1], parts[0]);
                conf.save(sellAllFile);
            } catch (IOException ex){
                ex.printStackTrace();
                return;
            }

            Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_mines_item_show_edit_success) + " [" + parts[0] + "]");
            p.closeInventory();
        }

        e.setCancelled(true);
    }

    private void setSellAllPrestigeMultiplier(InventoryClickEvent e, Player p, String[] parts) {

        // Rename the parts
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        // Initialize the variable
        double decreaseOrIncreaseValue = 0;

        // If there're enough parts init another variable
        if (parts.length == 4){
            decreaseOrIncreaseValue = Double.parseDouble(parts[3]);
        }

        // Check the button name and do the actions
        if (part1.equalsIgnoreCase("Confirm:")) {

            // Check the click type and do the actions
            if (e.isLeftClick()){

                // Execute the command
            	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall multiplier add" );
                Bukkit.dispatchCommand(p, registeredCmd + " " + part2 + " " + part3);

                // Close the inventory
                p.closeInventory();

                return;

                // Check the click type and do the actions
            } else if (e.isRightClick()){

                // Send a message to the player
                Output.get().sendWarn(new SpigotPlayer(p), "&cEvent cancelled.");

                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            } else {

                // Cancel the event
                e.setCancelled(true);
                return;
            }
        }

        // Give to val a value
        double val = Double.parseDouble(part2);

        // Check the calculator symbol
        if (part3.equals("-")){

            // Check if the value's already too low
            if (!((val -  decreaseOrIncreaseValue) < 0)) {

                // If it isn't too low then decrease it
                val = val - decreaseOrIncreaseValue;

                // If it is too low
            } else {

                // Tell to the player that the value's too low
                Output.get().sendWarn(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_gui_too_low_value));

                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();
                return;
            }

            // Open an updated GUI after the value changed
            SellAllPrestigesSetMultiplierGUI gui = new SellAllPrestigesSetMultiplierGUI(p, val, part1);
            gui.open();

            // Check the calculator symbol
        } else if (part3.equals("+")){

            // Check if the value isn't too high
            if (!((val + decreaseOrIncreaseValue) > 2147483646)) {

                // Increase the value
                val = val + decreaseOrIncreaseValue;

                // If the value's too high then do the action
            } else {

                // Close the GUI and tell it to the player
                Output.get().sendWarn(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_gui_too_high));
                e.setCancelled(true);
                p.closeInventory();
                return;
            }

            // Open a new updated GUI with new values
            SellAllPrestigesSetMultiplierGUI gui = new SellAllPrestigesSetMultiplierGUI(p, val, part1);
            gui.open();
        }
    }

    private void sellAllMultipliersGUI(InventoryClickEvent e, Player p, String buttonNameMain, String[] parts) {

        if (parts[0].equalsIgnoreCase("Next") || parts[0].equalsIgnoreCase("Prior")){

            // Open a new SpigotLadders GUI page.
            SellAllPrestigesMultiplierGUI gui = new SellAllPrestigesMultiplierGUI(p, Integer.parseInt(parts[1]));
            p.closeInventory();
            gui.open();
            return;
        }

        // Check the clicks
        if (e.isRightClick()) {
            // Execute the command
        	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall multiplier delete" );
            Bukkit.dispatchCommand(p, registeredCmd + " " + buttonNameMain);
            // Cancel the event
            e.setCancelled(true);
            // Close the inventory
            p.closeInventory();
            // Open a GUI
            SellAllPrestigesMultiplierGUI gui = new SellAllPrestigesMultiplierGUI(p, 0);
            gui.open();
            return;
        } else {

            Configuration sellAllConfig = SpigotPrison.getInstance().getSellAllConfig();

            // Open setMultiplierGUI
            String doubleString = sellAllConfig.getString("Multiplier." + parts[0] + ".MULTIPLIER");
            if (doubleString != null) {
                SellAllPrestigesSetMultiplierGUI gui = new SellAllPrestigesSetMultiplierGUI(p, Double.parseDouble(doubleString), parts[0]);
                gui.open();
            }
        }

        // Cancel the event
        e.setCancelled(true);
    }

    private void sellAllDelayGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Rename the parts
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        // Initialize the variable
        int decreaseOrIncreaseValue = 0;

        // If there're enough parts init another variable
        if (parts.length == 4){
            decreaseOrIncreaseValue = Integer.parseInt(parts[3]);
        }

        // Check the button name and do the actions
        if (part1.equalsIgnoreCase("Confirm:")) {

            // Check the click type and do the actions
            if (e.isLeftClick()){

                // Execute the command
            	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall set delay" );
                Bukkit.dispatchCommand(p, registeredCmd + " " + part3);

                // Close the inventory
                p.closeInventory();

                return;

                // Check the click type and do the actions
            } else if (e.isRightClick()){

                // Send a message to the player
                Output.get().sendWarn(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_event_cancelled));

                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            } else {

                // Cancel the event
                e.setCancelled(true);
                return;
            }
        }

        // Give to val a value
        int val = Integer.parseInt(part2);

        // Check the calculator symbol
        if (part3.equals("-")){

            // Check if the value's already too low
            if (!((val -  decreaseOrIncreaseValue) < 0)) {

                // If it isn't too low then decrease it
                val = val - decreaseOrIncreaseValue;

                // If it is too low
            } else {

                // Tell to the player that the value's too low
                Output.get().sendWarn(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_gui_too_low_value));

                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();
                return;
            }

            // Open an updated GUI after the value changed
            SellAllDelayGUI gui = new SellAllDelayGUI(p, val);
            gui.open();

            // Check the calculator symbol
        } else if (part3.equals("+")){

            // Check if the value isn't too high
            if (!((val + decreaseOrIncreaseValue) > 2147483646)) {

                // Increase the value
                val = val + decreaseOrIncreaseValue;

                // If the value's too high then do the action
            } else {

                // Close the GUI and tell it to the player
                Output.get().sendWarn(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_gui_too_high));
                e.setCancelled(true);
                p.closeInventory();
                return;
            }

            // Open a new updated GUI with new values
            SellAllDelayGUI gui = new SellAllDelayGUI(p, val);
            gui.open();
        }
    }

    private void sellAllAutoSellAdminGUI(InventoryClickEvent e, Player p, String buttonNameMain) {
        switch (buttonNameMain){

            case "PerUserToggleable":{

            	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall autosell perusertoggleable" );
                Bukkit.dispatchCommand(p, registeredCmd + " false");
                SellAllAdminAutoSellGUI gui = new SellAllAdminAutoSellGUI(p);
                gui.open();

                break;
            }

            case "PerUserToggleable-Disabled":{

            	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall autosell perusertoggleable" );
                Bukkit.dispatchCommand(p, registeredCmd + " true");
                SellAllAdminAutoSellGUI gui = new SellAllAdminAutoSellGUI(p);
                gui.open();

                break;
            }

            case "AutoSell":{

            	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall autosell" );
                Bukkit.dispatchCommand(p, registeredCmd + " false");
                SellAllAdminGUI gui = new SellAllAdminGUI(p);
                gui.open();

                break;
            }

            case "AutoSell-Disabled":{

            	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall autosell" );
                Bukkit.dispatchCommand(p, registeredCmd + " true");
                SellAllAdminGUI gui = new SellAllAdminGUI(p);
                gui.open();

                break;
            }
        }

        e.setCancelled(true);
    }

    private void sellAllAdminGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        Configuration sellAllConfig = SpigotPrison.getInstance().getSellAllConfig();
        switch (buttonNameMain){

            case "Blocks-Shop":{

                SellAllAdminBlocksGUI gui = new SellAllAdminBlocksGUI(p, 0);
                gui.open();
                break;
            }

            case "AutoSell":{

                if (e.getClick().isRightClick()){
                	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall autosell" );
                    Bukkit.dispatchCommand(p, registeredCmd + " false");
                    SellAllAdminGUI gui = new SellAllAdminGUI(p);
                    gui.open();
                } else {
                    SellAllAdminAutoSellGUI gui = new SellAllAdminAutoSellGUI(p);
                    gui.open();
                }
                break;
            }

            case "AutoSell-Disabled":{

                if (e.getClick().isRightClick()){
                	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall autosell" );
                    Bukkit.dispatchCommand(p, registeredCmd + " true");
                    p.closeInventory();
                    SellAllAdminGUI gui = new SellAllAdminGUI(p);
                    gui.open();
                } else {
                    Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_sellall_auto_disabled_cant_use));
                }
                break;
            }

            case "Delay-Enabled":{

                if (e.getClick().isRightClick()){
                	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall delay" );
                    Bukkit.dispatchCommand(p, registeredCmd + " false");
                    p.closeInventory();
                    SellAllAdminGUI gui = new SellAllAdminGUI(p);
                    gui.open();
                } else {
                    p.closeInventory();

                    int val = 0;

                    try {
                        String valString = sellAllConfig.getString("Options.Sell_Delay_Seconds");
                        if (valString != null) {
                            val = Integer.parseInt(valString);
                        }
                    } catch (NumberFormatException ignored){}

                    p.closeInventory();
                    SellAllDelayGUI gui = new SellAllDelayGUI(p, val);
                    gui.open();
                }

                break;
            }

            case "Delay-Disabled":{

                if (e.getClick().isRightClick()){
                	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall delay" );
                    Bukkit.dispatchCommand(p, registeredCmd + " true");
                    p.closeInventory();
                    SellAllAdminGUI gui = new SellAllAdminGUI(p);
                    gui.open();
                } else {
                    Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_sellall_delay_disabled_cant_use));
                }
                break;
            }

            case "SellAll-Currency":{

                // Send messages to the player
                Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_sellall_currency_chat_1));
                Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_sellall_currency_chat_2));
                Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_sellall_currency_chat_3));
                Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_sellall_currency_chat_4));

                chatInteractData(p, ChatMode.SellAll_Currency);
                p.closeInventory();

                break;
            }

            case "Prestige-Multipliers":{

                boolean multiplierEnabled = getBoolean(sellAllConfig.getString("Options.Multiplier_Enabled"));
                if (!multiplierEnabled){

                    Output.get().sendWarn(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_sellall_multiplier_are_disabled));

                    return;
                } else {

                    if (sellAllConfig.getConfigurationSection("Multiplier").getKeys(false).size() == 0) {
                        Output.get().sendWarn(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_gui_error_empty));
                        e.setCancelled(true);
                        return;
                    } else {

                        SellAllPrestigesMultiplierGUI gui = new SellAllPrestigesMultiplierGUI(p, 0);
                        gui.open();

                    }
                }

                break;
            }
        }

        e.setCancelled(true);
    }

    private boolean guiConditions(InventoryClickEvent e, Player p) {

        // If you click an empty slot, this should avoid the error.
        // Also if there is no button that was clicked, then it may not be a Prison GUI on click event?
        if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR ||
                e.getCurrentItem().getItemMeta() == null || !e.getCurrentItem().hasItemMeta()) {
            activeGuiEventCanceller(p, e);
            return true;
        } else {
            e.getCurrentItem().getItemMeta().getDisplayName();
        }

        // If an action equals one of these, and the inventory is open from the player equals
        // one of the Prison Title, it'll cancel the event
        activeGuiEventCanceller(p, e);

        // ensure the item has itemMeta and a display name
        return !e.getCurrentItem().hasItemMeta();
    }

    private void prisonSetupConfirmGUI(InventoryClickEvent e, Player p, String[] parts) {

        if (parts[0].equalsIgnoreCase("Confirm:")){
            Bukkit.dispatchCommand(p, "ranks autoConfigure");
        } else if (parts[0].equalsIgnoreCase("Cancel:")){
            Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_event_cancelled));
        }
        p.closeInventory();
        e.setCancelled(true);
    }

    private void blocksListGUI(InventoryClickEvent e, Player p, String[] parts) {
        String positionStr = ( parts.length > 2 ? parts[2] : "0" );
        int position = 0;
        try {
            position = Integer.parseInt( positionStr );
        }
        catch(NumberFormatException ignored) {}

        if (parts[0].equalsIgnoreCase("Next") || parts[0].equalsIgnoreCase("Prior")){

            SpigotBlocksListGUI gui = new SpigotBlocksListGUI(p, parts[1], position);

            p.closeInventory();

            gui.open();
        } else {
            SpigotMineBlockPercentageGUI gui = new SpigotMineBlockPercentageGUI(p, 0.00, parts[1], parts[0], position);

            p.closeInventory();

            gui.open();
        }

        e.setCancelled(true);
    }

    private void mineBlockPercentage(InventoryClickEvent e, Player p, String[] parts) {

        if (parts[0].equalsIgnoreCase("Close")){
            p.closeInventory();
            return;
        }

        // Rename the parts
        String part1;
        String part2;
        String part3;

        try{

            part1 = parts[0];
            part2 = parts[1];
            part3 = parts[2];

        } catch (ArrayIndexOutOfBoundsException ex){
            return;
        }
        
        String part4 = null;
        try{
            part4 = parts[3];
        } catch(ArrayIndexOutOfBoundsException ignored){}

        // Initialize the variable
        double decreaseOrIncreaseValue = 0;

        // If there're enough parts init another variable
        if (parts.length > 4 ){
            decreaseOrIncreaseValue = Double.parseDouble(parts[4]);
        }
        
        String positionStr = ( parts.length > 5 ? parts[5] : "0" );
        int position = 0;
        try {
        	position = Integer.parseInt( positionStr );
        }
        catch(NumberFormatException ignored) {}

        
        // Check the button name and do the actions
        if (part1.equalsIgnoreCase("Confirm:")) {

            // Check the click type and do the actions
            if (e.isLeftClick()){

                // Execute the command
                if (part4 != null) {
                    Bukkit.dispatchCommand(p, "mines block set " + part2 + " " + part3 + " " + part4);
                }

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;

                // Check the click type and do the actions
            } else if (e.isRightClick()){

                // Send a message to the player
                Output.get().sendWarn(new SpigotPlayer(p), "&cEvent cancelled.");

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            } else {

                // Cancel the event
                e.setCancelled(true);
                return;
            }
        }

        // Give to val a value
        double val = Double.parseDouble(part3);

        // Check the calculator symbol
        if (part4 != null && part4.equals("-")){

            // Check if the value's already too low
            if (!((val -  decreaseOrIncreaseValue) < 0)) {

                // If it isn't too low then decrease it
                val = val - decreaseOrIncreaseValue;

                // If it is too low
            } else {

                // Tell to the player that the value's too low
                Output.get().sendWarn(new SpigotPlayer(p), "&cToo low, under 0%!");

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            }

            // Open an updated GUI after the value changed
            SpigotMineBlockPercentageGUI gui = new SpigotMineBlockPercentageGUI(p, val, part1, part2, position);
            gui.open();

            // Check the calculator symbol
        } else if (part4 != null && part4.equals("+")) {

            // Check if the value isn't too high
            if (!((val + decreaseOrIncreaseValue) > 100)) {

                // Increase the value
                val = val + decreaseOrIncreaseValue;

                // If the value's too high then do the action
            } else {

                // Close the GUI and tell it to the player
                Output.get().sendWarn(new SpigotPlayer(p), "&cToo high, exceed 100%!");

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            }

            // Open a new updated GUI with new values
            SpigotMineBlockPercentageGUI gui = new SpigotMineBlockPercentageGUI(p, val, part1, part2, position);
            gui.open();

            // Cancel the event
            e.setCancelled(true);
        }
    }

    private void sellAllItemValue(InventoryClickEvent e, Player p, String[] parts) {

        // Rename the parts
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        // Initialize the variable
        int decreaseOrIncreaseValue = 0;

        // If there're enough parts init another variable
        if (parts.length == 4){
            decreaseOrIncreaseValue = Integer.parseInt(parts[3]);
        }

        // Check the button name and do the actions
        if (part1.equalsIgnoreCase("Confirm:")) {

            // Check the click type and do the actions
            if (e.isLeftClick()){

                // Execute the command
                Bukkit.dispatchCommand(p,"sellall edit " + part2 + " " + part3);

                // Close the inventory
                p.closeInventory();

                return;

                // Check the click type and do the actions
            } else if (e.isRightClick()){

                // Send a message to the player
                Output.get().sendWarn(new SpigotPlayer(p), "&cEvent cancelled.");

                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            } else {

                // Cancel the event
                e.setCancelled(true);
                return;
            }
        }

        // Give to val a value
        double val = Double.parseDouble(part2);

        // Check the calculator symbol
        if (part3.equals("-")){

            // Check if the value's already too low
            if (!((val -  decreaseOrIncreaseValue) < 0)) {

                // If it isn't too low then decrease it
                val = val - decreaseOrIncreaseValue;

                // If it is too low
            } else {

                // Tell to the player that the value's too low
                Output.get().sendWarn(new SpigotPlayer(p), "&cToo low value.");

                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();
                return;
            }

            // Open an updated GUI after the value changed
            SellAllPriceGUI gui = new SellAllPriceGUI(p, val, part1);
            gui.open();

            // Check the calculator symbol
        } else if (part3.equals("+")){

            // Check if the value isn't too high
            if (!((val + decreaseOrIncreaseValue) > 2147483646)) {

                // Increase the value
                val = val + decreaseOrIncreaseValue;

                // If the value's too high then do the action
            } else {

                // Close the GUI and tell it to the player
                Output.get().sendWarn(new SpigotPlayer(p), "&cToo high value.");
                e.setCancelled(true);
                p.closeInventory();
                return;
            }

            // Open a new updated GUI with new values
            SellAllPriceGUI gui = new SellAllPriceGUI(p, val, part1);
            gui.open();

        }
    }

    private void sellAllAdminBlocksGUI(InventoryClickEvent e, Player p, String[] parts) {

        if (parts[0].equalsIgnoreCase("Prior")){

            SellAllAdminBlocksGUI gui = new SellAllAdminBlocksGUI(p, Integer.parseInt(parts[1]));
            gui.open();

            e.setCancelled(true);
            return;
        } else if (parts[0].equalsIgnoreCase("Next")){

            SellAllAdminBlocksGUI gui = new SellAllAdminBlocksGUI(p, Integer.parseInt(parts[1]));
            gui.open();

            e.setCancelled(true);
            return;
        }

        if (e.isRightClick()){

        	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall delete" );
            Bukkit.dispatchCommand(p, registeredCmd + " " + parts[0]);
            p.closeInventory();

        } else if (e.isLeftClick()){

            Configuration sellAllConfig = SpigotPrison.getInstance().getSellAllConfig();

            String valueString = sellAllConfig.getString("Items." + parts[0] + ".ITEM_VALUE");
            if (valueString != null) {
                SellAllPriceGUI gui = new SellAllPriceGUI(p, Double.parseDouble(valueString), parts[0]);
                gui.open();
            }
        }

        e.setCancelled(true);
    }

    private void prisonManagerGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Check the Item display name and do open the right GUI.
        switch (buttonNameMain) {
            case "Ranks - Ladders": {
                SpigotLaddersGUI gui = new SpigotLaddersGUI(p, 0);
                gui.open();
                break;
            }

            // Check the Item display name and do open the right GUI.
            case "AutoManager": {

                // Check if the autofeatures config isn't null.
                if(SpigotGUIComponents.afConfig() != null) {
                    SpigotAutoFeaturesGUI gui = new SpigotAutoFeaturesGUI(p);
                    gui.open();
                } else {
                    Output.get().sendWarn(new SpigotPlayer(p), "Can't find an autofeatures config, maybe they're disabled.");
                }
                break;
            }

            // Check the Item display name and do open the right GUI.
            case "Mines": {
                SpigotMinesGUI gui = new SpigotMinesGUI(p, 0);
                gui.open();
                break;
            }

            // Check the Item display name and do open the right GUI.
            case "SellAll": {
                SellAllAdminGUI gui = new SellAllAdminGUI(p);
                gui.open();
                break;
            }
            case "Backpacks": {
                BackpacksAdminGUI gui = new BackpacksAdminGUI(p);
                gui.open();
                break;
            }
        }

        // Cancel the event.
        e.setCancelled(true);
    }

    private void laddersGUI(InventoryClickEvent e, Player p, String buttonNameMain, Module module, String[] parts) {

        // Check if the Ranks module's loaded.
        if(!(module instanceof PrisonRanks)){
            Output.get().sendWarn(new SpigotPlayer(p), "The GUI can't open because the &3Rank module &cisn't loaded");
            p.closeInventory();
            e.setCancelled(true);
            return;
        }

        if (parts[0].equalsIgnoreCase("Next") || parts[0].equalsIgnoreCase("Prior")){

            // Open a new SpigotLadders GUI page.
            SpigotLaddersGUI gui = new SpigotLaddersGUI(p, Integer.parseInt(parts[1]));
            p.closeInventory();
            gui.open();
            return;
        }

        // Get the ladder by the name of the button got before.
        ladder = Optional.of(PrisonRanks.getInstance().getLadderManager().getLadder(buttonNameMain));

        // When the player click an item with shift and right click, e.isShiftClick should be enough but i want
        // to be sure's a right click.
        if (e.isShiftClick() && e.isRightClick()) {

            // Execute the command
            Bukkit.dispatchCommand(p, "ranks ladder delete " + buttonNameMain);
            e.setCancelled(true);
            p.closeInventory();
            SpigotLaddersGUI gui = new SpigotLaddersGUI(p, 0);
            gui.open();
            return;

        }

        // Open the GUI of ranks.
        SpigotRanksGUI gui = new SpigotRanksGUI(p, ladder, 0);
        gui.open();

        // Cancel the event.
        e.setCancelled(true);
    }

    private void ranksGUI(InventoryClickEvent e, Player p, String buttonNameMain, String[] parts) {

        if (parts[0].equalsIgnoreCase("Next") || parts[0].equalsIgnoreCase("Prior")){

            // Open a new SpigotLadders GUI page.
            SpigotRanksGUI gui = new SpigotRanksGUI(p, ladder, Integer.parseInt(parts[1]));
            p.closeInventory();
            gui.open();
            return;
        }

        // Get the rank.
        Rank rank = PrisonRanks.getInstance().getRankManager().getRank(buttonNameMain);

        // Check if the rank exist.
        if (rank == null) {
            Output.get().sendWarn(new SpigotPlayer(p), "&cThe rank " + buttonNameMain + " does not exist.");
            return;
        }

        // Check clicks.
        if (e.isShiftClick() && e.isRightClick()) {

            // Execute the command.
            Bukkit.dispatchCommand(p, "ranks delete " + buttonNameMain);
            e.setCancelled(true);
            p.closeInventory();
            SpigotRanksGUI gui = new SpigotRanksGUI(p, ladder, 0);
            gui.open();
            return;

        } else {

            // Open a GUI.
            SpigotRankManagerGUI gui = new SpigotRankManagerGUI(p, rank);
            gui.open();

        }

        // Cancel the event.
        e.setCancelled(true);
    }

    private void playerPrestigesGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Check the button name and do the actions.
        if (buttonNameMain.equalsIgnoreCase("Prestige")){
            // Close the inventory.
            p.closeInventory();
            // Execute the command.
            Bukkit.dispatchCommand(p, "prestige");
        }

        // Cancel the event.
        e.setCancelled(true);
    }

    private void prestigeConfirmationGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Check the button name and do the actions.
        if (buttonNameMain.equalsIgnoreCase("Confirm: Prestige")){
        	Output.get().logDebug( DebugTarget.rankup, "rankup: GUI: 'Confirm: Prestige'   calling: '/rankup prestiges'" );
        	
        	// Execute the command.
        	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "rankup" );

            Bukkit.dispatchCommand(p, registeredCmd + " prestiges");
            // Close the inventory.
            p.closeInventory();
        } else if (buttonNameMain.equalsIgnoreCase("Cancel: Don't Prestige")){
        	Output.get().logDebug( DebugTarget.rankup, "rankup: GUI/: 'Cancel: Don't Prestige'   sendInfo: 'cancelled'" );

        	// Send a message to the player.
            Output.get().sendInfo(new SpigotPlayer(p), "&cCancelled");
            // Close the inventory.
            p.closeInventory();
        }

        // Cancel the event.
        e.setCancelled(true);
    }

    private void rankManagerGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Output finally the buttonName and the minename explicit out of the array.
        String buttonName = parts[0];
        String rankName = parts[1];

        // Get the rank.
        Rank rank = PrisonRanks.getInstance().getRankManager().getRank(rankName);

        // Check the button name and do the actions.
        if (buttonName.equalsIgnoreCase("RankupCommands")){

            // Check if the rank exist.
            if (rank == null) {
                // Send a message to the player.
                Output.get().sendWarn(new SpigotPlayer(p), "&cThe rank " + rankName + " does not exist.");
                return;
            }

            // Check the rankupCommand of the Rank.
            if (rank.getRankUpCommands() == null) {
                // Send a message to the player.
                Output.get().sendWarn(new SpigotPlayer(p), "&cThere aren't commands for this rank anymore.");
            }

            // Open the GUI of commands.
            else {
                SpigotRankUPCommandsGUI gui = new SpigotRankUPCommandsGUI(p, rank);
                gui.open();
            }

        // Check the button name and do the actions.
        } else if (buttonName.equalsIgnoreCase("RankPrice")){

            // Check and open a GUI.
            if(rank != null) {
                RankPlayer rankPlayer = PrisonRanks.getInstance().getPlayerManager().getPlayer( new SpigotPlayer(p) );
                PlayerRank pRank = rankPlayer.getRank( rank.getLadder() );
                
                SpigotRankPriceGUI gui = new SpigotRankPriceGUI(p, pRank.getRankCost().intValue(), rank.getName());
                gui.open();
            }

        // Check the button name and do the actions.
        } else if (buttonName.equalsIgnoreCase("RankTag")){

            // Send messages to the player.
            Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_ranks_tag_chat_rename_1));
            Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_ranks_tag_chat_rename_2));
            // Start the async task.
            tempChatVariable = rankName;
            chatInteractData(p, ChatMode.RankName);
            p.closeInventory();
        }

        // Cancel the event.
        e.setCancelled(true);
    }

    private void playerRanksGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Check the buttonName and do the actions.
        if (buttonNameMain.equals(SpigotPrison.format(messages.getString(MessagesConfig.StringID.spigot_gui_lore_rankup).substring(2)))){
            Bukkit.dispatchCommand(p, "rankup " + guiConfig.getString("Options.Ranks.Ladder"));
            p.closeInventory();
        }

        // Cancel the event.
        e.setCancelled(true);
    }

    private void rankUPCommandsGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Check the clickType.
        if (e.isShiftClick() && e.isRightClick()) {

            // Execute the command.
            Bukkit.dispatchCommand(p, "ranks command remove " + buttonNameMain);
            // Cancel the event.
            e.setCancelled(true);
            // Close the inventory.
            p.closeInventory();
            return;

        }

        // Cancel the event.
        e.setCancelled(true);
    }

    private void rankPriceGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Rename the parts.
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        // Initialize the variable.
        int decreaseOrIncreaseValue = 0;

        // If there're enough parts init another variable.
        if (parts.length == 4){
            decreaseOrIncreaseValue = Integer.parseInt(parts[3]);
        }

        // Check the button name and do the actions.
        if (part1.equalsIgnoreCase("Confirm:")) {

            // Check the click type and do the actions.
            if (e.isLeftClick()){

                // Execute the command.
                Bukkit.dispatchCommand(p,"ranks set cost " + part2 + " " + part3);

                // Close the inventory.
                p.closeInventory();

                return;

                // Check the click type and do the actions.
            } else if (e.isRightClick()){

                // Send a message to the player.
                Output.get().sendWarn(new SpigotPlayer(p), "&cEvent cancelled.");

                e.setCancelled(true);

                // Close the inventory.
                p.closeInventory();

                return;
            } else {

                // Cancel the event.
                e.setCancelled(true);
                return;
            }
        }

        // Give to val a value.
        int val = Integer.parseInt(part2);

        // Check the calculator symbol.
        if (part3.equals("-")){

            // Check if the value's already too low.
            if (!((val -  decreaseOrIncreaseValue) < 0)) {

                // If it isn't too low then decrease it.
                val = val - decreaseOrIncreaseValue;

                // If it is too low.
            } else {

                // Tell to the player that the value's too low.
                Output.get().sendWarn(new SpigotPlayer(p), "&cToo low value.");

                e.setCancelled(true);

                // Close the inventory.
                p.closeInventory();
                return;
            }

            // Open an updated GUI after the value changed.
            SpigotRankPriceGUI gui = new SpigotRankPriceGUI(p, val, part1);
            gui.open();

            // Check the calculator symbol
        } else if (part3.equals("+")){

            // Check if the value isn't too high.
            if (!((val + decreaseOrIncreaseValue) > 2147483646)) {

                // Increase the value
                val = val + decreaseOrIncreaseValue;

                // If the value's too high then do the action.
            } else {

                // Close the GUI and tell it to the player.
                Output.get().sendWarn(new SpigotPlayer(p), "&cToo high value.");
                e.setCancelled(true);
                p.closeInventory();
                return;
            }

            // Open a new updated GUI with new values.
            SpigotRankPriceGUI gui = new SpigotRankPriceGUI(p, val, part1);
            gui.open();

        }
    }

    private void minesGUI(InventoryClickEvent e, Player p, String buttonNameMain, String[] parts) {

        if (parts[0].equalsIgnoreCase("Next") || parts[0].equalsIgnoreCase("Prior")){

            // Open a new SpigotLadders GUI page.
            SpigotMinesGUI gui = new SpigotMinesGUI(p, Integer.parseInt(parts[1]));
            p.closeInventory();
            gui.open();
            return;
        }

        // Variables.
        PrisonMines pMines = PrisonMines.getInstance();
        Mine m = pMines.getMine(buttonNameMain);

        // Check the clicks.
        if (e.isShiftClick() && e.isRightClick()) {
            // Execute the command.
            Bukkit.dispatchCommand(p, "mines delete " + buttonNameMain);
            // Cancel the event.
            e.setCancelled(true);
            // Close the inventory.
            p.closeInventory();
            // Open a GUI.
            SpigotMinesConfirmGUI gui = new SpigotMinesConfirmGUI(p, buttonNameMain);
            gui.open();
            return;
        }

        // Open the GUI of mines info.
        SpigotMineInfoGUI gui = new SpigotMineInfoGUI(p, m, buttonNameMain);
        gui.open();

        // Cancel the event.
        e.setCancelled(true);
    }

    private void playerMinesGUI(Player p, InventoryClickEvent e) {

        String permission = SpigotPrison.format(guiConfig.getString("Options.Mines.PermissionWarpPlugin"));

        String mineName = null;

        if (e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getLore() != null && e.getCurrentItem().getItemMeta().getLore().get(0) != null) {
            mineName = e.getCurrentItem().getItemMeta().getLore().get(0).substring(2);
        }

        if (mineName != null) {
        	
        	// Confirm the player has access to the mine before trying to TP them there:
        	Mine mine = PrisonMines.getInstance().getMine( mineName );
        	SpigotPlayer spigotPlayer = new SpigotPlayer(p);
        	if (mine.hasMiningAccess(spigotPlayer) || p.hasPermission(permission + mineName) || 
        						p.hasPermission(permission.substring(0, permission.length() - 1))) {
        		Bukkit.dispatchCommand(p, SpigotPrison.format(guiConfig.getString("Options.Mines.CommandWarpPlugin") + " " + mineName));
        	}
        }
    }

    private void mineInfoGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Output finally the buttonName and the mineName explicit out of the array.
        String buttonName = parts[0];
        String mineName = parts[1];

        // Check the name of the button and do the actions.
        switch (buttonName) {
            case "Blocks_of_the_Mine:":

                // Open the GUI.
                SpigotMinesBlocksGUI gui = new SpigotMinesBlocksGUI(p, mineName);
                gui.open();

                break;

            // Check the name of the button and do the actions.
            case "Reset_Mine:":

                // Check the clickType and do the actions.
                if (e.isLeftClick()) {
                    // Execute the command.
                    Bukkit.dispatchCommand(p, "mines reset " + mineName);
                } else if (e.isRightClick()){
                    // Execute the command.
                    Bukkit.dispatchCommand(p, "mines set skipReset " + mineName);
                } else if (e.isRightClick() && e.isShiftClick()){
                    // Execute the command.
                    Bukkit.dispatchCommand(p, "mines set zeroBlockResetDelay " + mineName);
                }

                // Cancel the event.
                e.setCancelled(true);

                break;

            // Check the name of the button and do the actions.
            case "Mine_Spawn:":

                // Execute the command.
                Bukkit.dispatchCommand(p, "mines set spawn " + mineName);

                // Cancel the event.
                e.setCancelled(true);
                break;

            // Check the name of the button and do the actions.
            case "Mine_notifications:":

                // Open the GUI.
                SpigotMineNotificationsGUI gui1 = new SpigotMineNotificationsGUI(p, mineName);
                gui1.open();

                break;

            // Check the name of the button and do the actions.
            case "TP_to_the_Mine:":

                // Close the inventory.
                p.closeInventory();

                // Execute the Command.
                Bukkit.dispatchCommand(p, "mines tp " + mineName);

                break;

            // Check the name of the button and do the actions.
            case "Reset_Time:":

                // Initialize the variables.
                PrisonMines pMines = PrisonMines.getInstance();
                Mine m = pMines.getMine(mineName);
                int val = m.getResetTime();

                // Open the GUI.
                SpigotMineResetTimeGUI gui2 = new SpigotMineResetTimeGUI(p, val, mineName);
                gui2.open();

                break;
            case "Mine_Name:": {

                // Send messages to the player
                Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_mines_name_chat_1));
                Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_mines_name_chat_2));

                // Start the async task
                tempChatVariable = mineName;
                chatInteractData(p, ChatMode.MineName);
                p.closeInventory();
                break;
            }
            case "Mine_Show_Item:":{

                // Open select Block GUI.
                SpigotBlocksMineListGUI guiBlocks = new SpigotBlocksMineListGUI(p, mineName, 0);
                guiBlocks.open();

                break;
            }
        }
    }

    private void minesDeleteGUI(Player p, String[] parts) {

        // Output finally the buttonname and the minename explicit out of the array
        String buttonname = parts[0];
        String mineName = parts[1];

        // Check the name of the button and do the actions
        if (buttonname.equals("Confirm:")) {

            // Confirm
            Bukkit.dispatchCommand(p, "mines delete " + mineName + " confirm");

            // Close the Inventory
            p.closeInventory();

        // Check the name of the button and do the actions
        } else if (buttonname.equals("Cancel:")) {

            // Cancel
            Bukkit.dispatchCommand(p, "mines delete " + mineName + " cancel");

            // Close the inventory
            p.closeInventory();

        }
    }

    private void blocksGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Output finally the buttonname and the minename explicit out of the array
        String buttonname = parts[0];
        String mineName = parts[1];

        if (buttonname.equalsIgnoreCase("Add")){
            SpigotBlocksListGUI gui = new SpigotBlocksListGUI(p, mineName, 0);

            p.closeInventory();

            gui.open();
            return;
        }

        // Check the click Type and do the actions
        if (e.isShiftClick() && e.isRightClick()) {

            // Execute the command
            Bukkit.dispatchCommand(p, "mines block remove " + mineName + " " + buttonname);

            // Cancel the event
            e.setCancelled(true);

            // Close the GUI so it can be updated
            p.closeInventory();

            // Open the GUI
            SpigotMinesBlocksGUI gui = new SpigotMinesBlocksGUI(p, mineName);
            gui.open();
        } else {
        	
        	String positionStr = ( parts.length > 2 ? parts[2] : "0" );
        	int position = 0;
        	try {
        		position = Integer.parseInt( positionStr );
        	}
        	catch(NumberFormatException ignored) {}

            double percentage = Double.parseDouble(parts[2]);
            SpigotMineBlockPercentageGUI gui = new SpigotMineBlockPercentageGUI(p, percentage, mineName, buttonname, position);
            gui.open();

        }
    }

    private void resetTimeGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Rename the parts
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        // Initialize the variable
        int decreaseOrIncreaseValue = 0;

        // If there're enough parts init another variable
        if (parts.length == 4){
            decreaseOrIncreaseValue = Integer.parseInt(parts[3]);
        }

        // Check the button name and do the actions
        if (part1.equalsIgnoreCase("Confirm:")) {

            // Check the click type and do the actions
            if (e.isLeftClick()){

                // Execute the command
                Bukkit.dispatchCommand(p,"mines set resettime " + part2 + " " + part3);

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;

            // Check the click type and do the actions
            } else if (e.isRightClick()){

                // Send a message to the player
                Output.get().sendWarn(new SpigotPlayer(p), "&cEvent cancelled.");

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            } else {

                // Cancel the event
                e.setCancelled(true);
                return;
            }
        }

        // Give to val a value
        int val = Integer.parseInt(part2);

        // Check the calculator symbol
        if (part3.equals("-")){

            // Check if the value's already too low
            if (!((val -  decreaseOrIncreaseValue) < 0)) {

                // If it isn't too low then decrease it
                val = val - decreaseOrIncreaseValue;

            // If it is too low
            } else {

                // Tell to the player that the value's too low
                Output.get().sendWarn(new SpigotPlayer(p), "&cToo low value.");

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            }

            // Open an updated GUI after the value changed
            SpigotMineResetTimeGUI gui = new SpigotMineResetTimeGUI(p, val, part1);
            gui.open();

        // Check the calculator symbol
        } else if (part3.equals("+")){

            // Check if the value isn't too high
            if (!((val + decreaseOrIncreaseValue) > 999999)) {

                // Increase the value
                val = val + decreaseOrIncreaseValue;

            // If the value's too high then do the action
            } else {

                // Close the GUI and tell it to the player
                Output.get().sendWarn(new SpigotPlayer(p), "&cToo high value.");

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            }

            // Open a new updated GUI with new values
            SpigotMineResetTimeGUI gui = new SpigotMineResetTimeGUI(p, val, part1);
            gui.open();

            // Cancel the event
            e.setCancelled(true);

        }
    }

    private void mineNotificationsGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Output finally the buttonname and the minename explicit out of the array
        String buttonname = parts[0];
        String mineName = parts[1];
        String typeNotification;
        long val;

        // Init variables
        PrisonMines pMines = PrisonMines.getInstance();
        Mine m = pMines.getMine(mineName);

        // Check the button name and do the actions
        if (buttonname.equalsIgnoreCase("Within_Mode:")){

            // Change the value of the variable
            typeNotification = "within";

            // Execute command
            Bukkit.dispatchCommand(p, "mines set notification " + mineName + " " + typeNotification + " 0");

            // Cancel the event and close the inventory
            e.setCancelled(true);
            p.closeInventory();

            // Check the button name and do the actions
        } else if (buttonname.equalsIgnoreCase("Radius_Mode:")){

            // Change the value of the variable
            typeNotification = "radius";

            // Get the variable value
            val = m.getNotificationRadius();

            // Open the GUI
            SpigotMineNotificationRadiusGUI gui = new SpigotMineNotificationRadiusGUI(p, val,  typeNotification, mineName);
            gui.open();

        // Check the button name and do the actions
        } else if (buttonname.equalsIgnoreCase("Disabled_Mode:")){

            // Change the value of the variable
            typeNotification = "disabled";

            // Execute the command
            Bukkit.dispatchCommand(p, "mines set notification " + mineName + " " + typeNotification + " 0");

            // Cancel the event and close the inventory
            e.setCancelled(true);
            p.closeInventory();

        }
    }

    private void radiusGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Rename the variables
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];
        String typeNotification;

        // Init the variable
        int decreaseOrIncreaseValue = 0;

        // Check the button name and do the actions
        if (!(part1.equalsIgnoreCase("Confirm:"))){

            // Give them a value
            decreaseOrIncreaseValue = Integer.parseInt(parts[3]);
            typeNotification = parts[4];

        // Do others actions
        } else {

            // Give it a value
            typeNotification = parts[3];
        }

        // Check the button name and do the actions
        if (part1.equalsIgnoreCase("Confirm:")) {

            // Check the click type
            if (e.isLeftClick()){

                // Execute the command
                Bukkit.dispatchCommand(p,"mines set notification " + part2 + " " + typeNotification + " " + part3);

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            } else if (e.isRightClick()){

                // Close the inventory
                Output.get().sendWarn(new SpigotPlayer(p), "&cEvent cancelled.");

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            } else {

                // Cancel the event
                e.setCancelled(true);
                return;
            }
        }

        // Init a new value
        long val = Integer.parseInt(part2);

        // Check the calculator symbol
        if (part3.equals("-")){

            // Check if the value's too low
            if (!((val -  decreaseOrIncreaseValue) < 0)) {

                // Decrease the value
                val = val - decreaseOrIncreaseValue;

            // If the value's too low
            } else {

                // Close the inventory and tell it the player
                Output.get().sendWarn(new SpigotPlayer(p), "&cToo low value.");

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();
                return;
            }

            // Open a new updated GUI with new values
            SpigotMineNotificationRadiusGUI gui = new SpigotMineNotificationRadiusGUI(p, val,  typeNotification, part1);
            gui.open();

        // Check the calculator symbol
        } else if (part3.equals("+")){

            // Check if the value's too high
            if (!((val + decreaseOrIncreaseValue) > 9999999)) {

                // Increase the value
                val = val + decreaseOrIncreaseValue;

            // If the value's too high
            } else {

                // Close the inventory and tell it to the player
                Output.get().sendWarn(new SpigotPlayer(p), "&cToo high value.");

                // Cancel the inventory
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();
                return;
            }

            // Open a new updated GUI with new values
            SpigotMineNotificationRadiusGUI gui = new SpigotMineNotificationRadiusGUI(p, val,  typeNotification, part1);
            gui.open();

        }
    }

    private void autoFeaturesGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Get the config
        AutoFeaturesFileConfig afConfig = AutoFeaturesWrapper.getInstance().getAutoFeaturesConfig();
        
        // Output finally the buttonname and the mode explicit out of the array
        String buttonName = parts[0];
        String mode = parts[1];
        
        boolean enabled = mode.equalsIgnoreCase("Enabled");

        // Check the clickType and do the actions
        if ( enabled && e.isRightClick() && e.isShiftClick() ||
                !enabled && e.isRightClick()){

            if (buttonName.equalsIgnoreCase("Full-Inventory-Sound")){
                afConfig.setFeature( AutoFeatures.playSoundIfInventoryIsFull, !enabled );
                saveConfigAutoFeatures(e, p);
            }

            if (buttonName.equalsIgnoreCase("Full-Inventory-Hologram")){
                afConfig.setFeature(AutoFeatures.hologramIfInventoryIsFull, !enabled);
                saveConfigAutoFeatures(e,p);
            }

            if (buttonName.equalsIgnoreCase("All")){
                afConfig.setFeature(AutoFeatures.isAutoManagerEnabled, !enabled);
                saveConfigAutoFeatures(e,p);
            }

        }

        // Check the clickType and do the actions
        if (enabled && e.isRightClick() && e.isShiftClick() || !enabled && e.isRightClick() || enabled && e.isLeftClick()){
            if (buttonName.equalsIgnoreCase("AutoPickup")){
                if (e.isLeftClick()){
                    SpigotAutoPickupGUI gui = new SpigotAutoPickupGUI(p);
                    gui.open();
                    return;
                }
                afConfig.setFeature(AutoFeatures.autoPickupEnabled, !enabled);
                saveConfigAutoFeatures(e,p);
            }

            if (buttonName.equalsIgnoreCase("AutoSmelt")){
                if (e.isLeftClick()){
                    SpigotAutoSmeltGUI gui = new SpigotAutoSmeltGUI(p);
                    gui.open();
                    return;
                }
                afConfig.setFeature(AutoFeatures.autoSmeltEnabled, !enabled);
                saveConfigAutoFeatures(e,p);
            }

            if (buttonName.equalsIgnoreCase("AutoBlock")){
                if (e.isLeftClick()){
                    SpigotAutoBlockGUI gui = new SpigotAutoBlockGUI(p);
                    gui.open();
                    return;
                }
                afConfig.setFeature(AutoFeatures.autoBlockEnabled, !enabled);
                saveConfigAutoFeatures(e,p);
            }
        }
    }

    private void autoPickupGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Get the config
        AutoFeaturesFileConfig afConfig = AutoFeaturesWrapper.getInstance().getAutoFeaturesConfig();

        // Output finally the buttonname and the mode explicit out of the array
        String buttonname = parts[0];
        String mode = parts[1];

        boolean enabled = mode.equalsIgnoreCase("Enabled");
        
        // Check the click and do the actions, also the buttonName
        if ( enabled && e.isRightClick() && e.isShiftClick() ||
        		!enabled && e.isRightClick() ){

            switch (buttonname){
                case "All_Blocks":{
                    afConfig.setFeature( AutoFeatures.pickupAllBlocks, !enabled );
                    saveConfigPickup(e, p);
                    break;
                }
                case "Cobblestone":{
                    afConfig.setFeature(AutoFeatures.pickupCobbleStone, !enabled);
                    saveConfigPickup(e,p);
                    break;
                }
                case "Gold_Ore":{
                    afConfig.setFeature( AutoFeatures.pickupGoldOre, !enabled );
                    saveConfigPickup(e, p);
                    break;
                }
                case "Iron_Ore":{
                    afConfig.setFeature( AutoFeatures.pickupIronOre, !enabled );
                    saveConfigPickup(e, p);
                    break;
                }
                case "Coal_Ore":{
                    afConfig.setFeature( AutoFeatures.pickupCoalOre, !enabled );
                    saveConfigPickup(e, p);
                    break;
                }
                case "Diamond_Ore":{
                    afConfig.setFeature( AutoFeatures.pickupDiamondOre, !enabled );
                    saveConfigPickup(e, p);
                    break;
                }
                case "Redstone_Ore":{
                    afConfig.setFeature( AutoFeatures.pickupRedStoneOre, !enabled );
                    saveConfigPickup(e, p);
                    break;
                }
                case "Emerald_Ore":{
                    afConfig.setFeature( AutoFeatures.pickupEmeraldOre, !enabled );
                    saveConfigPickup(e, p);
                    break;
                }
                case "Quartz_Ore":{
                    afConfig.setFeature( AutoFeatures.pickupQuartzOre, !enabled );
                    saveConfigPickup(e, p);
                    break;
                }
                case "Lapis_Ore":{
                    afConfig.setFeature( AutoFeatures.pickupLapisOre, !enabled );
                    saveConfigPickup(e, p);
                    break;
                }
                case "Snow_Ball":{
                    afConfig.setFeature( AutoFeatures.pickupSnowBall, !enabled );
                    saveConfigPickup(e, p);
                    break;
                }
                case "Glowstone_Dust":{
                    afConfig.setFeature( AutoFeatures.pickupGlowstoneDust, !enabled );
                    saveConfigPickup(e, p);
                    break;
                }
                default:{
                    break;
                }

            }
        }
    }

    private void autoSmeltGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Get the config
        AutoFeaturesFileConfig afConfig = AutoFeaturesWrapper.getInstance().getAutoFeaturesConfig();

        // Output finally the buttonname and the mode explicit out of the array
        String buttonname = parts[0];
        String mode = parts[1];
        
        boolean enabled = mode.equalsIgnoreCase("Enabled");

        // Check the clickType and do the actions
        if ( enabled && e.isRightClick() && e.isShiftClick() ||
        		!enabled && e.isRightClick()){

            switch (buttonname){
                case "Gold_Ore":{
                    afConfig.setFeature( AutoFeatures.smeltGoldOre, !enabled );
                    saveConfigSmelt(e, p);
                    break;
                }
                case "Iron_Ore":{
                    afConfig.setFeature( AutoFeatures.smeltIronOre, !enabled );
                    saveConfigSmelt(e, p);
                    break;
                }
                case "All_Ores":{
                    afConfig.setFeature( AutoFeatures.smeltAllBlocks, !enabled );
                    saveConfigSmelt(e, p);
                    break;
                }
                default:{
                    break;
                }
            }
        }
    }

    private void autoBlockGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Get the config
        AutoFeaturesFileConfig afConfig = AutoFeaturesWrapper.getInstance().getAutoFeaturesConfig();

        // Output finally the buttonname and the mode explicit out of the array
        String buttonname = parts[0];
        String mode = parts[1];

        boolean enabled = mode.equalsIgnoreCase("Enabled");

        // Check the clickType and do the actions
        if ( enabled && e.isRightClick() && e.isShiftClick() ||
        		!enabled && e.isRightClick()){

            switch (buttonname){
                case "Gold_Block":{
                    afConfig.setFeature( AutoFeatures.blockGoldBlock, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                case "Iron_Block":{
                    afConfig.setFeature( AutoFeatures.blockIronBlock, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                case "Coal_Block":{
                    afConfig.setFeature( AutoFeatures.blockCoalBlock, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                case "Diamond_Block":{
                    afConfig.setFeature( AutoFeatures.blockDiamondBlock, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                case "Redstone_Block":{
                    afConfig.setFeature( AutoFeatures.blockRedstoneBlock, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                case "Emerald_Block":{
                    afConfig.setFeature( AutoFeatures.blockEmeraldBlock, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                case "Quartz_Block":{
                    afConfig.setFeature( AutoFeatures.blockQuartzBlock, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                case "Prismarine_Block":{
                    afConfig.setFeature( AutoFeatures.blockPrismarineBlock, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                case "Lapis_Block":{
                    afConfig.setFeature( AutoFeatures.blockLapisBlock, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                case "Snow_Block":{
                    afConfig.setFeature( AutoFeatures.blockSnowBlock, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                case "Glowstone_Block":{
                    afConfig.setFeature( AutoFeatures.blockGlowstone, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                case "All_Blocks":{
                    afConfig.setFeature( AutoFeatures.blockAllBlocks, !enabled );
                    saveConfigBlock(e, p);
                    break;
                }
                default:{
                    break;
                }
            }
        }

    }


    private void modeAction(AsyncPlayerChatEvent e, Player p, String message) {
        switch(mode){
            case Prestige:{
                prestigeAction(e, p, message);
                break;
            }
            case SellAll_Currency:{
                sellAllCurrencyChat(e, p, message);
                break;
            }
            case RankName:{
                rankAction(e, p, message);
                break;
            }
            case MineName:{
                mineAction(e, p, message);
                break;
            }
            default:{
                break;
            }
        }
    }

    private void sellAllCurrencyChat(AsyncPlayerChatEvent e, Player p, String message) {

        // Check message and do the action
    	String registeredCmd = Prison.get().getCommandHandler().findRegisteredCommand( "sellall set currency" );
        if (message.equalsIgnoreCase("cancel")){
            Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_event_cancelled));
        } else if (message.equalsIgnoreCase("default")){
            Bukkit.getScheduler().runTask(SpigotPrison.getInstance(), () -> Bukkit.getServer().dispatchCommand(p, registeredCmd + " default"));
        } else {
            Bukkit.getScheduler().runTask(SpigotPrison.getInstance(), () -> Bukkit.getServer().dispatchCommand(p, registeredCmd + " " + message));
        }

        // Cancel event.
        e.setCancelled(true);
        // Set the event to false, because it's finished.
        isChatEventActive = false;
    }

    private void prestigeAction(AsyncPlayerChatEvent e, Player p, String message) {

        // Check the chat message and do the actions
        if (message.equalsIgnoreCase("cancel")) {
            Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_prestiges_cancelled));
        } else if (message.equalsIgnoreCase("confirm")) {
            Bukkit.getScheduler().runTask(SpigotPrison.getInstance(), () -> Bukkit.getServer().dispatchCommand(p, "rankup prestiges"));
        } else {
            Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_prestiges_cancelled_wrong_keyword));
        }
        // Cancel the event
        e.setCancelled(true);
        // Set the event to false, because it got deactivated
        isChatEventActive = false;
    }

    private void mineAction(AsyncPlayerChatEvent e, Player p, String message) {

        // Check the chat message and do the action
        if (message.equalsIgnoreCase("close")) {
            Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_mines_name_chat_cancelled));
        } else {
            Bukkit.getScheduler().runTask(SpigotPrison.getInstance(), () -> Bukkit.getServer().dispatchCommand(p, "mines rename " + tempChatVariable + " " + message));
        }
        // Cancel the event and deactivate the chat event, set boolean to false
        e.setCancelled(true);
        isChatEventActive = false;
    }

    private void rankAction(AsyncPlayerChatEvent e, Player p, String message) {
        // Check the chat message and do the action
        if (message.equalsIgnoreCase("close")) {
            Output.get().sendInfo(new SpigotPlayer(p), messages.getString(MessagesConfig.StringID.spigot_message_ranks_tag_chat_cancelled));
        } else {
            Bukkit.getScheduler().runTask(SpigotPrison.getInstance(), () -> Bukkit.getServer().dispatchCommand(p, "ranks set tag " + tempChatVariable + " " + message));
        }
        // Cancel the event and set the boolean to false, so it can be deactivated.
        e.setCancelled(true);
        isChatEventActive = false;
    }

    /**
     * Save the auto features, and then cancel the event and close the inventory.
     * 
     * @param e
     * @param player
     */
    private boolean saveAutoFeatures( InventoryClickEvent e, Player player ) {
    	boolean success = AutoFeaturesWrapper.getInstance().getAutoFeaturesConfig().saveConf();
    	e.setCancelled(true);
    	player.closeInventory();
    	return success;
    }

    
    private boolean saveConfigBlock(InventoryClickEvent e, Player p) {
    	boolean success = saveAutoFeatures( e, p );
        SpigotAutoBlockGUI gui = new SpigotAutoBlockGUI(p);
        gui.open();
        return success;
    }

    private boolean saveConfigSmelt(InventoryClickEvent e, Player p) {
    	boolean success = saveAutoFeatures( e, p );
        SpigotAutoSmeltGUI gui = new SpigotAutoSmeltGUI(p);
        gui.open();
        return success;
    }

    private boolean saveConfigPickup(InventoryClickEvent e, Player p) {
    	boolean success = saveAutoFeatures( e, p );
        SpigotAutoPickupGUI gui = new SpigotAutoPickupGUI(p);
        gui.open();
        return success;
    }

    private boolean saveConfigAutoFeatures(InventoryClickEvent e, Player p) {
    	boolean success = saveAutoFeatures( e, p );
        SpigotAutoFeaturesGUI gui = new SpigotAutoFeaturesGUI(p);
        gui.open();
        return success;
    }
}
