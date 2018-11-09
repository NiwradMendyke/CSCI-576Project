
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class Editor {

	JFrame frame;
	JLabel im1;
	JLabel im2;
    JLabel currentFrame1;
    JLabel currentFrame2;
    JSlider slider1; 
    JSlider slider2;
	BufferedImage ogImg;
	BufferedImage scaledImg;
	int width = 352;
	int height = 288;

    //Should set these values when video is loaded
    //Currently initialized to 9000 for testing purposes
    int im1Frames = 9000;
    int im2Frames = 9000;




	public void createGUI() {

        //File Chooser
        JFileChooser fc = new JFileChooser();

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

        //Loading Buttons
        JButton loadOne = new JButton("Load Primary");
        loadOne.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int ret = fc.showOpenDialog(frame);

                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    //Do file things
                    System.out.println(file.getName());
                }
            }
        });

        JButton loadTwo = new JButton("Load Secondary");
        loadTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int ret = fc.showOpenDialog(frame);

                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    //Do file things
                    System.out.println(file.getName());
                }
            }
        });

        //Image containers
        im1 = new JLabel("Primary Video", SwingConstants.CENTER);
        im1.setMinimumSize(new Dimension(352, 288));
        im1.setPreferredSize(new Dimension(352, 288));
        im2 = new JLabel("Secondary Video", SwingConstants.CENTER);
        im2.setMinimumSize(new Dimension(352, 288));
        im2.setPreferredSize(new Dimension(352, 288));

        //Sliders
        slider1 = new JSlider(JSlider.HORIZONTAL, 0, 9000, 0);
        slider1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if(!source.getValueIsAdjusting()) {
                    currentFrame1.setText(Integer.toString(source.getValue()));
                }
            }
        });

        slider2 = new JSlider(JSlider.HORIZONTAL, 0, 9000, 0);
        slider2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if(!source.getValueIsAdjusting()) {
                    currentFrame2.setText(Integer.toString(source.getValue()));
                }
            }
        });

        //Current frame labels
        currentFrame1 = new JLabel();
        currentFrame1.setText(Integer.toString(slider1.getValue()));
        currentFrame1.setHorizontalAlignment(SwingConstants.CENTER);

        currentFrame2 = new JLabel();
        currentFrame2.setText(Integer.toString(slider2.getValue()));
        currentFrame2.setHorizontalAlignment(SwingConstants.CENTER);

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

        //Add Sliders and current frame labels
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 2;
        frame.add(slider1, c);

        c.gridx = 1;
        frame.add(slider2, c);

        //Add current frame labels
        c.insets = new Insets(0,0,0,0);
        c.gridx = 0;
        c.gridy = 3;
        frame.add(currentFrame1, c);

        c.gridx = 1;
        frame.add(currentFrame2, c);









        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		Editor e = new Editor();
		e.createGUI();

	}

}
