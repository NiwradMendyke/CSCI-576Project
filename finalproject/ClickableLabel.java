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

	Point mouseClick; // tracks point for translating box
	String selectedCorner = ""; // tracks corner for dragging box corners

	int drawMode = 0; // represents which state the drawable label is in
	// 0 is no drawing, 1 is draw start frame, 2 is draw end frame, 3 is modify link box

	JFrame parentFrame;
	JLabel helpText;
	JList<String> linkList;


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

		if (drawMode == 2) {
			newLink.getFrames(currFrame, currRects);
		}
		if (drawMode == 0 && currRects.size() > 0) {
			drawMode = 3;
		}
		if (drawMode == 3 && currRects.size() == 0) {
			drawMode = 0;
		}
	}

	public void updateFrame(int newFrame) { // called by Editor to update current frame number and the rects in the current frame
		currFrame = newFrame;
		updateFrame();
	}

	public void updateFrame(int newFrame, String currentLink) { // called by Editor to update current frame number, rects, and the current hyperlink
		currFrame = newFrame;
		currLink = currentLink;
		// hyperlinkManager.updateManager(links.get(currLink));
		updateFrame();
	}



	public void mouseClicked(MouseEvent e) { }
  	public void mouseEntered(MouseEvent e) { }
  	public void mouseExited(MouseEvent e) { }
	public void mousePressed(MouseEvent e) {}
    public void mouseReleased (MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) { }


	@Override
   protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	Graphics2D g2 = (Graphics2D) g;
    
    	if (rect != null) {
    		currRects.put(rect, currLink);
    	}

 		currRects.forEach((r, name) -> {
 			if (name.equals(currLink)) {
 				g2.setStroke(new BasicStroke(3));
 			}
 			else {
 				g2.setStroke(new BasicStroke(1));
 			}

 			if (newLink != null && name.equals(newLink.getName())) {
 				g.setColor(newLink.getColor());
 			}
 			else {
 				g.setColor(links.get(name).getColor());
 			}

 			g.drawRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
 		});
   }
}

