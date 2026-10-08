package com.combattrophies;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Inject;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * The "Trophy Unlocked" toast that slides in from the top of the screen.
 */
public class TrophyToastOverlay extends Overlay
{
	private static final int WIDTH = 320;
	private static final int HEIGHT = 66;
	private static final long SLIDE_MS = 450;

	private final CombatTrophiesConfig config;
	private final Queue<Trophy> queue = new ConcurrentLinkedQueue<>();

	private Trophy current;
	private long shownAt;

	@Inject
	public TrophyToastOverlay(CombatTrophiesConfig config)
	{
		this.config = config;
		setPosition(OverlayPosition.TOP_CENTER);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	public void push(Trophy trophy)
	{
		queue.add(trophy);
	}

	public void clear()
	{
		queue.clear();
		current = null;
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		long now = System.currentTimeMillis();
		if (current == null)
		{
			current = queue.poll();
			if (current == null)
			{
				return null;
			}
			shownAt = now;
		}

		long holdMs = config.popupSeconds() * 1000L;
		long elapsed = now - shownAt;
		if (elapsed >= SLIDE_MS * 2 + holdMs)
		{
			current = null;
			return null;
		}

		double progress;
		if (elapsed < SLIDE_MS)
		{
			progress = ease(elapsed / (double) SLIDE_MS);
		}
		else if (elapsed > SLIDE_MS + holdMs)
		{
			progress = 1 - ease((elapsed - SLIDE_MS - holdMs) / (double) SLIDE_MS);
		}
		else
		{
			progress = 1;
		}

		AffineTransform oldTransform = g.getTransform();
		Composite oldComposite = g.getComposite();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.translate(0, -(1 - progress) * (HEIGHT + 24));
		g.setComposite(AlphaComposite.SrcOver.derive((float) Math.max(0, Math.min(1, progress))));

		TrophyTier tier = current.getTier();

		g.setPaint(new GradientPaint(0, 0, new Color(32, 38, 56, 240), 0, HEIGHT, new Color(12, 16, 26, 240)));
		g.fill(new RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, 14, 14));
		g.setColor(tier.getBase());
		g.setStroke(new BasicStroke(2f));
		g.draw(new RoundRectangle2D.Double(1, 1, WIDTH - 2, HEIGHT - 2, 14, 14));

		g.drawImage(TrophyIcons.create(tier, 46), 12, 10, null);

		int textX = 70;
		int maxText = WIDTH - textX - 12;

		g.setFont(FontManager.getRunescapeSmallFont());
		drawShadowed(g, "TROPHY UNLOCKED", textX, 21, tier.getLight());

		g.setFont(FontManager.getRunescapeBoldFont());
		FontMetrics boldMetrics = g.getFontMetrics();
		drawShadowed(g, fit(current.getName(), boldMetrics, maxText), textX, 39, Color.WHITE);

		g.setFont(FontManager.getRunescapeSmallFont());
		String sub = tier.getTrophyName() + " \u2022 " + tier.getTaskTier() + " \u2022 "
			+ current.getPoints() + (current.getPoints() == 1 ? " point" : " points");
		drawShadowed(g, sub, textX, 56, new Color(190, 196, 210));

		g.setTransform(oldTransform);
		g.setComposite(oldComposite);
		return new Dimension(WIDTH, HEIGHT);
	}

	private static double ease(double t)
	{
		double c = Math.max(0, Math.min(1, t));
		return 1 - Math.pow(1 - c, 3);
	}

	private static void drawShadowed(Graphics2D g, String text, int x, int y, Color color)
	{
		g.setColor(Color.BLACK);
		g.drawString(text, x + 1, y + 1);
		g.setColor(color);
		g.drawString(text, x, y);
	}

	private static String fit(String s, FontMetrics fm, int max)
	{
		if (fm.stringWidth(s) <= max)
		{
			return s;
		}
		String ellipsis = "...";
		while (s.length() > 1 && fm.stringWidth(s + ellipsis) > max)
		{
			s = s.substring(0, s.length() - 1);
		}
		return s + ellipsis;
	}
}
