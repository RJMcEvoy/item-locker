package com.itemlocker;

import com.google.inject.Provides;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Item Locker"
)
public class ItemLockerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ItemLockerConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Provides
	ItemLockerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ItemLockerConfig.class);
	}

    @Inject
    private ItemManager itemManager;

    /**
	 * Normalize a MenuEntryAdded into the base item ID.
	 */
	private int getItemId(MenuEntryAdded event, MenuEntry entry)
	{
		int raw = Math.max(event.getItemId(), entry.getItemId());
		return itemManager.canonicalize(raw);
	}


    /**
	 * Returns true if the entry appears to be for a ground item.
	 */
	private boolean isGroundItem(MenuEntry entry) {
		String option = Text.removeTags(entry.getOption()).toLowerCase();
		MenuAction action = entry.getType();
		return action.toString().contains("GROUND_ITEM")
				|| option.contains("take")
				|| option.contains("pick-up")
				|| option.contains("pickup");
	}

	private static final Set<MenuAction> disabledActions = EnumSet.of(
			MenuAction.CC_OP,               // inventory “Use” on locked
			MenuAction.WIDGET_TARGET,       // “Use” on widgets
			MenuAction.WIDGET_TARGET_ON_WIDGET  // “Use” on widget -> widget
	);

    private boolean isUnlocked(int id) {
        String[] lockedItems = config.lockedItemsString().split(",");
        // return whether the id is in the list of locked_items
        for (String lockedItem : lockedItems) {
            if (id == Integer.parseInt(lockedItem)) {
                return false;
            }
        }
        return true;
    }


    /**
	 * This method handles non-ground items (or any other cases) by checking if the item is enabled.
	 * It returns true if the action should be allowed.
	 */
	private boolean isEnabled(int id, MenuEntry entry, MenuAction action) {
		String option = Text.removeTags(entry.getOption());
		String target = Text.removeTags(entry.getTarget());

		// Always allow "Drop"
		if (option.equalsIgnoreCase("drop"))
			return true;
		if (option.equalsIgnoreCase("clean") || option.equalsIgnoreCase("rub"))
		{
			return isUnlocked(id);
		}


		boolean enabled;
        
        enabled = !disabledActions.contains(action);

		if (enabled)
			return true;
		if (id == 0 || id == -1 )
			return true;
		return isUnlocked(id);
	}


    @Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event) {
		MenuEntry entry = event.getMenuEntry();
		MenuAction action = entry.getType();
		int id = getItemId(event, entry);
		boolean enabled;
		// Check if the entry looks like it's for a ground item.
		if (isGroundItem(entry)) {
			return;
		} else {
			enabled = isEnabled(id, entry, action);
		}
		// If not enabled, grey out the text and set the click handler to DISABLED.
		if (!enabled) {
			String option = Text.removeTags(entry.getOption());
			String target = Text.removeTags(entry.getTarget());
			entry.setOption("<col=808080>" + option);
			entry.setTarget("<col=808080>" + target);
			entry.onClick(DISABLED);
		}
	}
    
    private final Consumer<MenuEntry> DISABLED = e -> { };

    @Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		// If the entry is disabled, consume the event.
		if (event.getMenuEntry().onClick() == DISABLED) {
			event.consume();
			return;
		}
		
	}
}
