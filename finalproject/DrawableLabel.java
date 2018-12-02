package finalproject;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class DrawableLabel extends JLabel implements MouseListener, MouseMotionListener { // custom JLabel with drawing functionalities

	public HashMap<String, Hyperlink> links; // reference to master list of hyperlinks in Editor.java
	Hyperlink newLink; // temporary reference to new hyperlink 
	String currLink;
	HyperlinkManager hyperlinkManager;

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


	public DrawableLabel(JFrame parent, JLabel help, JList<String> list, HashMap<String, Hyperlink> hyperlinks, String name, int orientation) {
		super(name, orientation);

		parentFrame = parent;
		helpText = help;
		linkList = list;

		links = hyperlinks;
		currRects = new HashMap<Rectangle, String>();

		hyperlinkManager = new HyperlinkManager(parent, this);

		addMouseListener(this);
    	addMouseMotionListener(this);
	}

	public void updateManager(String selectedLink) {
		hyperlinkManager.updateManager(links.get(selectedLink));
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

	public void updateName(String oldName, String newName) { // updates the name of the given hyperlink
		links.get(oldName).setName(newName);
		links.put(newName, links.get(oldName));
		links.remove(oldName); // modifies the master list of name/hyperlinks

		DefaultListModel<String> model = (DefaultListModel<String>) linkList.getModel();
		model.setElementAt(newName, linkList.getSelectedIndex());
		linkList.setModel(model); // updates the Jlist data

		currLink = newName;
		updateFrame(); // updates current frames
	}

	public void createNewLink(String name, Color color) { // called by Editor.java to start process for new hyperlink
		newLink = new Hyperlink(name, color);
		drawMode = 1;
		currLink = name;
		helpText.setText("Draw start frame");
	}

	public void mouseClicked(MouseEvent e) { }

  	public void mouseEntered(MouseEvent e) { }

  	public void mouseExited(MouseEvent e) { }

	public void mousePressed(MouseEvent e) {
		// System.out.println("Current draw mode is " + drawMode);
		if (drawMode == 1) {
			rect = new Rectangle(e.getPoint());
		}
		if (drawMode == 2) {
			if (currFrame <= newLink.getStart()) {
				JOptionPane.showMessageDialog(parentFrame, "End frame must be after start frame");
			}
			else {
				rect = new Rectangle(e.getPoint());
			}
		}
		if (drawMode == 3) {
			currRects.forEach((r, name) -> {
				if (rect == null && (setCorner(r, e.getPoint()) || r.contains(e.getPoint()))) {
					rect = r;
					currLink = name;
					// hyperlinkManager.updateManager(links.get(currLink));
					mouseClick = e.getPoint();
					
					for (int i = 0; i < linkList.getModel().getSize(); i++) {
						// used to update the selected hyperlink in the gui list to whichever hyperlink the user is editing
						if (linkList.getModel().getElementAt(i).equals(name)) { 
							linkList.setSelectedIndex(i);
						}
					}
				}
			});
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (drawMode == 1) {
			newLink.addKeyframe(currFrame, rect);
			rect = null;
			drawMode = 2;
			helpText.setText("Draw end frame");
		}
		if (drawMode == 3) {
			links.get(currLink).addKeyframe(currFrame, rect);
			rect = null;
		}
		if (drawMode == 2 && rect != null) {
			newLink.addKeyframe(currFrame, rect);
			links.put(newLink.getName(), newLink);
			hyperlinkManager.updateManager(newLink);
			rect = null;
			drawMode = 3;
			helpText.setText("");
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (currRects.get(rect) != null) { // removes previous instance of the rect from currRects before adding the updated version
			currRects.remove(rect);
		}

		if (drawMode == 1) {
			rect.setSize(e.getX() - (int)rect.getX(), e.getY() - (int)rect.getY());
		}
		if (drawMode == 2 && rect != null) {
			rect.setSize(e.getX() - (int)rect.getX(), e.getY() - (int)rect.getY());
		}
		if (drawMode == 3 && rect != null) {
			if (!selectedCorner.equals("")) {
				moveCorner(e);
			}
			else if (rect.contains(e.getPoint())) {
				moveRectangle(e);
			}
		}
		repaint();
	}

	public void mouseMoved(MouseEvent e) { }

	private boolean setCorner(Rectangle r, Point mousePoint) { // used to check if a corner is selected
		double clickDistance = 10.0;

		if (mousePoint.distance(r.getX(), r.getY()) < clickDistance) {
			selectedCorner = "topleft";
		}
		else if (mousePoint.distance(r.getX() + r.getWidth(), r.getY()) < clickDistance) {
			selectedCorner = "topright";
		}
		else if (mousePoint.distance(r.getX() + r.getWidth(), r.getY() + r.getHeight()) < clickDistance) {
			selectedCorner = "bottomright";
		}
		else if (mousePoint.distance(r.getX(), r.getY() + r.getHeight()) < clickDistance) {
			selectedCorner = "bottomleft";
		}
		else {
			selectedCorner = "";
		}

		// System.out.println("selectedCorner = " + selectedCorner);
		return !selectedCorner.equals("");
	}

	private void moveCorner(MouseEvent e) { // used to move a corner of a box
		if (selectedCorner.equals("topleft")) {
			rect.setBounds(e.getX(), e.getY(), 
				(int)rect.getWidth() - (e.getX() - (int)rect.getX()), (int)rect.getHeight() - (e.getY() - (int)rect.getY()));
		}
		if (selectedCorner.equals("topright")) {
			rect.setBounds((int)rect.getX(), e.getY(),
				e.getX() - (int)rect.getX(), (int)rect.getHeight() - (e.getY() - (int)rect.getY()));
		}
		if (selectedCorner.equals("bottomright")) {
			rect.setBounds((int)rect.getX(), (int)rect.getY(),
				e.getX() - (int)rect.getX(), e.getY() - (int)rect.getY());
		}
		if (selectedCorner.equals("bottomleft")) {
			rect.setBounds(e.getX(), (int)rect.getY(),
				(int)rect.getWidth() - (e.getX() - (int)rect.getX()), e.getY() - (int)rect.getY());
		}

		if (rect.getWidth() < 20) {
			rect.setSize(20, (int)rect.getHeight());
		}
		if (rect.getHeight() < 20) {
			rect.setSize((int)rect.getWidth(), 20);
		}
	}

	private void moveRectangle(MouseEvent e) { // used to translate a box
		int xdist = e.getX() - (int)mouseClick.getX();
		int ydist = e.getY() - (int)mouseClick.getY();
		rect.setLocation((int)rect.getX() + xdist, (int)rect.getY() + ydist);
		mouseClick = e.getPoint();
	}

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

