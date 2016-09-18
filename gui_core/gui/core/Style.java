// License: GPL. For details, see Readme.txt file.
package gui.core;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.io.Serializable;

public class Style implements Serializable /*TALMA add serilizebae*/{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2867934774312343273L;
	private Color color;
    private Color backColor;
    private MyStroke stroke;
    private Font font;

    private static final AlphaComposite TRANSPARENCY = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
    private static final AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC);

    public Style() {
        super();
    }

    public Style(Color color, Color backColor, MyStroke stroke, Font font) {
        super();
        this.color = color;
        this.backColor = backColor;
        this.stroke = stroke;
        this.font = font;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getBackColor() {
        return backColor;
    }

    public void setBackColor(Color backColor) {
        this.backColor = backColor;
    }

    public Stroke getStroke() {
        return stroke.getStroke();
    }

    public void setStroke(MyStroke stroke) {
        this.stroke = stroke;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    private static AlphaComposite getAlphaComposite(Color color) {
        return color.getAlpha() == 255 ? OPAQUE : TRANSPARENCY;
    }

    public AlphaComposite getAlphaComposite() {
        return getAlphaComposite(color);
    }

    public AlphaComposite getBackAlphaComposite() {
        return getAlphaComposite(backColor);
    }
}
