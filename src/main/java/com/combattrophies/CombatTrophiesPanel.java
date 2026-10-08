package com.combattrophies;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;

/**
 * The "trophy case" side panel.
 */
public class CombatTrophiesPanel extends PluginPanel
{
	private static final String ALL = "All";
	private static final DateTimeFormatter DATE_FORMAT =
		DateTimeFormatter.ofPattern("d MMM yyyy").withZone(ZoneId.systemDefault());

	private final CombatTrophiesConfig config;
	private final Runnable onReset;

	private final JLabel summaryLabel = new JLabel("No trophies yet");
	private final Map<TrophyTier, JLabel> tierCells = new EnumMap<>(TrophyTier.class);
	private final JComboBox<String> tierFilter = new JComboBox<>();
	private final IconTextField searchBar = new IconTextField();
	private final JPanel listPanel = new JPanel();

	private List<Trophy> trophies = new ArrayList<>();

	CombatTrophiesPanel(CombatTrophiesConfig config, Runnable onReset)
	{
		super(false);
		this.config = config;
		this.onReset = onReset;

		setLayout(new BorderLayout(0, 8));
		setBorder(new EmptyBorder(8, 8, 8, 8));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.setOpaque(false);

		JLabel title = new JLabel("Combat Trophies");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		top.add(wrap(title));

		summaryLabel.setFont(FontManager.getRunescapeSmallFont());
		summaryLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		top.add(wrap(summaryLabel));
		top.add(Box.createVerticalStrut(8));

		JPanel grid = new JPanel(new GridLayout(2, 3, 4, 4));
		grid.setOpaque(false);
		for (TrophyTier tier : TrophyTier.values())
		{
			JLabel cell = new JLabel();
			cell.setOpaque(true);
			cell.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			cell.setHorizontalAlignment(SwingConstants.CENTER);
			cell.setHorizontalTextPosition(SwingConstants.CENTER);
			cell.setVerticalTextPosition(SwingConstants.BOTTOM);
			cell.setIcon(new ImageIcon(TrophyIcons.create(tier, 28)));
			cell.setForeground(tier.getBase());
			cell.setFont(FontManager.getRunescapeSmallFont());
			cell.setBorder(new EmptyBorder(6, 2, 6, 2));
			cell.setToolTipText(tier.getTrophyName() + " (" + tier.getTaskTier() + " tasks)");
			tierCells.put(tier, cell);
			grid.add(cell);
		}
		top.add(wrap(grid));
		top.add(Box.createVerticalStrut(8));

		tierFilter.addItem(ALL);
		for (TrophyTier tier : TrophyTier.values())
		{
			tierFilter.addItem(tier.getTrophyName());
		}
		tierFilter.addActionListener(e -> rebuildList());
		top.add(wrap(tierFilter));
		top.add(Box.createVerticalStrut(6));

		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 16, 30));
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		searchBar.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				rebuildList();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				rebuildList();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				rebuildList();
			}
		});
		searchBar.addClearListener(this::rebuildList);
		top.add(wrap(searchBar));

		add(top, BorderLayout.NORTH);

		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JPanel listWrapper = new JPanel(new BorderLayout());
		listWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		listWrapper.add(listPanel, BorderLayout.NORTH);

		JScrollPane scroll = new JScrollPane(listWrapper);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		add(scroll, BorderLayout.CENTER);

		JButton reset = new JButton("Reset trophy data");
		reset.setFocusable(false);
		reset.addActionListener(e ->
		{
			int result = JOptionPane.showConfirmDialog(this,
				"Delete all tracked trophies for this account?",
				"Reset Combat Trophies", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.YES_OPTION)
			{
				onReset.run();
			}
		});
		add(reset, BorderLayout.SOUTH);

		refreshSummary();
		rebuildList();
	}

	public void update(List<Trophy> data)
	{
		SwingUtilities.invokeLater(() ->
		{
			trophies = new ArrayList<>(data);
			refreshSummary();
			rebuildList();
		});
	}

	private void refreshSummary()
	{
		Map<TrophyTier, Integer> counts = new EnumMap<>(TrophyTier.class);
		int points = 0;
		for (Trophy t : trophies)
		{
			counts.merge(t.getTier(), 1, Integer::sum);
			points += t.getPoints();
		}

		for (TrophyTier tier : TrophyTier.values())
		{
			int count = counts.getOrDefault(tier, 0);
			int total = totalFor(tier);
			String value = total > 0 ? count + "/" + total : String.valueOf(count);
			tierCells.get(tier).setText("<html><center>" + value + "<br>" + tier.getTaskTier() + "</center></html>");
		}

		summaryLabel.setText(trophies.size() + (trophies.size() == 1 ? " trophy" : " trophies") + " \u2022 " + points + " points");
	}

	private int totalFor(TrophyTier tier)
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

	private void rebuildList()
	{
		listPanel.removeAll();

		String query = searchBar.getText().trim().toLowerCase();
		String selected = (String) tierFilter.getSelectedItem();

		List<Trophy> sorted = new ArrayList<>(trophies);
		sorted.sort(Comparator.comparingLong(Trophy::getUnlockedAt).reversed());

		int shown = 0;
		for (Trophy trophy : sorted)
		{
			if (selected != null && !ALL.equals(selected) && !trophy.getTier().getTrophyName().equals(selected))
			{
				continue;
			}
			if (!query.isEmpty() && !trophy.getName().toLowerCase().contains(query))
			{
				continue;
			}
			listPanel.add(createRow(trophy));
			listPanel.add(Box.createVerticalStrut(4));
			shown++;
		}

		if (shown == 0)
		{
			JLabel empty = new JLabel("<html><center>No trophies to show.<br>Complete a combat task to unlock one!</center></html>");
			empty.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			empty.setFont(FontManager.getRunescapeSmallFont());
			empty.setHorizontalAlignment(SwingConstants.CENTER);
			empty.setBorder(new EmptyBorder(20, 0, 0, 0));
			listPanel.add(wrap(empty));
		}

		listPanel.revalidate();
		listPanel.repaint();
	}

	private JPanel createRow(Trophy trophy)
	{
		JPanel row = new JPanel(new BorderLayout(8, 0));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(new EmptyBorder(6, 6, 6, 6));

		JLabel icon = new JLabel(new ImageIcon(TrophyIcons.create(trophy.getTier(), 28)));
		row.add(icon, BorderLayout.WEST);

		JPanel text = new JPanel(new GridLayout(2, 1));
		text.setOpaque(false);

		JLabel name = new JLabel(trophy.getName());
		name.setFont(FontManager.getRunescapeBoldFont());
		name.setForeground(Color.WHITE);
		name.setToolTipText(trophy.getName());

		JLabel sub = new JLabel(trophy.getTier().getTrophyName() + " \u2022 " + trophy.getPoints() + " pts \u2022 "
			+ DATE_FORMAT.format(Instant.ofEpochMilli(trophy.getUnlockedAt())));
		sub.setFont(FontManager.getRunescapeSmallFont());
		sub.setForeground(trophy.getTier().getBase());

		text.add(name);
		text.add(sub);
		row.add(text, BorderLayout.CENTER);
		return row;
	}

	private static JPanel wrap(Component c)
	{
		JPanel p = new JPanel(new BorderLayout());
		p.setOpaque(false);
		p.setAlignmentX(Component.LEFT_ALIGNMENT);
		p.add(c, BorderLayout.CENTER);
		return p;
	}
}
