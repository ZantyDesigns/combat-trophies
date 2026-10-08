package com.combattrophies;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Combat Trophies",
	description = "A PlayStation-style trophy tracker for Combat Achievements",
	tags = {"combat", "achievements", "trophy", "trophies", "ca", "tracker"}
)
public class CombatTrophiesPlugin extends Plugin
{
	private static final String TROPHIES_KEY = "trophies";
	private static final Type TROPHY_LIST = new TypeToken<List<Trophy>>()
	{
	}.getType();

	// e.g. "Congratulations, you've completed a hard combat task: Perfect Zulrah (2 points)."
	private static final Pattern TASK_PATTERN =
		Pattern.compile("completed an? (\\w+) combat task: (.+?) \\((\\d+) points?\\)");

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Notifier notifier;

	@Inject
	private Gson gson;

	@Inject
	private CombatTrophiesConfig config;

	@Inject
	private TrophyToastOverlay toastOverlay;

	private CombatTrophiesPanel panel;
	private NavigationButton navButton;

	private final List<Trophy> trophies = new ArrayList<>();

	@Override
	protected void startUp()
	{
		panel = new CombatTrophiesPanel(config, this::clearTrophies);

		navButton = NavigationButton.builder()
			.tooltip("Combat Trophies")
			.icon(TrophyIcons.create(TrophyTier.GOLD, 16))
			.priority(7)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
		overlayManager.add(toastOverlay);

		clientThread.invokeLater(this::loadTrophies);
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		overlayManager.remove(toastOverlay);
		toastOverlay.clear();
		synchronized (trophies)
		{
			trophies.clear();
		}
		panel = null;
		navButton = null;
	}

	@Provides
	CombatTrophiesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CombatTrophiesConfig.class);
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		loadTrophies();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (CombatTrophiesConfig.GROUP.equals(event.getGroup()))
		{
			refreshPanel();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		Matcher matcher = TASK_PATTERN.matcher(Text.removeTags(event.getMessage()));
		if (!matcher.find())
		{
			return;
		}

		TrophyTier tier = TrophyTier.fromTaskTier(matcher.group(1));
		if (tier == null)
		{
			return;
		}

		unlock(matcher.group(2).trim(), tier, Integer.parseInt(matcher.group(3)));
	}

	private void unlock(String name, TrophyTier tier, int points)
	{
		Trophy trophy = new Trophy(name, tier, points, System.currentTimeMillis());

		synchronized (trophies)
		{
			for (Trophy existing : trophies)
			{
				if (existing.getName().equalsIgnoreCase(name))
				{
					return; // already tracked
				}
			}
			trophies.add(trophy);
		}

		save();
		refreshPanel();

		if (config.showPopup())
		{
			toastOverlay.push(trophy);
		}
		if (config.playSound())
		{
			client.playSoundEffect(config.soundEffectId());
		}
		if (config.notifyOnUnlock())
		{
			notifier.notify("Trophy unlocked: " + name + " (" + tier.getTrophyName() + ")");
		}
	}

	private void loadTrophies()
	{
		List<Trophy> loaded = new ArrayList<>();

		if (client.getGameState() == GameState.LOGGED_IN && configManager.getRSProfileKey() != null)
		{
			String json = configManager.getRSProfileConfiguration(CombatTrophiesConfig.GROUP, TROPHIES_KEY);
			if (json != null && !json.isEmpty())
			{
				try
				{
					List<Trophy> parsed = gson.fromJson(json, TROPHY_LIST);
					if (parsed != null)
					{
						for (Trophy t : parsed)
						{
							if (t != null && t.getName() != null && t.getTier() != null)
							{
								loaded.add(t);
							}
						}
					}
				}
				catch (JsonParseException e)
				{
					log.warn("Could not read saved trophies", e);
				}
			}
		}

		synchronized (trophies)
		{
			trophies.clear();
			trophies.addAll(loaded);
		}
		refreshPanel();
	}

	private void save()
	{
		if (configManager.getRSProfileKey() == null)
		{
			return;
		}

		String json;
		synchronized (trophies)
		{
			json = gson.toJson(trophies, TROPHY_LIST);
		}
		configManager.setRSProfileConfiguration(CombatTrophiesConfig.GROUP, TROPHIES_KEY, json);
	}

	private void clearTrophies()
	{
		synchronized (trophies)
		{
			trophies.clear();
		}
		if (configManager.getRSProfileKey() != null)
		{
			configManager.unsetRSProfileConfiguration(CombatTrophiesConfig.GROUP, TROPHIES_KEY);
		}
		refreshPanel();
	}

	private void refreshPanel()
	{
		if (panel == null)
		{
			return;
		}
		List<Trophy> snapshot;
		synchronized (trophies)
		{
			snapshot = new ArrayList<>(trophies);
		}
		panel.update(snapshot);
	}
}
