
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;

public class MyCompression {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;

	BufferedImage ogImg;
	BufferedImage modifiedImg;
	
	int width = 352;
	int height = 288;

	private BufferedImage drawRawImg(byte[] bytes) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);

		int count = 0; 
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
		    	byte b = bytes[count++];
		    	int pix = 0x00000000 | ((b & 0xff) << 16) | ((b & 0xff) << 8) | (b & 0xff);
		    	
		    	img.setRGB(x, y, pix);
		    }
		}

		System.out.println("Read the .raw image file");
		return img;
	}

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

		System.out.println("Read the .rgb image file");
		return img;
	}

	private double getDistance(raw a, raw b) {
		double diff_square_sum = 0.0;
        for (int i = 0; i < a.getArray().length; i++) {
            diff_square_sum += Math.pow((a.get(i) - b.get(i)), 2);
        }
        return Math.sqrt(diff_square_sum);
	}

	private double getDistance(rgb a, rgb b) {
		double diff_square_sum = 0.0;
		for (int i = 0; i < a.getArray().length; i++) {
			diff_square_sum += Math.pow((a.get(i, 0) - b.get(i, 0)), 2);
			diff_square_sum += Math.pow((a.get(i, 1) - b.get(i, 1)), 2);
			diff_square_sum += Math.pow((a.get(i, 2) - b.get(i, 2)), 2);
		}
		return Math.sqrt(diff_square_sum);
	}

	private void vectorAdd(int[] a, int[] b) {
		for (int i = 0; i < a.length; i++) {
			a[i] += b[i];
		}
	}

	private void vectorAdd(int[][] a, int[][] b) {
		for (int i = 0; i < a.length; i++) {
			a[i][0] += b[i][0];
			a[i][1] += b[i][1];
			a[i][2] += b[i][2];
		}
	}

	private ArrayList<raw> assignClustersRaw(ArrayList<raw> clusters, 
		ArrayList< ArrayList<Integer> > cluster_vals, ArrayList<raw> pixels) {
		for (int i = 0; i < cluster_vals.size(); i++) { cluster_vals.get(i).clear(); }

		for (int i = 0; i < pixels.size(); i++) {
			double least = -1;
			int closest_index = -1;
			// System.out.println("Pixel val of " + pixels.get(i).toString());
			for (int j = 0; j < clusters.size(); j++) {
				double new_distance = getDistance(clusters.get(j), pixels.get(i));
				// System.out.println("new distance " + new_distance + " index of " + closest_index);
				if (new_distance < least || least == -1) {
					least = new_distance;
					closest_index = j;
				}
			}
			// System.out.println("Adding " + i + " to index " + closest_index);
			cluster_vals.get(closest_index).add(i);
		}
		// for (ArrayList<Integer> vals : cluster_vals) { System.out.print(vals.size() + " "); }
		// System.out.println("");

		ArrayList<raw> newClusters = new ArrayList<raw>();
		for (int i = 0; i < clusters.size(); i++) {
			ArrayList<Integer> vals = cluster_vals.get(i);
			if (vals.size() == 0) {
				newClusters.add(clusters.get(i));
			}
			else {
				int[] sum = pixels.get(vals.get(0)).getArray();
				for (int j = 1; j < vals.size(); j++) {
					vectorAdd(sum, pixels.get(vals.get(j)).getArray());
				}
				for (int j = 0; j < sum.length; j++) { sum[j] = sum[j] / vals.size(); }
				newClusters.add(new raw(sum));
			}
		}
		return newClusters;
	}

	private void quantizeRaw(byte[] bytes, int num_clusters, int mode) {
		// format initial data
		ArrayList<raw> pixels = new ArrayList<raw>();
		if (mode == 1) {
			for (int i = 0; i < bytes.length; i += 2) {
				pixels.add(new raw(new int[]{ (bytes[i] & 0xff), (bytes[i + 1] & 0xff) }));
			}
		}
		if (mode == 2) {
			for (int i = 0; i < height; i += 2) {
				for (int j = 0; j < width; j += 2) {
					int[] pixel = new int[4];
					int ind = 0;
					for (int y = i; y <= i + 1; y++) {
						for (int x = j; x <= j + 1; x++) { pixel[ind++] = (bytes[(y * width) + x] & 0xff); }
					}
					pixels.add(new raw(pixel));
				}
			}
		}
		if (mode == 3) {
			for (int i = 0; i < height; i += 4) {
				for (int j = 0; j < width; j += 4) {
					int[] pixel = new int[16];
					int ind = 0;
					for (int y = i; y <= i + 3; y++) {
						for (int x = j; x <= j + 3; x++) { pixel[ind++] = (bytes[(y * width) + x] & 0xff); }
					}
					pixels.add(new raw(pixel));
				}
			}
		}
		System.out.println(pixels.size());

		// initialize clusters
		ArrayList<raw> clusters = new ArrayList<raw>();
		ArrayList<ArrayList<Integer>> cluster_vals = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < num_clusters; i++) {
			clusters.add(pixels.get((int)(Math.random() * pixels.size())));
			cluster_vals.add(new ArrayList<Integer>());
		}
		// System.out.println(clusters.toString());

		// loops through clustering algorithm
		for (int i = 0; i < 50; i++) {
			clusters = assignClustersRaw(clusters, cluster_vals, pixels);
			System.out.println("Clustering...");
		}
		// System.out.println(clusters.toString() + "\n");

		// assign each pixel in list a cluster 
		int[] coded_pixel_vals = new int[(int) pixels.size()];
		for (int i = 0; i < cluster_vals.size(); i++) {
			for (int j = 0; j < cluster_vals.get(i).size(); j++) {
				coded_pixel_vals[cluster_vals.get(i).get(j)] = i;
			}
		}

		// replace each byte value with cluster value
		for (int i = 0; i < coded_pixel_vals.length; i++) {
			if (mode == 1) {
				bytes[i * 2] = (byte) clusters.get(coded_pixel_vals[i]).get(0);
				bytes[(i * 2) + 1] = (byte) clusters.get(coded_pixel_vals[i]).get(1);
			}
			if (mode == 2) {
				int start = (i % (width / 2)) * 2 + ((i * 2 / width) * width * 2);
				int ind = 0;
				for (int j = start; j < start + (width) + 2; j++) {
					if (j > start && (j - start) % width == 2) j += width - 2;
					bytes[j] = (byte) clusters.get(coded_pixel_vals[i]).get(ind++);
				}
			}
			if (mode == 3) {
				int start = (i % (width / 4)) * 4 + ((i * 4 / width) * width * 4);
				int ind = 0;
				for (int j = start; j < start + (width * 3) + 4; j++) {
					if (j > start && (j - start) % width == 4) j += width - 4;
					bytes[j] = (byte) clusters.get(coded_pixel_vals[i]).get(ind++);
				}
			}
		}
	}

	private ArrayList<rgb> assignClustersRgb(ArrayList<rgb> clusters, 
		ArrayList< ArrayList<Integer> > cluster_vals, ArrayList<rgb> pixels) {
		for (int i = 0; i < cluster_vals.size(); i++) { cluster_vals.get(i).clear(); }

		for (int i = 0; i < pixels.size(); i++) {
			double least = -1;
			int closest_index = -1;
			// System.out.println("Pixel val of " + pixels.get(i).toString());
			for (int j = 0; j < clusters.size(); j++) {
				double new_distance = getDistance(clusters.get(j), pixels.get(i));
				// System.out.println("new distance " + new_distance + " index of " + closest_index);
				if (new_distance < least || least == -1) {
					least = new_distance;
					closest_index = j;
				}
			}
			// System.out.println("Adding " + i + " to index " + closest_index);
			cluster_vals.get(closest_index).add(i);
		}

		ArrayList<rgb> newClusters = new ArrayList<rgb>();
		for (int i = 0; i < clusters.size(); i++) {
			ArrayList<Integer> vals = cluster_vals.get(i);
			if (vals.size() == 0) {
				newClusters.add(clusters.get(i));
			}
			else {
				int[][] sum = pixels.get(vals.get(0)).getArray();
				for (int j = 1; j < vals.size(); j++) {
					vectorAdd(sum, pixels.get(vals.get(j)).getArray());
				}
				for (int j = 0; j < sum.length; j++) { 
					sum[j][0] = sum[j][0] / vals.size(); 
					sum[j][1] = sum[j][1] / vals.size(); 
					sum[j][2] = sum[j][2] / vals.size(); 
				}
				newClusters.add(new rgb(sum));
			}
		}
		return newClusters;
	}

	private void quantizeRgb(byte[] bytes, int num_clusters, int mode) {
		// format initial data
		ArrayList<rgb> pixels = new ArrayList<rgb>();
		int num_pixels = width * height;

		if (mode == 1) {
			for (int i = 0; i < num_pixels; i += 2) {
				int[][] pixel = new int[2][3];
				int ind = 0;
				for (int x = i; x <= i + 1; x++) { 
					pixel[ind++] = new int[]{ (bytes[x] & 0xff), (bytes[x + num_pixels] & 0xff), (bytes[x + (num_pixels * 2)] & 0xff) }; 
				}
				pixels.add(new rgb(pixel));
			}
		}
		if (mode == 2) {
			for (int i = 0; i < height; i += 2) {
				for (int j = 0; j < width; j += 2) {
					int[][] pixel = new int[4][3];
					int ind = 0;
					for (int y = i; y <= i + 1; y++) {
						for (int x = j; x <= j + 1; x++) { 
							int index = (y * width) + x;
							pixel[ind++] = new int[]{ (bytes[index] & 0xff), (bytes[index + num_pixels] & 0xff), (bytes[index + (num_pixels * 2)] & 0xff) };
						}
					}
					pixels.add(new rgb(pixel));
				}
			}
		}
		if (mode == 3) {
			for (int i = 0; i < height; i += 4) {
				for (int j = 0; j < width; j += 4) {
					int[][] pixel = new int[16][3];
					int ind = 0;
					for (int y = i; y <= i + 3; y++) {
						for (int x = j; x <= j + 3; x++) { 
							int index = (y * width) + x;
							pixel[ind++] = new int[]{ (bytes[index] & 0xff), (bytes[index + num_pixels] & 0xff), (bytes[index + (num_pixels * 2)] & 0xff) };
						}
					}
					pixels.add(new rgb(pixel));
					// System.out.println((i * width + j) + " " + Arrays.deepToString(pixel));
				}
			}
		}
		System.out.println(pixels.size());

		// initialize clusters
		ArrayList<rgb> clusters = new ArrayList<rgb>();
		ArrayList<ArrayList<Integer>> cluster_vals = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < num_clusters; i++) {
			clusters.add(pixels.get((int)(Math.random() * pixels.size())));
			cluster_vals.add(new ArrayList<Integer>());
		}
		// System.out.println(clusters.toString());

		// loops through clustering algorithm
		for (int i = 0; i < 50; i++) {
			clusters = assignClustersRgb(clusters, cluster_vals, pixels);
			System.out.println("Clustering...");
		}
		// System.out.println("\n" + clusters.toString());

		// assign each pixel in list a cluster 
		int[] coded_pixel_vals = new int[(int) pixels.size()];
		for (int i = 0; i < cluster_vals.size(); i++) {
			for (int j = 0; j < cluster_vals.get(i).size(); j++) {
				coded_pixel_vals[cluster_vals.get(i).get(j)] = i;
			}
		}
		// System.out.println(Arrays.toString(coded_pixel_vals));

		// replace each byte value with cluster value
		for (int i = 0; i < coded_pixel_vals.length; i++) {
			if (mode == 1) {
				for (int j = (i * 2); j < (i * 2) + 2; j++) {
					bytes[j] = (byte) clusters.get(coded_pixel_vals[i]).get(0, 0);
					bytes[j + num_pixels] = (byte) clusters.get(coded_pixel_vals[i]).get(0, 1);
					bytes[j + (num_pixels * 2)] = (byte) clusters.get(coded_pixel_vals[i]).get(0, 2);
				}
			}
			if (mode == 2) {
				int start = (i % (width / 2)) * 2 + ((i * 2 / width) * width * 2);
				int ind = 0;
				for (int j = start; j < start + (width) + 2; j++) {
					if (j > start && (j - start) % width == 2) j += width - 2;
					bytes[j] = (byte) clusters.get(coded_pixel_vals[i]).get(ind, 0);
					bytes[j + num_pixels] = (byte) clusters.get(coded_pixel_vals[i]).get(ind, 1);
					bytes[j + (num_pixels * 2)] = (byte) clusters.get(coded_pixel_vals[i]).get(ind, 2);
					ind++;
				}
			}
			if (mode == 3) {
				int start = (i % (width / 4)) * 4 + ((i * 4 / width) * width * 4);
				int ind = 0;
				for (int j = start; j < start + (width * 3) + 4; j++) {
					if (j > start && (j - start) % width == 4) j += width - 4;
					bytes[j] = (byte) clusters.get(coded_pixel_vals[i]).get(ind, 0);
					bytes[j + num_pixels] = (byte) clusters.get(coded_pixel_vals[i]).get(ind, 1);
					bytes[j + (num_pixels * 2)] = (byte) clusters.get(coded_pixel_vals[i]).get(ind, 2);
					ind++;
				}
			}
		}
	}

	public void showIms(String[] args) {
		String image_name = "";
		int num_vecs = -1;
		int mode = -1;

		// Read a parameter from command line
		if (args.length > 2) {
			image_name = args[0];
			num_vecs = Integer.parseInt(args[1]);
			mode = Integer.parseInt(args[2]);
		}
		System.out.println(image_name + " " + num_vecs + " " + mode);

		try {
			File file = new File(image_name);
			InputStream fis = new FileInputStream(file);

			long len = file.length();
			int offset = 0;
			int numRead = 0;

			byte[] bytes = new byte[(int) len]; 

			while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			if (image_name.substring(image_name.length() - 3).equals("raw")) {
				ogImg = drawRawImg(bytes);
				quantizeRaw(bytes, num_vecs, mode);
				modifiedImg = drawRawImg(bytes);
			}
			if (image_name.substring(image_name.length() - 3).equals("rgb")) {
				ogImg = drawRgbImg(bytes);
				quantizeRgb(bytes, num_vecs, mode);
				modifiedImg = drawRgbImg(bytes);
			}

			fis.close();
		} 
		catch(IOException e) {
			System.out.println("Error: "+e);
		}

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original image (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Image after modification (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);
		lbIm1 = new JLabel(new ImageIcon(ogImg));
		lbIm2 = new JLabel(new ImageIcon(modifiedImg));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(10,0,10,10);
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0,100,0,0);
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		frame.getContentPane().add(lbText2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,10,10,0);
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


	private class raw {
		int[] vals;

		public raw(int[] vals_) {
			vals = vals_;
		}

		public int get(int i) {
			return vals[i];
		}

		public int[] getArray() {
			return vals;
		}

		public String toString() {
			return Arrays.toString(vals);
		}
	}

	private class rgb {
		int[][] vals;

		public rgb(int[][] vals_) {
			vals = vals_;
		}

		public int get(int i1, int i2) {
			return vals[i1][i2];
		}

		public int[][] getArray() {
			return vals;
		}

		public String toString() {
			return Arrays.deepToString(vals);
		}
	}

	public static void main(String[] args) {
		MyCompression ren = new MyCompression();
		ren.showIms(args);
	}
}