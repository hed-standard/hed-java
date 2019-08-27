package edu.utsa.tagger.gui;

import edu.utsa.tagger.guisupport.XButton;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class TagEditOptionButton extends XButton {
    public TagEditOptionButton(String text) {
        super(text);
    }
    @Override
    public Font getFont() {
        Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
        fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        return FontsAndColors.contentFont.deriveFont(fontAttributes);
    }

    @Override protected void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        Color fg;
        Color bg;

        if (isPressed() && isHover()) {
            bg = getPressedBg();
            fg = getPressedFg();
        } else if (!isPressed() && isHover()) {
            bg = getHoverBg();
            fg = getHoverFg();
        } else {
            bg = getNormalBg();
            fg = getNormalFg();
        }

        g2d.setColor(bg);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        double x = (getWidth() - g2d.getFontMetrics().stringWidth(getText())) / 2;
        double y = getHeight() / 2 + g2d.getFontMetrics().getHeight() / 4;

        g2d.setFont(getFont());
        g2d.setColor(fg);
        g2d.drawString(getText(), (int) x, (int) y);
    }
    @Override public void mouseEntered(MouseEvent e) {
        if (isEnabled()) {
            setHover(true);
            repaint();
        }
    }

    @Override public void mouseExited(MouseEvent e) {
        setHover(false);
        repaint();
    }

    @Override public void mousePressed(MouseEvent e) {
        if (isEnabled()) {
            setPressed(true);
            repaint();
        }
    }

    @Override public void mouseReleased(MouseEvent e) {
        setPressed(false);
        repaint();
    }

}
