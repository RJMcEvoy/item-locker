package com.itemlocker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface ItemLockerConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default String greeting()
	{
		return "Hello";
	}
    
    @ConfigItem(
		keyName = "lockedItemsString",
		name = "Locked Items",
		description = "Comma-separated list of locked item ids"
	)
    default String lockedItemsString()
	{
		return "1359";
	}
}
