package finalproject;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.Border;
import javafx.util.*;
import java.util.*;

public class Player {

    HashMap<String, Hyperlink> links; // master list of hyperlinks for a loaded primary video, might be replaced later

	JFrame frame;
	JLabel im1;
    JSlider slider1;
    JLabel currentFrame1;

	BufferedImage ogImg;
	BufferedImage scaledImg;

	File primaryFile;

	int width = 352;
	int height = 288;




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

	public void showIms(File videoFolder, int num, JLabel im, JLabel frameNum) {
        frameNum.setText(Integer.toString(num));
        String newFrame = String.format("%04d", num) + ".rgb";
        File f = new File(videoFolder, videoFolder.getName() + newFrame);

		BufferedImage img;

		try {
			File file = new File(f.getAbsolutePath());
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

        JButton load;

        JFileChooser fc = new JFileChooser();

        //Makes file chooser select directories, delete if we want to select files
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

        //Loading Buttons
        load = new JButton("Load");
        load.setHorizontalAlignment(SwingConstants.CENTER);
        load.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {

                
                fc.setDialogTitle("Load Video");
                int ret = fc.showOpenDialog(frame);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    primaryFile = fc.getSelectedFile();
                }
                if (primaryFile == null) {
                    return;
                }
                 
                File linkData = new File(primaryFile, "hyperlinks");
                if (linkData.exists()) {
                    try {
                        
                        FileInputStream fileStream = new FileInputStream(linkData);
                        ObjectInputStream linkStream = new ObjectInputStream(fileStream);

                        links = (HashMap<String, Hyperlink>)linkStream.readObject();

                        linkStream.close();
                        fileStream.close();

                        System.out.println("Hyperlinks have been loaded");
                    }

                    catch (IOException exception) {
                        System.out.println("IOException, links may not have loaded");
                    }
                    catch (ClassNotFoundException exception) {
                        System.out.println("Class Not Found Exception, links may not have loaded");
                    }
                }
                showIms(primaryFile, 1, im1, currentFrame1);
            	slider1.setEnabled(true);

            }
        });



        im1 = new JLabel("Load Video", SwingConstants.CENTER);
        im1.setMinimumSize(new Dimension(352, 288));
        im1.setPreferredSize(new Dimension(352, 288));

        slider1 = new JSlider(JSlider.HORIZONTAL, 1, 9000, 1);
        slider1.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                int num = ((JSlider)e.getSource()).getValue();
            }
        });
        slider1.setEnabled(false);

        currentFrame1 = new JLabel();
        currentFrame1.setText(Integer.toString(slider1.getValue()));
        currentFrame1.setHorizontalAlignment(SwingConstants.CENTER);

		GridBagConstraints c = new GridBagConstraints();


        //Add Buttons
		c.weightx = 1;
        c.weighty = 1;
		c.gridx = 0;
        c.gridy = 0;
		frame.add(load, c);

        //Add Image containers
        c.gridheight = 2;
        c.gridwidth = 2;
        c.gridy = 1;
        c.gridx = 0;
        frame.add(im1, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 3;
        frame.add(slider1, c);

        c.insets = new Insets(0,0,10,0);
        c.gridx = 0;
        c.gridy = 4;
        frame.add(currentFrame1, c);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		Player p = new Player();
		p.createGUI();
	}
}
