package com.combattrophies;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(CombatTrophiesConfig.GROUP)
public interface CombatTrophiesConfig extends Config
{
	String GROUP = "combattrophies";

	@ConfigSection(
		name = "Tier totals",
		description = "Optional: how many tasks exist in each tier, so the panel can show progress (x/y).",
		position = 10,
		closedByDefault = true
	)
	String totalsSection = "totals";

	@ConfigItem(keyName = "showPopup", name = "Show unlock popup", description = "Show a trophy popup when a combat task is completed", position = 1)
	default boolean showPopup()
	{
		return true;
	}

	@Range(min = 2, max = 15)
	@Units(Units.SECONDS)
	@ConfigItem(keyName = "popupSeconds", name = "Popup duration", description = "How long the popup stays on screen", position = 2)
	default int popupSeconds()
	{
		return 5;
	}

	@ConfigItem(keyName = "playSound", name = "Play sound", description = "Play a sound effect when a trophy unlocks", position = 3)
	default boolean playSound()
	{
		return true;
	}

	@ConfigItem(keyName = "soundEffectId", name = "Sound effect ID", description = "Game sound effect ID to play (see SoundEffectID in the RuneLite API)", position = 4)
	default int soundEffectId()
	{
		return 2266;
	}

	@ConfigItem(keyName = "notify", name = "Send notification", description = "Also fire a RuneLite notification (respects your notification settings)", position = 5)
	default boolean notifyOnUnlock()
	{
		return false;
	}

	@Range(min = 0, max = 500)
	@ConfigItem(keyName = "totalBronze", name = "Easy (Bronze)", description = "0 hides progress", section = totalsSection, position = 11)
	default int totalBronze()
	{
		return 0;
	}

	@Range(min = 0, max = 500)
	@ConfigItem(keyName = "totalSilver", name = "Medium (Silver)", description = "0 hides progress", section = totalsSection, position = 12)
	default int totalSilver()
	{
		return 0;
	}

	@Range(min = 0, max = 500)
	@ConfigItem(keyName = "totalGold", name = "Hard (Gold)", description = "0 hides progress", section = totalsSection, position = 13)
	default int totalGold()
	{
		return 0;
	}

	@Range(min = 0, max = 500)
	@ConfigItem(keyName = "totalSapphire", name = "Elite (Platinum)", description = "0 hides progress", section = totalsSection, position = 14)
	default int totalSapphire()
	{
		return 0;
	}

	@Range(min = 0, max = 500)
	@ConfigItem(keyName = "totalRuby", name = "Master (Ruby)", description = "0 hides progress", section = totalsSection, position = 15)
	default int totalRuby()
	{
		return 0;
	}

	@Range(min = 0, max = 500)
	@ConfigItem(keyName = "totalAmethyst", name = "Grandmaster (Amethyst)", description = "0 hides progress", section = totalsSection, position = 16)
	default int totalAmethyst()
	{
		return 0;
	}
}
