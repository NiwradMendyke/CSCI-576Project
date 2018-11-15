package finalproject;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class DrawableLabel extends JLabel implements MouseListener, MouseMotionListener {

	Rectangle rect; 
	Point mouseClick;

	int x, y;

	public DrawableLabel(String name, int orientation) {
		super(name, orientation);

		addMouseListener(this);
    	addMouseMotionListener(this);
	}

	public void mouseClicked(MouseEvent e) { }

  	public void mouseEntered(MouseEvent e) { }

  	public void mouseExited(MouseEvent e) { }

	public void mousePressed(MouseEvent e) {
		if (getIcon() != null) {
			if (rect != null && rect.contains(e.getPoint())) {
				mouseClick = e.getPoint();
			}
			else {
				rect = new Rectangle(e.getPoint());
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		paintRectangle(e);
	}

	public void mouseDragged(MouseEvent e) {
		paintRectangle(e);
	}

	public void mouseMoved(MouseEvent e) { }

	private void paintRectangle(MouseEvent e) {
		if (getIcon() != null) {
			if (rect != null && rect.contains(e.getPoint())) {
				int xdist = e.getX() - (int)mouseClick.getX();
				int ydist = e.getY() - (int)mouseClick.getY();
				rect.setLocation((int)rect.getX() + xdist, (int)rect.getY() + ydist);
				mouseClick = e.getPoint();
			}
			else {
				rect.setSize(e.getX() - (int)rect.getX(), e.getY() - (int)rect.getY());
			}
			repaint();
		}
	}

	@Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	if (rect != null) {
    		g.setColor(Color.magenta);
    		g.drawRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
    	}
    }
}