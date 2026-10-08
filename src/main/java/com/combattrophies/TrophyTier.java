package com.combattrophies;

import java.awt.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Maps each Combat Achievement tier onto a PlayStation-style trophy grade.
 */
@Getter
@RequiredArgsConstructor
public enum TrophyTier
{
	BRONZE("Bronze", "Easy", new Color(205, 127, 50), new Color(246, 182, 120), new Color(122, 70, 22)),
	SILVER("Silver", "Medium", new Color(190, 192, 202), new Color(240, 242, 250), new Color(104, 106, 122)),
	GOLD("Gold", "Hard", new Color(255, 200, 40), new Color(255, 238, 140), new Color(160, 108, 0)),
	SAPPHIRE("Sapphire", "Elite", new Color(15, 82, 186), new Color(137, 207, 240), new Color(8, 37, 103)),
	RUBY("Ruby", "Master", new Color(222, 52, 74), new Color(255, 150, 160), new Color(116, 14, 30)),
	AMETHYST("Amethyst", "Grand\nmaster", new Color(172, 92, 232), new Color(224, 176, 255), new Color(78, 28, 130)),
	PLATINUM("Platinum", "Platinum", new Color(120, 190, 235), new Color(215, 240, 255), new Color(50, 92, 150));

	private final String trophyName;
	private final String taskTier;
	private final Color base;
	private final Color light;
	private final Color dark;

	public static TrophyTier fromTaskTier(String name)
	{
		for (TrophyTier tier : values())
		{
			if (tier.taskTier.equalsIgnoreCase(name))
			{
				return tier;
			}
		}
		return null;
	}
}
