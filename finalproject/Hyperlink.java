package finalproject;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.nio.file.Files;
import java.util.*;

public class Hyperlink {
	
	String linkName;
	int start, length;

	Color linkColor;

	HashMap<Integer, Rectangle> keyframes;
	ArrayList<Rectangle> frames;


	public Hyperlink(String name, Color color) {
		linkName = name;
		linkColor = color;

		keyframes = new HashMap<Integer, Rectangle>();
		frames = new ArrayList<Rectangle>();
	}

	public String getName() { return linkName; }

	public int getStart() { return start; }

	public int getLength() { return length; }

	public Color getColor() { return linkColor; }

	public void addKeyframe(int frame, Rectangle rect) { // adds a key frame
		if (keyframes.size() == 0) { // sets the start frame
			System.out.println("Adding start frame to " + linkName + " at " + frame);
			frames.add(new Rectangle(rect));
			start = frame;
		}
		if (keyframes.size() == 1) { // sets the end frame
			length = frame - start;
		}
		keyframes.put(frame, new Rectangle(rect));
		if (keyframes.size() >= 2) {
			generateFrames();
		}
	}

	public void getFrames(int frame, HashMap<Rectangle, String> currRects) { // fills an arraylist with link boxes for current frame
		if (frame >= start && frame <= (start + length)) {
			//System.out.println(frames.get(frame - start).toString());
			currRects.put(frames.get(frame - start), linkName);
		}
	}

	private void generateFrames() { // called whenever a keyframe is added, uses those keyframes to generate a list of frames
		System.out.println("Generating frames from " + keyframes.size() + " keyframes");
		frames.clear();
        Rectangle prev = keyframes.get(start);
        
        int i = 0;
        while (i <= length) {
            int j = i+1;
            //Find next keyframe
            //if doesn't exist, then break loop
            while (keyframes.get(start + j) == null && j <= length) {
                j += 1;
            }
            if (j > length) {
                break;
            }
            
            //if we didn't break, then we got some work to do
            Rectangle next = keyframes.get(start + j);
            double x_mod = (next.getX() - prev.getX()) / (j-i);
            double y_mod = (next.getY() - prev.getY()) / (j-i);
            double width_mod = (next.getWidth() - prev.getWidth()) / (j-i);
            double height_mod = (next.getHeight() - prev.getHeight()) / (j-i);
            

            int k = 0;
            while (i+k != j+1) {
                int x = (int) Math.round(prev.getX() + x_mod*k);
                int y = (int) Math.round(prev.getY() + y_mod*k);
                int width = (int) Math.round(prev.getWidth() + width_mod*k);
                int height = (int) Math.round(prev.getHeight() + height_mod*k);
                frames.add(new Rectangle(x, y, width, height));
                k+=1;
                }
            i = j;
            prev = next;
        }
        
        /*
        for (int i = 0; i <= length; i++) {
            if (keyframes.get(start + i) != null) {
                prev =  keyframes.get(start + i);
            }
            frames.add(new Rectangle(prev));
        }
        */
		System.out.println("Interpolated Rectangles");

	}
}

