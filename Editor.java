
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

public class Editor {

	JFrame frame;
	JLabel im1;
	JLabel im2;
	BufferedImage ogImg;
	BufferedImage scaledImg;
	int width = 352;
	int height = 288;


	public void createGUI(){

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

        //Loading Buttons
        JButton loadOne = new JButton("Load Primary");
        JButton loadTwo = new JButton("Load Secondary");

        //Image containers
        im1 = new JLabel("Primary Video", SwingConstants.CENTER);
        im1.setMinimumSize(new Dimension(352, 288));
        im1.setPreferredSize(new Dimension(352, 288));
        im2 = new JLabel("Secondary Video", SwingConstants.CENTER);
        im2.setMinimumSize(new Dimension(352, 288));
        im2.setPreferredSize(new Dimension(352, 288));

		GridBagConstraints c = new GridBagConstraints();

        //Add Buttons
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 1;
        c.weighty = 1;
		c.gridx = 0;
        c.gridy = 0;
		frame.add(loadOne, c);

        c.gridx = 1;
        c.gridy = 0;
        frame.add(loadTwo, c);

        //Add Image containers
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 1;
        frame.add(im1, c);

        c.weighty = 1;
        c.gridx = 1;
        c.gridy = 1;
        frame.add(im2, c);





        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		Editor e = new Editor();
		e.createGUI();

	}

}
