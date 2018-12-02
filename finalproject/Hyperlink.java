package finalproject;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import javafx.util.*;

public class Hyperlink implements java.io.Serializable { // class that represents a single hyperlink, with all of it's associated rects
	
	String linkName;
	int start, length;

	Color linkColor; // color to draw the link with

	ArrayList<Rectangle> frames; // list of frames generated from the keyframes
	HashMap<Integer, Rectangle> keyframes; // tracks the keyframes and their indices

	Pair<File, Integer> linkedVideo;


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

	public void setName(String newName) {
		linkName = newName;
	}

	public void setLinkedVideo(File videoFolder, int frameNum) {
		linkedVideo = new Pair<File, Integer>(videoFolder, frameNum);
	}

	public Pair<File, Integer> getLinkedVideo() {
		return linkedVideo;
	}

	public void addKeyframe(int frame, Rectangle rect) { // adds a key frame
		if (keyframes.size() == 0) { // sets the start frame
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

	public void setStartEndFrames(int newStart, int newEnd) { // allows the hyperlinkmanager to modify the start/end frames of the hyperlink
		int oldEnd = start + length;

		if (newStart < start) {
			keyframes.put(newStart, keyframes.get(start));
			start = newStart;
			length = oldEnd - start;
		}
		if (newStart > start) {
			keyframes.put(newStart, keyframes.get(start));
			for (int i = start; i < newStart; i++) {
				if (keyframes.get(i) != null) {
					keyframes.remove(i);
				}
			}
			start = newStart;
			length = oldEnd - start;
		}
		if (newEnd < oldEnd) {
			keyframes.put(newEnd, keyframes.get(oldEnd));
			for (int i = newEnd+1; i <= oldEnd; i++) {
				if (keyframes.get(i) != null) {
					keyframes.remove(i);
				}
			}
			length = newEnd - start;
		}
		if (newEnd > oldEnd) {
			keyframes.put(newEnd, keyframes.get(oldEnd));
			length = newEnd - start;
		}

		generateFrames();
	}

	private void generateFrames() { // called whenever a keyframe is added, uses those keyframes to generate a list of frames
		frames.clear();
        Rectangle prev = keyframes.get(start);
        
        int i = 0;
        while (i <= length) {
        	frames.add(prev);

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
            

            int k = 1;
            while (i+k != j) {
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
	}
}

