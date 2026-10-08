package com.combattrophies;

import lombok.Value;

@Value
public class Trophy
{
	String name;
	TrophyTier tier;
	int points;
	long unlockedAt;
}
