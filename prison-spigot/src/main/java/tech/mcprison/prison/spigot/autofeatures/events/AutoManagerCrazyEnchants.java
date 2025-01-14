package tech.mcprison.prison.spigot.autofeatures.events;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;

import me.badbones69.crazyenchantments.api.events.BlastUseEvent;
import tech.mcprison.prison.Prison;
import tech.mcprison.prison.autofeatures.AutoFeaturesFileConfig.AutoFeatures;
import tech.mcprison.prison.mines.features.MineBlockEvent.BlockEventType;
import tech.mcprison.prison.output.ChatDisplay;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.output.Output.DebugTarget;
import tech.mcprison.prison.spigot.SpigotPrison;
import tech.mcprison.prison.spigot.api.PrisonMinesBlockBreakEvent;
import tech.mcprison.prison.spigot.autofeatures.AutoManagerFeatures;
import tech.mcprison.prison.spigot.block.BlockBreakPriority;
import tech.mcprison.prison.spigot.block.OnBlockBreakExternalEvents;
import tech.mcprison.prison.spigot.block.SpigotBlock;
import tech.mcprison.prison.spigot.game.SpigotHandlerList;
import tech.mcprison.prison.spigot.game.SpigotPlayer;
import tech.mcprison.prison.spigot.integrations.IntegrationCrazyEnchantmentsPickaxes;

public class AutoManagerCrazyEnchants
	extends AutoManagerFeatures
	implements PrisonEventManager
{
	private BlockBreakPriority bbPriority;
	
	private Boolean crazyEnchantEnabled;
	
	public AutoManagerCrazyEnchants() {
		super();
		
		this.crazyEnchantEnabled = null;
	}
	
	
	public BlockBreakPriority getBbPriority() {
		return bbPriority;
	}
	public void setBbPriority( BlockBreakPriority bbPriority ) {
		this.bbPriority = bbPriority;
	}
	
	@Override
	public void registerEvents() {
		
		initialize();
		
	}

		
	public class AutoManagerBlastUseEventListener
		extends AutoManagerCrazyEnchants
		implements Listener {
		
		@EventHandler(priority=EventPriority.NORMAL) 
		public void onCrazyEnchantsBlockExplode( BlastUseEvent e, BlockBreakPriority bbPriority) {
	
			if ( isDisabled( e.getPlayer().getLocation().getWorld().getName() ) ) {
				return;
			}
			
			handleBlastUseEvent( e, bbPriority );
		}
	}
    

	@Override
	public void initialize() {

		String eP = getMessage( AutoFeatures.CrazyEnchantsBlastUseEventPriority );
		setBbPriority( BlockBreakPriority.fromString( eP ) );
		
//		boolean isEventEnabled = eP != null && !"DISABLED".equalsIgnoreCase( eP );
		
		if ( getBbPriority() == BlockBreakPriority.DISABLED ) {
			return;
		}
		
		// Check to see if the class BlastUseEvent even exists:
		try {
			Output.get().logInfo( "AutoManager: checking if loaded: CrazyEnchants" );
			
			Class.forName( "me.badbones69.crazyenchantments.api.events.BlastUseEvent", false, 
					this.getClass().getClassLoader() );
			
			Output.get().logInfo( "AutoManager: Trying to register CrazyEnchants" );
			
			
			SpigotPrison prison = SpigotPrison.getInstance();
			PluginManager pm = Bukkit.getServer().getPluginManager();
			EventPriority ePriority = getBbPriority().getBukkitEventPriority(); 
			
			
			AutoManagerBlastUseEventListener autoManagerlListener = 
					new AutoManagerBlastUseEventListener();
			
			pm.registerEvent(BlastUseEvent.class, autoManagerlListener, ePriority,
					new EventExecutor() {
				public void execute(Listener l, Event e) { 
					
					BlastUseEvent buEvent = (BlastUseEvent) e;
					
					((AutoManagerBlastUseEventListener)l)
									.onCrazyEnchantsBlockExplode( buEvent, getBbPriority() );
				}
			},
					prison);
			prison.getRegisteredBlockListeners().add( autoManagerlListener );
			
			
		}
		catch ( ClassNotFoundException e ) {
			// CrazyEnchants is not loaded... so ignore.
			Output.get().logInfo( "AutoManager: CrazyEnchants is not loaded" );
		}
		catch ( Exception e ) {
			Output.get().logInfo( "AutoManager: CrazyEnchants failed to load. [%s]", e.getMessage() );
		}
	}
   
	
    @Override
    public void unregisterListeners() {
    	
//    	super.unregisterListeners();
    }
	
	@Override
	public void dumpEventListeners() {
		
		StringBuilder sb = new StringBuilder();
		
		dumpEventListeners( sb );
		
		if ( sb.length() > 0 ) {

			
			for ( String line : sb.toString().split( "\n" ) ) {
				
				Output.get().logInfo( line );
			}
		}
		
	}
    
	
	@Override
	public void dumpEventListeners( StringBuilder sb ) {
		
		String eP = getMessage( AutoFeatures.CrazyEnchantsBlastUseEventPriority );
		boolean isEventEnabled = eP != null && !"DISABLED".equalsIgnoreCase( eP );

		if ( !isEventEnabled ) {
			return;
		}
		
		// Check to see if the class BlastUseEvent even exists:
		try {
			
			Class.forName( "me.badbones69.crazyenchantments.api.events.BlastUseEvent", false, 
					this.getClass().getClassLoader() );
			
    		BlockBreakPriority bbPriority = BlockBreakPriority.fromString( eP );

			
			String title = String.format( 
					"BlastUseEvent (%s)", 
					( bbPriority == null ? "--none--" : bbPriority.name()) );
			
			ChatDisplay eventDisplay = Prison.get().getPlatform().dumpEventListenersChatDisplay( 
					title, 
					new SpigotHandlerList( BlastUseEvent.getHandlerList()) );
			
			if ( eventDisplay != null ) {
				sb.append( eventDisplay.toStringBuilder() );
				sb.append( "\n" );
			}
		}
		catch ( ClassNotFoundException e ) {
			// CrazyEnchants is not loaded... so ignore.
		}
		catch ( Exception e ) {
			Output.get().logInfo( "AutoManager: CrazyEnchants failed to load. [%s]", e.getMessage() );
		}
	}
	
	/**
	 * <p>Since there are multiple blocks associated with this event, pull out the player first and
	 * get the mine, then loop through those blocks to make sure they are within the mine.
	 * </p>
	 * 
	 * <p>The logic in this function is slightly different compared to genericBlockEvent() because this
	 * event contains multiple blocks so it's far more efficient to process the player data once. 
	 * So that basically needed a slight refactoring.
	 * </p>
	 * 
	 * @param e
	 */
	public void handleBlastUseEvent( BlastUseEvent e, BlockBreakPriority bbPriority ) {
			
//			boolean monitor, boolean blockEventsOnly, 
//			boolean autoManager ) {

		long start = System.nanoTime();
		
    	if ( e.isCancelled() ||  ignoreMinesBlockBreakEvent( e, e.getPlayer(), e.getBlockList().get( 0 )) ) {
    		return;
    	}
    	
		
		// Register all external events such as mcMMO and EZBlocks:
		OnBlockBreakExternalEvents.getInstance().registerAllExternalEvents();
				
		StringBuilder debugInfo = new StringBuilder();
		
		debugInfo.append( String.format( "### ** genericBlastUseEvent ** ### " +
				"(event: BlastUseEvent, config: %s, priority: %s, canceled: %s) ",
				bbPriority.name(),
				bbPriority.getBukkitEventPriority().name(),
				(e.isCancelled() ? "TRUE " : "FALSE")
				) );
		
		
		// NOTE that check for auto manager has happened prior to accessing this function.
    	if ( bbPriority != BlockBreakPriority.MONITOR && !e.isCancelled() || bbPriority == BlockBreakPriority.MONITOR &&
    			e.getBlockList().size() > 0 ) {

			
	    	String eP = getMessage( AutoFeatures.CrazyEnchantsBlastUseEventPriority );
			boolean isCEBlockExplodeEnabled = eP != null && !"DISABLED".equalsIgnoreCase( eP );

			
    		Block bukkitBlock = e.getBlockList().get( 0 );
    		
    		// Need to wrap in a Prison block so it can be used with the mines:
    		SpigotBlock sBlock = SpigotBlock.getSpigotBlock( bukkitBlock );
    		SpigotPlayer sPlayer = new SpigotPlayer(e.getPlayer());
    		
    		BlockEventType eventType = BlockEventType.CEXplosion;
    		String triggered = null;
    		

    		PrisonMinesBlockBreakEvent pmEvent = new PrisonMinesBlockBreakEvent( bukkitBlock, e.getPlayer(),
					sBlock, sPlayer, bbPriority, eventType, triggered );
		
    		
    		for ( int i = 1; i < e.getBlockList().size(); i++ ) {
    			pmEvent.getUnprocessedRawBlocks().add( e.getBlockList().get( i ) );
    		}
    		
    		
    		if ( !validateEvent( pmEvent, debugInfo ) ) {
    			
    			// The event has not passed validation. All logging and Errors have been recorded
    			// so do nothing more. This is to just prevent normal processing from occurring.
    			
    			if ( pmEvent.isCancelOriginalEvent() ) {
    				
    				e.setCancelled( true );
    			}
    		}

    		
    		else if ( pmEvent.getBbPriority() == BlockBreakPriority.MONITOR ) {
    			// Stop here, and prevent additional processing. Monitors should never process the event beyond this.
    		}
    		


    		// now process all blocks (non-monitor):
    		else if ( isCEBlockExplodeEnabled && 
    				( pmEvent.getMine() != null || pmEvent.getMine() == null && !isBoolean( AutoFeatures.pickupLimitToMines )) ) {


    			if ( pmEvent.getExplodedBlocks().size() > 0 ) {
    				
//					String triggered = null;
    				
					
					// Warning: BlastUseEvent does not identify the block the player actually hit, so the dummyBlock
					//          is just a random first block from the explodedBlocks list and may not be the block
					//          that initiated the explosion event.
//					SpigotBlock dummyBlock = explodedBlocks.get( 0 );
					
//	    			PrisonMinesBlockBreakEvent pmbbEvent = new PrisonMinesBlockBreakEvent( dummyBlock.getWrapper(), e.getPlayer(),
//	    												mine, dummyBlock, explodedBlocks, BlockEventType.CEXplosion, triggered );
	                Bukkit.getServer().getPluginManager().callEvent(pmEvent);
	                if ( pmEvent.isCancelled() ) {
	                	debugInfo.append( "(normal processing: PrisonMinesBlockBreakEvent was canceled) " );
	                }
	                else {
	                	
//	                	// Cancel drops if so configured:
//	                	if ( isBoolean( AutoFeatures.cancelAllBlockEventBlockDrops ) ) {
//	                		
//	                		try
//	                		{
//	                			e.setDropItems( false );
//	                		}
//	                		catch ( NoSuchMethodError e1 )
//	                		{
//	                			String message = String.format( 
//	                					"Warning: The autoFeaturesConfig.yml setting `cancelAllBlockEventBlockDrops` " +
//	                					"is not valid for this version of Spigot. Modify the config settings and set " +
//	                					"this value to `false`. [%s]",
//	                					e1.getMessage() );
//	                			Output.get().logWarn( message );
//	                		}
//	                	}
	                	
	                	if ( doAction( pmEvent, debugInfo ) ) {
	                		
	                		if ( isBoolean( AutoFeatures.cancelAllBlockBreakEvents ) ) {
	                			
	                			e.setCancelled( true );
	                		}
	                		else {
	                			
	                			debugInfo.append( "(event was not canceled) " );
	                		}
	                		
	                		finalizeBreakTheBlocks( pmEvent );
	                		
	                		doBlockEvents( pmEvent );

	                	}
	                	
	                	else {
	                		
	                		debugInfo.append( "(doAction failed without details) " );
	                	}
	                	
	                }
    			}
    			

    			debugInfo.append( "(normal processing) " );
    		}
    		else {
    			
    			debugInfo.append( "(logic bypass) " );
    		}

		}
    	
		if ( debugInfo.length() > 0 ) {
			
			long stop = System.nanoTime();
			debugInfo.append( " [" ).append( (stop - start) / 1000000d ).append( " ms]" );
			
			Output.get().logDebug( DebugTarget.blockBreak, debugInfo.toString() );
		}
	}

	@Override
	protected int checkBonusXp( Player player, Block block, ItemStack item ) {
		int bonusXp = 0;
		
		try {
			if ( isCrazyEnchantEnabled() == null ) {
				Class.forName( 
						"tech.mcprison.prison.spigot.integrations.IntegrationCrazyEnchantmentsPickaxes", false, 
						this.getClass().getClassLoader() );
				setCrazyEnchantEnabled( Boolean.TRUE );
			}
			
			if ( isCrazyEnchantEnabled() != null && isCrazyEnchantEnabled().booleanValue() && 
					item != null && IntegrationCrazyEnchantmentsPickaxes.getInstance().isEnabled() ) {
				
				bonusXp = IntegrationCrazyEnchantmentsPickaxes.getInstance()
						.getPickaxeEnchantmentExperienceBonus( player, block, item );
			}
		}
		catch ( NoClassDefFoundError | Exception e ) {
			setCrazyEnchantEnabled( Boolean.FALSE );
		}
		
		return bonusXp;
	}
	
	public Boolean isCrazyEnchantEnabled() {
		return crazyEnchantEnabled;
	}
	public void setCrazyEnchantEnabled( Boolean crazyEnchantEnabled ) {
		this.crazyEnchantEnabled = crazyEnchantEnabled;
	}
	
}
