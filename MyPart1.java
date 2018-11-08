
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

public class MyPart1 {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage ogImg;
	BufferedImage scaledImg;
	int width = 512;
	int height = 512;

	// Draws a black line on the given buffered image from the pixel defined by (x1, y1) to (x2, y2)
	public void drawLine(BufferedImage image, int x1, int y1, int x2, int y2) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));
		g.drawLine(x1, y1, x2, y2);
		g.drawImage(image, 0, 0, null);
	}

	// Taken from https://stackoverflow.com/questions/28162488/get-average-color-on-bufferedimage-and-bufferedimage-portion-as-fast-as-possible
	public int averageColor(BufferedImage bi, int x0, int y0, int w, int h) {
	    int x1 = x0 + w;
	    int y1 = y0 + h;
	    long sumr = 0, sumg = 0, sumb = 0;
	    for (int x = x0; x < x1; x++) {
	        for (int y = y0; y < y1; y++) {
	            Color pixel = new Color(bi.getRGB(x, y));
	            sumr += pixel.getRed();
	            sumg += pixel.getGreen();
	            sumb += pixel.getBlue();
	        }
	    }
	    int num = w * h;
	    Color newColor = new Color((int)(sumr / num), (int)(sumg / num), (int)(sumb / num));
	    return newColor.getRGB();
	}

	public BufferedImage drawImageSquare(int n, double scale, boolean antialiasing) {
		int scaledHeight = (int)(height * 1 / scale);
		int scaledWidth = (int)(width * 1 / scale);
		BufferedImage newImg = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);

		if (!antialiasing) {
			int ind = 0;
			for(int y = 0; y < scaledHeight; y++){

				for(int x = 0; x < scaledWidth; x++){

					// byte a = (byte) 255;
					byte r = (byte) 255;
					byte g = (byte) 255;
					byte b = (byte) 255;

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					newImg.setRGB(x,y,pix);
					ind++;
				}
			}

			drawLine(newImg, 0, 0, scaledWidth-1, 0); // top edge
			drawLine(newImg, 0, 0, 0, scaledHeight-1); // left edge
			drawLine(newImg, 0, scaledHeight-1, scaledWidth-1, scaledHeight-1);	// bottom edge
			drawLine(newImg, scaledWidth-1, scaledHeight-1, scaledWidth-1, 0); 	// right edge

			for (double angle = 0.0; angle < 360.0; angle += (360.0 / (double)n)) {
				double radians = Math.toRadians(angle);
				double xcoord = scaledWidth * (0.5 + Math.cos(radians));
				double ycoord = scaledHeight * (0.5 - Math.sin(radians));
				drawLine(newImg, scaledWidth / 2, scaledHeight / 2, (int)xcoord, (int)ycoord);
			}
		}
		else {
			for(int y = 0; y < scaledHeight; y++){
				for(int x = 0; x < scaledWidth; x++){
					int averaged_pixel = averageColor(ogImg, x*(int)scale, y*(int)scale, (int)scale, (int)scale);
					newImg.setRGB(x,y,averaged_pixel);
				}
			}
		}

		return newImg;
	}

	public void showIms(String[] args){

		int n = 1;
		double scale = 1.0;
		boolean antialiasing = false;

		// Read a parameter from command line
		if (args.length > 0) { n = Integer.parseInt(args[0]); }
		if (args.length > 1) { scale = Double.parseDouble(args[1]); }
		if (args.length > 2) { antialiasing = (Integer.parseInt(args[2]) == 1); }
		System.out.println(n + " " + scale + " " + antialiasing);

		// Initialize a plain white image
		ogImg = drawImageSquare(n, 1.0, false);
		scaledImg = drawImageSquare(n, scale, antialiasing);

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original image (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Image after modification (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);
		lbIm1 = new JLabel(new ImageIcon(ogImg));
		lbIm2 = new JLabel(new ImageIcon(scaledImg));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(10,10,0,0);
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0,100,0,10);
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		frame.getContentPane().add(lbText2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10,10,10,0);
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,100,10,10);
		c.gridx = 1;
		c.gridy = 1;
		frame.getContentPane().add(lbIm2, c);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		MyPart1 ren = new MyPart1();
		ren.showIms(args);
	}

}