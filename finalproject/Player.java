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
	ClickableLabel im1;
    JSlider slider1;
    JLabel currentFrame1;

    Stack<Pair<File, Integer>> videoStack;

	BufferedImage ogImg;
	BufferedImage scaledImg;

	File primaryFile;

    Audio audioPlayer;
    Scanner scanner;

	int width = 352;
	int height = 288;

    boolean playing = false;




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

    public void playVideo() {
        slider1.setValue(slider1.getValue()+1);
        currentFrame1.setText(Integer.toString(slider1.getValue()));
        showIms(primaryFile, slider1.getValue(), im1, currentFrame1);
        im1.updateFrame(slider1.getValue());
        if (slider1.getValue() % 120 == 0) {
            audioPlayer.jump(slider1.getValue() * 33333);
        }
        java.util.Timer t = new java.util.Timer();
        if (playing && slider1.getValue() != 9000) {
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    playVideo();
                }
            }, 21);
        }
    }

            
        


	public void createGUI() {

        videoStack = new Stack<Pair<File, Integer>>();
        JButton load, pause, back;
        load = new JButton("Load");
        pause = new JButton("Pause");
        back = new JButton("<--");

        JFileChooser fc = new JFileChooser();

        //Makes file chooser select directories, delete if we want to select files
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

        //Loading Buttons
        load.setHorizontalAlignment(SwingConstants.RIGHT);
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

                        im1.links = links;
                        im1.updateFrame();

                        System.out.println("Hyperlinks have been loaded");
                    }

                    catch (IOException exception) {
                        System.out.println("IOException, links may not have loaded");
                    }
                    catch (ClassNotFoundException exception) {
                        System.out.println("Class Not Found Exception, links may not have loaded");
                    }
                }
                try {
                audioPlayer = new Audio(new File(primaryFile, primaryFile.getName() + ".wav"));
                }
                catch (Exception ex) {
                    System.out.println("Error with playing sound");
                    ex.printStackTrace();
                }
                scanner = new Scanner(System.in);
                slider1.setValue(1);
                showIms(primaryFile, 1, im1, currentFrame1);
                playing = true;
                pause.setText("Pause");
                playVideo();
                audioPlayer.play();

            }
        });

        back.setHorizontalAlignment(SwingConstants.LEFT);
        back.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {

                if (!videoStack.empty()) {
                    playing = false;
                    Pair<File, Integer> tmp = videoStack.pop();
                    primaryFile = tmp.getKey();
                    slider1.setValue(tmp.getValue());
                 
                    File linkData = new File(primaryFile, "hyperlinks");
                    if (linkData.exists()) {
                        try {
                            
                            FileInputStream fileStream = new FileInputStream(linkData);
                            ObjectInputStream linkStream = new ObjectInputStream(fileStream);

                            links = (HashMap<String, Hyperlink>)linkStream.readObject();

                            linkStream.close();
                            fileStream.close();

                            im1.links = links;
                            im1.updateFrame();

                            System.out.println("Hyperlinks have been loaded");
                        }

                        catch (IOException exception) {
                            System.out.println("IOException, links may not have loaded");
                        }
                        catch (ClassNotFoundException exception) {
                            System.out.println("Class Not Found Exception, links may not have loaded");
                        }
                    }
                    else {
                        links.clear();
                        System.out.println("No hyperlink data exists, starting fresh.");
                    }

                    audioPlayer.stop();
                    try {
                        audioPlayer.resetAudioStream(new File(primaryFile, primaryFile.getName() + ".wav"));
                        audioPlayer.play();
                        audioPlayer.jump(slider1.getValue() * 33333);
                    }
                    catch (Exception ex) {
                        System.out.println("Audio issue");
                    }
                    im1.updateFrame();
                    showIms(primaryFile, slider1.getValue(), im1, currentFrame1);
                    playing = true;
                    pause.setText("Pause");
                    playVideo();
                }
            }
        });


        pause.setHorizontalAlignment(SwingConstants.CENTER);
        pause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (pause.getText() == "Pause") {
                    pause.setText("Play");
                    playing = false;
                    audioPlayer.pause();
                    slider1.setEnabled(true);
                }
                else if (pause.getText() == "Play") {
                    pause.setText("Pause");
                    playing = true;
                    slider1.setEnabled(false);
                    audioPlayer.jump(slider1.getValue() * 33333);
                    audioPlayer.play();
                    playVideo();
                }
            }
        });



        im1 = new ClickableLabel(frame, links, "Load Video", SwingConstants.CENTER);
        im1.setMinimumSize(new Dimension(352, 288));
        im1.setPreferredSize(new Dimension(352, 288));
        im1.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("unchecked")
            public void mousePressed(MouseEvent e) {
                String linkName = null;
                for (Map.Entry<Rectangle, String> entry : im1.currRects.entrySet()) {
                    if (entry.getKey().contains(e.getPoint())) {
                        linkName = entry.getValue();
                    }
                }
                if (linkName != null) {
                    videoStack.push(new Pair<File, Integer> (primaryFile, slider1.getValue()));
                    playing = false;
                    Hyperlink clickedLink = links.get(linkName);
                    File newVideoFile = clickedLink.linkedVideo.getKey();  
                    int newVideoFrame = clickedLink.linkedVideo.getValue();
                    File linkData = new File(newVideoFile, "hyperlinks");
                    im1.links.clear();
                    System.out.println(linkData);
                    if (linkData.exists()) {
                        try {
                            
                            FileInputStream fileStream = new FileInputStream(linkData);
                            ObjectInputStream linkStream = new ObjectInputStream(fileStream);

                            links = (HashMap<String, Hyperlink>)linkStream.readObject();

                            linkStream.close();
                            fileStream.close();

                            im1.links = links;
                            im1.updateFrame();

                            System.out.println("Hyperlinks have been loaded");
                        }

                        catch (IOException exception) {
                            System.out.println("IOException, links may not have loaded");
                        }
                        catch (ClassNotFoundException exception) {
                            System.out.println("Class Not Found Exception, links may not have loaded");
                        }
                    }
                    else {
                        links.clear();
                        System.out.println("No hyperlink data exists, starting fresh.");
                    }
                    primaryFile = newVideoFile;
                    slider1.setValue(newVideoFrame);
                    audioPlayer.stop();
                    try {
                        audioPlayer.resetAudioStream(new File(primaryFile, primaryFile.getName() + ".wav"));
                        audioPlayer.play();
                        audioPlayer.jump(slider1.getValue() * 33333);
                    }
                    catch (Exception ex) {
                        System.out.println("Audio issue");
                    }
                    im1.updateFrame();
                    showIms(primaryFile, slider1.getValue(), im1, currentFrame1);
                    slider1.setEnabled(false);
                    playing = true;
                    pause.setText("Pause");
                    playVideo();
                }
            }
        });
            

        slider1 = new JSlider(JSlider.HORIZONTAL, 1, 9000, 1);
        slider1.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                int num = ((JSlider)e.getSource()).getValue();
                im1.updateFrame(num);
                showIms(primaryFile, num, im1, currentFrame1);
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
        frame.add(back, c);

        c.gridx = 1;
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

        c.gridy = 5;
        frame.add(pause, c);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		Player p = new Player();
		p.createGUI();
	}
}
