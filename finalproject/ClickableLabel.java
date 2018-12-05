package finalproject;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class ClickableLabel extends JLabel implements MouseListener, MouseMotionListener { // custom JLabel with drawing functionalities

	public HashMap<String, Hyperlink> links; // reference to master list of hyperlinks in Editor.java
	Hyperlink newLink; // temporary reference to new hyperlink 
	String currLink;

	int currFrame = 1; // current frame being displayed
	HashMap<Rectangle, String> currRects; // list of hyperlink boxes in the current frame

	Rectangle rect; 

	Point lastMouse;

	JFrame parentFrame;


	public ClickableLabel(JFrame parent, HashMap<String, Hyperlink> hyperlinks, String name, int orientation) {
		super(name, orientation);

		parentFrame = parent;

		links = hyperlinks;
		currRects = new HashMap<Rectangle, String>();


		addMouseListener(this);
    	addMouseMotionListener(this);
	}


	public void updateFrame() { // called by hyperlinkmanager to just update rects in the current frame
		currRects.clear();

		links.forEach((name, link) -> link.getFrames(currFrame, currRects));

		rect = null;

		currRects.forEach((r, name) -> {
			if (r.contains(lastMouse)) {
				rect = r;
				return;
			}
		});
	}

	public void updateFrame(int newFrame) { // called by Editor to update current frame number and the rects in the current frame
		currFrame = newFrame;
		updateFrame();
	}


	public void mouseClicked(MouseEvent e) { }
  	public void mouseEntered(MouseEvent e) { }
  	public void mouseExited(MouseEvent e) { }
	public void mousePressed(MouseEvent e) {}
    public void mouseReleased (MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}

	public void mouseMoved(MouseEvent e) { 
		lastMouse = e.getPoint();
		if (rect == null || rect.contains(lastMouse)) {
			rect = null;

			currRects.forEach((r, name) -> {
				if (r.contains(lastMouse)) {
					rect = r;
					return;
				}
			});
		}
	}


	@Override
   protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	Graphics2D g2 = (Graphics2D) g;
    
    	currRects.forEach((r, name) -> {
 			Color tmp = links.get(name).getColor();

			g.setColor(new Color(tmp.getRed(), tmp.getGreen(), tmp.getBlue(), 40));
 			g.fillRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());

 			if (rect != null && r.equals(rect)) {
 				g.setColor(tmp);
 				g.drawRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
 			}
 		});
   }
}

