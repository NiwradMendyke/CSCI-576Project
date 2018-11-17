package finalproject;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.nio.file.Files;
import java.util.*;

public class Hyperlink {
	
	String linkName;
	int start, length;

	HashMap<Integer, Rectangle> keyframes;
	ArrayList<Rectangle> frames;

	public Hyperlink(String name) {
		linkName = name;

		keyframes = new HashMap<Integer, Rectangle>();
		frames = new ArrayList<Rectangle>();
	}

	public int getStart() { return start; }

	public int getLength() { return length; }

	public void addKeyframe(int frame, Rectangle rect) { // adds a key frame
		if (keyframes.size() == 0) { // sets the start frame
			System.out.println("Adding start frame to " + linkName + " at " + frame);
			frames.add(new Rectangle(rect));
			start = frame;
		}
		if (keyframes.size() == 1) { // sets the end frame
			length = frame - start;
		}
		keyframes.put(frame, rect);
		if (keyframes.size() >= 2) {
			generateFrames();
		}
	}

	public void getFrames(int frame, ArrayList<Rectangle> currRects) { // fills an arraylist with link boxes for current frame
		if (frame >= start && frame <= (start + length)) {
			System.out.println(frames.get(frame - start).toString());
			currRects.add(frames.get(frame - start));
		}
	}

	private void generateFrames() { // called whenever a keyframe is added, uses those keyframes to generate a list of frames
		frames.clear();
		Rectangle prev = keyframes.get(start);
		for (int i = 0; i <= length; i++) {
			if (keyframes.get(start + i) != null) {
				prev = keyframes.get(start + i);
			}
			frames.add(new Rectangle(prev));
		}
	}
}

