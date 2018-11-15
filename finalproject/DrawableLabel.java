package finalproject;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class DrawableLabel extends JLabel {

	public DrawableLabel(String name, int orientation) {
		super(name, orientation);
	}

	@Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    }
}