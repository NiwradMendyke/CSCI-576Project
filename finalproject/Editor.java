package finalproject;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.Border;
import java.util.*;

// import DrawableLabel;

public class Editor {

    HashMap<String, Hyperlink> links; // master list of hyperlinks for a loaded primary video, might be replaced later

	JFrame frame;
	DrawableLabel im1;
	JLabel im2;
    JLabel currentFrame1, currentFrame2;
    JSlider slider1, slider2;
    JList<String> linkList;

	BufferedImage ogImg;
	BufferedImage scaledImg;

	File primaryFile;
	File secondaryFile;

	int width = 352;
	int height = 288;

    Color[] colors = {Color.magenta, Color.red, Color.pink, Color.cyan, Color.yellow, Color.green};
    int colorIndex = 0;

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

        links = new HashMap<String, Hyperlink>();

        //File Chooser
        JFileChooser fc = new JFileChooser();

        JButton loadOne, loadTwo, newHyperlink, connectVideo, save;
        JLabel helperText;

        //Makes file chooser select directories, delete if we want to select files
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

        //Loading Buttons
        loadOne = new JButton("Load Primary");
        loadOne.setHorizontalAlignment(SwingConstants.CENTER);
        loadOne.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                 fc.setDialogTitle("Load Primary Video");
                 int ret = fc.showOpenDialog(frame);
                 if (ret == JFileChooser.APPROVE_OPTION) {
                     primaryFile = fc.getSelectedFile();
                 }
                 if (primaryFile == null) {
                     return;
                 }

                File f = new File(primaryFile, primaryFile.getName() + "0001.rgb");
            	showIms(f.getAbsolutePath(), im1);
            	slider1.setEnabled(true);

            }
        });

        loadTwo = new JButton("Load Secondary");
        loadTwo.setHorizontalAlignment(SwingConstants.CENTER);
        loadTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                fc.setDialogTitle("Load Secondary Video");
                int ret = fc.showOpenDialog(frame);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    secondaryFile = fc.getSelectedFile();
                }
                if (secondaryFile == null) {
                    return;
                }
                File f = new File(secondaryFile, secondaryFile.getName() + "0001.rgb");
            	showIms(f.getAbsolutePath(), im2);
            	slider2.setEnabled(true);

            }
        });

        newHyperlink = new JButton("Create New Hyperlink");
        newHyperlink.setHorizontalAlignment(SwingConstants.CENTER);
        newHyperlink.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (im1.getIcon() == null) {
                    JOptionPane.showMessageDialog(frame, "You must load a primary video before you can create a hyperlink");
                    return;
                }
                String newLinkName = JOptionPane.showInputDialog(frame, "Please enter a name for the new link");                
                im1.createNewLink(newLinkName, colors[colorIndex]);
                colorIndex = (colorIndex + 1) % colors.length;
            }
        });

        connectVideo = new JButton("Connect Video");
        connectVideo.setHorizontalAlignment(SwingConstants.CENTER);

        save = new JButton("Save File");
        save.setHorizontalAlignment(SwingConstants.CENTER);

        helperText = new JLabel();
        helperText.setText("Helper text here");
        helperText.setHorizontalAlignment(SwingConstants.CENTER);

        //Image containers
        im1 = new DrawableLabel(frame, helperText, links, "Primary Video", SwingConstants.CENTER);
        im1.setMinimumSize(new Dimension(352, 288));
        im1.setPreferredSize(new Dimension(352, 288));
        im2 = new JLabel("Secondary Video", SwingConstants.CENTER);
        im2.setMinimumSize(new Dimension(352, 288));
        im2.setPreferredSize(new Dimension(352, 288));

        String[] s = {"one", "two", "three", "four", "five"};
        linkList = new JList<String>(s);

        //Sliders
        slider1 = new JSlider(JSlider.HORIZONTAL, 1, 9000, 1);
        slider1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {

                JSlider source = (JSlider)e.getSource();
                currentFrame1.setText(Integer.toString(source.getValue()));
                String newFrame = String.format("%04d", source.getValue()) + ".rgb";
                File f = new File(primaryFile, primaryFile.getName() + newFrame);
                showIms(f.getAbsolutePath(), im1);
                im1.updateFrame(source.getValue());
            }
        });
        slider1.setEnabled(false);

        slider2 = new JSlider(JSlider.HORIZONTAL, 1, 9000, 1);
        slider2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {

                JSlider source = (JSlider)e.getSource();
                currentFrame2.setText(Integer.toString(source.getValue()));
                String newFrame = String.format("%04d", source.getValue()) + ".rgb";
                File f = new File(secondaryFile, secondaryFile.getName() + newFrame);
                showIms(f.getAbsolutePath(), im2);
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
		// c.anchor = GridBagConstraints.CENTER;
		c.weightx = 1;
        c.weighty = 1;
		c.gridx = 0;
        c.gridy = 0;
		frame.add(loadOne, c);

		c.gridx = 1;
        frame.add(loadTwo, c);

		c.gridx = 2;
        frame.add(newHyperlink, c);

        c.gridx = 3;
        frame.add(connectVideo, c);

        c.gridx = 4;
        frame.add(save, c);

        //Add list of hyperlinks
        c.gridy = 1;
        c.gridx = 2;
        frame.add(linkList, c);

        //Add Image containers
        c.gridheight = 1;
        c.gridwidth = 2;
        c.gridx = 0;
        frame.add(im1, c);

        c.gridx = 3;
        frame.add(im2, c);

        //Add Sliders and current frame labels
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        c.gridx = 0;
        c.gridy = 2;
        frame.add(slider1, c);

        c.gridx = 3;
        frame.add(slider2, c);

        //Add current frame labels
        c.insets = new Insets(0,0,10,0);
        c.gridx = 0;
        c.gridy = 3;
        frame.add(currentFrame1, c);

        c.gridx = 3;
        frame.add(currentFrame2, c);

        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 2;
        frame.add(helperText, c);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		Editor e = new Editor();
		e.createGUI();
	}
}
