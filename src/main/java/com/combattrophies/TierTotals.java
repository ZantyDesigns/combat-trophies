package com.combattrophies;

final class TierTotals
{
	private TierTotals()
	{
	}

	static int of(CombatTrophiesConfig config, TrophyTier tier)
	{
		switch (tier)
		{
			case BRONZE:
				return config.totalBronze();
			case SILVER:
				return config.totalSilver();
			case GOLD:
				return config.totalGold();
			case SAPPHIRE:
				return config.totalSapphire();
			case RUBY:
				return config.totalRuby();
			case AMETHYST:
				return config.totalAmethyst();
			default:
				return 0;
		}
	}
}
