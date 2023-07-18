/**
 * 
 */
package org.richfaces.renderkit.html.images;

import java.awt.Color;

/**
 * @author Nick Belaevski
 *         mailto:nbelaevski@exadel.com
 *         created 16.04.2008
 *
 */
public enum GradientType {

    GLASS {

	@Override
	public BiColor getFirstLayerColors(BiColor biColor) {
	    Color bottomColor = biColor.getBottomColor();
	    if (bottomColor != null) {
		float[] hsb = Color.RGBtoHSB(bottomColor.getRed(), bottomColor.getGreen(), bottomColor.getBlue(), null);

		hsb[2] = 0.2f * hsb[2] + 0.80f; //(hsb[2] * 2) / 10 + 80;
		hsb[1] = 0.5f * hsb[1];

		Color topColor = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));

		return new BiColor(bottomColor, topColor);
	    } else {
		return null;
	    }
	}

	@Override
	public BiColor getSecondLayerColors(BiColor biColor) {
	    if (biColor.getBottomColor() != null) {
		return WHITE;
	    } else {
		return null;
	    }
	}
	
    },
    
    PLASTIC {

	@Override
	public BiColor getFirstLayerColors(BiColor biColor) {
	    Color bottomColor = biColor.getBottomColor();
	    if (bottomColor != null) {
		float[] hsb = Color.RGBtoHSB(bottomColor.getRed(), bottomColor.getGreen(), bottomColor.getBlue(), null);

		hsb[2] = 0.25f * hsb[2] + 0.75f; //(100 - hsb[2]) * 0.75f + hsb[2];
		hsb[1] = 0.75f * hsb[1];

		Color topColor = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));

		return new BiColor(topColor, bottomColor);
	    } else {
		return null;
	    }
	}

	@Override
	public BiColor getSecondLayerColors(BiColor biColor) {
	    if (biColor.getBottomColor() != null) {
		return WHITE;
	    } else {
		return null;
	    }
	}
	
    },
    
    PLAIN {

	@Override
	public BiColor getFirstLayerColors(BiColor biColor) {
	    if (biColor.getBottomColor() != null && biColor.getTopColor() != null) {
		return biColor;
	    } else {
		return null;
	    }
	}

	@Override
	public BiColor getSecondLayerColors(BiColor biColor) {
	    return null;
	}
	
    };
    
    public static final GradientType getByParameter(String string) {
	if (string == null || string.length() == 0) {
	    return PLAIN;
	}
	
	return GradientType.valueOf(string.toUpperCase());
    }
    
    public abstract BiColor getFirstLayerColors(BiColor biColor);
    public abstract BiColor getSecondLayerColors(BiColor biColor);
    
    private static final BiColor WHITE = new BiColor(new Color(0xff, 0xff, 0xff, (int) (0.65f * 0xff)),
	    new Color(0xff, 0xff, 0xff, (int) (0.20f * 0xff)));
	
    public static class BiColor {
	private Color topColor;
	private Color bottomColor;

	public BiColor(Color topColor, Color bottomColor) {
	    super();

	    this.topColor = topColor;
	    this.bottomColor = bottomColor;
	}

	public BiColor(Integer topColor, Integer bottomColor) {
	    super();

	    this.topColor = topColor != null ? new Color(topColor.intValue()) : null;
	    this.bottomColor = bottomColor != null ? new Color(bottomColor.intValue()) : null;
	}
	
	public Color getTopColor() {
	    return topColor;
	}

	public Color getBottomColor() {
	    return bottomColor;
	}

	@Override
	public String toString() {
	    return "BiColor: [" + topColor + " -> " + bottomColor + "]";
	}
    };
    
}
