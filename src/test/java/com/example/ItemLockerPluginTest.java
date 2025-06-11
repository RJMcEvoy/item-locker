package com.example;

import com.itemlocker.ItemLockerPlugin;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ItemLockerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ItemLockerPlugin.class);
		RuneLite.main(args);
	}
}