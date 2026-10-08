package com.combattrophies;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Draws the trophy cup procedurally so the plugin needs no image assets.
 */
public final class TrophyIcons
{
	private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();

	private TrophyIcons()
	{
	}

	public static BufferedImage create(TrophyTier tier, int size)
	{
		return CACHE.computeIfAbsent(tier.name() + ":" + size, k -> draw(tier, size));
	}

	private static BufferedImage draw(TrophyTier tier, int size)
	{
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.scale(size / 100.0, size / 100.0);

		Color base = tier.getBase();
		Color light = tier.getLight();
		Color dark = tier.getDark();

		// handles
		g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(dark);
		g.draw(new Arc2D.Double(8, 14, 30, 32, 90, 180, Arc2D.OPEN));
		g.draw(new Arc2D.Double(62, 14, 30, 32, 270, 180, Arc2D.OPEN));

		// stem + base
		g.setPaint(new LinearGradientPaint(0, 0, 100, 0, new float[]{0f, 0.4f, 1f}, new Color[]{base, light, dark}));
		g.fill(new RoundRectangle2D.Double(43, 62, 14, 16, 4, 4));
		g.setColor(dark);
		g.fill(new RoundRectangle2D.Double(28, 80, 44, 10, 5, 5));
		g.setColor(base);
		g.fill(new RoundRectangle2D.Double(32, 82, 36, 4, 3, 3));

		// bowl
		Path2D bowl = new Path2D.Double();
		bowl.moveTo(22, 10);
		bowl.lineTo(78, 10);
		bowl.curveTo(78, 48, 67, 64, 50, 66);
		bowl.curveTo(33, 64, 22, 48, 22, 10);
		bowl.closePath();
		g.setPaint(new LinearGradientPaint(22, 0, 78, 0, new float[]{0f, 0.35f, 1f}, new Color[]{base, light, dark}));
		g.fill(bowl);
		g.setStroke(new BasicStroke(2f));
		g.setColor(dark);
		g.draw(bowl);

		// shine
		g.setColor(new Color(255, 255, 255, 110));
		g.fill(new Ellipse2D.Double(29, 16, 8, 26));

		g.dispose();
		return img;
	}
}
