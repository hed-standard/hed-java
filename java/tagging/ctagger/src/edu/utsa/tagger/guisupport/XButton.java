package edu.utsa.tagger.guisupport;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

@SuppressWarnings("serial")
public abstract class XButton extends JButton implements MouseListener {
	
	private String text;
	
	private boolean hover = false;
	private boolean pressed = false;
	
	private Color normalBg = null;
	private Color normalFg = null;
	private Color hoverBg = null;
	private Color hoverFg = null;
	private Color pressedBg = null;
	private Color pressedFg = null;
	
	public XButton(String textArg) {
		text = textArg;
		addMouseListener(this);
	}
	public XButton(Icon icon) {
		super(icon);
		addMouseListener(this);
	}
	
	public abstract Font getFont();
	
	public String getText() {
		return text;
	}
	
	@Override public void mouseClicked(MouseEvent e) {
	}

	@Override public void mouseEntered(MouseEvent e) {
//		if (isEnabled()) {
//			hover = true;
//			repaint();
//		}
	}

	@Override public void mouseExited(MouseEvent e) {
//		hover = false;
//		repaint();
	}

	@Override public void mousePressed(MouseEvent e) {
//		if (isEnabled()) {
//			pressed = true;
//			repaint();
//		}
	}

	@Override public void mouseReleased(MouseEvent e) {
//		pressed = false;
//		repaint();
	}
	
//	@Override protected void paintComponent(Graphics g) {
//
//		Graphics2D g2d = (Graphics2D) g;
//		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
//
//		Color fg;
//		Color bg;
//
//		if (pressed && hover) {
//			bg = pressedBg;
//			fg = pressedFg;
//		} else if (!pressed && hover) {
//			bg = hoverBg;
//			fg = hoverFg;
//		} else {
//			bg = normalBg;
//			fg = normalFg;
//		}
//
//		g2d.setColor(bg);
//		g2d.fillRect(0, 0, getWidth(), getHeight());
//
//		double x = (getWidth() - g2d.getFontMetrics().stringWidth(text)) / 2;
//		double y = getHeight() / 2 + g2d.getFontMetrics().getHeight() / 4;
//
//		g2d.setFont(getFont());
//		g2d.setColor(fg);
//		g2d.drawString(text, (int) x, (int) y);
//
//	}
	
	public void setHoverBackground(Color hoverBgArg) {
		hoverBg = hoverBgArg;
	}

	public void setHoverForeground(Color hoverFgArg) {
		hoverFg = hoverFgArg;
	}

	public void setNormalBackground(Color normalBgArg) {
		normalBg = normalBgArg;
	}
	
	public void setNormalForeground(Color normalFgArg) {
		normalFg = normalFgArg;
	}
	
	public void setPressedBackground(Color pressedBgArg) {
		pressedBg = pressedBgArg;
	}
	
	public void setPressedForeground(Color pressedFgArg) {
		pressedFg = pressedFgArg;
	}
	
	public void setText(String textArg) {
		text = textArg;
	}

	public boolean isHover() {
		return hover;
	}

	public void setHover(boolean hover) {
		this.hover = hover;
	}

	public boolean isPressed() {
		return pressed;
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	public Color getNormalBg() {
		return normalBg;
	}

	public void setNormalBg(Color normalBg) {
		this.normalBg = normalBg;
	}

	public Color getNormalFg() {
		return normalFg;
	}

	public void setNormalFg(Color normalFg) {
		this.normalFg = normalFg;
	}

	public Color getHoverBg() {
		return hoverBg;
	}

	public void setHoverBg(Color hoverBg) {
		this.hoverBg = hoverBg;
	}

	public Color getHoverFg() {
		return hoverFg;
	}

	public void setHoverFg(Color hoverFg) {
		this.hoverFg = hoverFg;
	}

	public Color getPressedBg() {
		return pressedBg;
	}

	public void setPressedBg(Color pressedBg) {
		this.pressedBg = pressedBg;
	}

	public Color getPressedFg() {
		return pressedFg;
	}

	public void setPressedFg(Color pressedFg) {
		this.pressedFg = pressedFg;
	}
}
