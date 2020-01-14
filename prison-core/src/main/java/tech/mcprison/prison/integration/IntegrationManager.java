package tech.mcprison.prison.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The IntegrationManager stores instances of each {@link Integration} and allows
 * them to be registered and retrieved.
 *
 * @author Faizaan A. Datoo
 * @since API 1.0
 */
public class IntegrationManager {

    private Map<IntegrationType, List<Integration>> integrations;

    public IntegrationManager() {
        this.integrations = new HashMap<>();
    }

    /**
     * Returns a list of all of the {@link Integration}s that are registered under a certain {@link IntegrationType}, if any.
     * This includes integrations that have not successfully integrated.
     * If there are none, an empty list will be returned.
     *
     * @param type The desired {@link IntegrationType}.
     * @return A list.
     */
    public List<Integration> getAllForType(IntegrationType type) {
        return integrations.getOrDefault(type, Collections.emptyList());
    }

    /**
     * Returns an optional containing the first working {@link Integration} for the specified {@link IntegrationType}.
     * If there are no working integrations, the optional will be empty.
     *
     * @param type The desired {@link IntegrationType}.
     * @return An optional containing the first working integration, or empty if none are found.
     */
    public Optional<Integration> getForType(IntegrationType type) {
        if(!integrations.containsKey(type)) {
            return Optional.empty();
        }
        return integrations.get(type).stream().filter(Integration::hasIntegrated).findFirst();
    }

    /**
     * Returns true if there are any working {@link Integration}s registered for a specific {@link IntegrationType}.
     *
     * @param type The desired {@link IntegrationType}.
     * @return true if there is any working {@link Integration} registered, false otherwise.
     */
    public boolean hasForType(IntegrationType type) {
        return getForType(type).isPresent();
    }

    /**
     * Registers an {@link Integration}.
     * @param i The {@link Integration}.
     */
    public void register(Integration i) {
    	IntegrationType iType = i.getType();
    	if ( !integrations.containsKey( iType ) ) {
    		integrations.put(iType, new ArrayList<>());
    	}
    	integrations.get(iType).add(i);
    }

    
    /**
     * <p>This function formats all the Integrations, both active and inactive, to be displayed
     * to a user, or sent to the server logs.  This function returns a List to provide a 
     * degree of flexibility in where it is being used, without this container 
     * (The integration manager) having to know where the data will be used. This function 
     * keeps the business logic of relationship of integrations to Integration Types 
     * internal so as to no leak that knowledge out of this function. 
     * </p>
     * 
     * @return
     */
    public List<String> toStrings() {
    	List<String> results = new ArrayList<>();
    	
        for ( IntegrationType integType : IntegrationType.values() )
		{
			results.add( String.format( "&7Integration Type: &3%s", integType.name() ) );
			
			List<Integration> plugins = getAllForType( integType );
			
			if ( plugins == null || plugins.size() == 0 ) {
				results.add( "    &e&onone" );
			} else {
				for ( Integration plugin : plugins )
				{
					results.add( String.format( "    &a%s &7<%s&7>", plugin.getProviderName(),
							( plugin.hasIntegrated() ? "&aActive" : "&cInactive")) );
				}
			}
		}
    	
    	return results;
    }

}
