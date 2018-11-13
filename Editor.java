
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
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

	String primaryFile = "";
	String secondaryFile = "";

	int width = 352;
	int height = 288;

    //Should set these values when video is loaded
    //Currently initialized to 9000 for testing purposes
    int im1Frames = 9000;
    int im2Frames = 9000;




    private BufferedImage drawRgbImg(byte[] bytes) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int count = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				byte a = 0;
				byte r = bytes[count];
				byte g = bytes[count + (height * width)];
				byte b = bytes[count + (height * width * 2)];

				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				img.setRGB(x, y, pix);
				count++;
			}
		}

		// System.out.println("Read the .rgb image file");
		return img;
	}

	public void showIms(String filePath, JLabel im) {
		BufferedImage img;

		try {
			File file = new File(filePath);
			InputStream fis = new FileInputStream(file);

			long len = file.length();
			int offset = 0;
			int numRead = 0;

			byte[] bytes = new byte[(int) len]; 

			while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			img = drawRgbImg(bytes);
			im.setIcon(new ImageIcon(img));
			frame.revalidate();
			frame.repaint();

			fis.close();
		}
		catch(IOException e) {
			System.out.println("Error: "+e);
		}
	}


	public void createGUI() {

        //File Chooser
        JFileChooser fc = new JFileChooser();

        //Makes file chooser select directories, delete if we want to select files
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

        //Loading Buttons
        JButton loadOne = new JButton("Load Primary");
        loadOne.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

              fc.setDialogTitle("Load Primary Video");
              int ret = fc.showOpenDialog(frame);

                if (ret == JFileChooser.APPROVE_OPTION) {
                    primaryFile = fc.getSelectedFile().getAbsolutePath();
                }
            	showIms(primaryFile + "0001.rgb", im1);
            	slider1.setEnabled(true);

            }
        });

        JButton loadTwo = new JButton("Load Secondary");
        loadTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              
            	secondaryFile = "../London/LondonTwo/LondonTwo";
            	showIms(secondaryFile + "0001.rgb", im2);
            	slider2.setEnabled(true);

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
        slider1 = new JSlider(JSlider.HORIZONTAL, 1, 9000, 1);
        slider1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();

                currentFrame1.setText(Integer.toString(source.getValue()));
                if (!primaryFile.equals("")) {
					showIms(primaryFile + String.format("%04d", source.getValue()) + ".rgb", im1);

                }
            }
        });
        slider1.setEnabled(false);

        slider2 = new JSlider(JSlider.HORIZONTAL, 1, 9000, 1);
        slider2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();

                currentFrame2.setText(Integer.toString(source.getValue()));
                if (!secondaryFile.equals("")) {
					showIms(secondaryFile + String.format("%04d", source.getValue()) + ".rgb", im2);

                }
            }
        });
        slider2.setEnabled(false);

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
