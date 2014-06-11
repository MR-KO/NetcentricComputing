/* Code for opening, displaying, and splitting an image from file. */
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;



public class ImageHandler {
	BufferedImage img = null;
	String imgName = null;
	int width = -1;
	int height = -1;

	/* Returns -1 if no image has been opened. */
	public int getWidth() {
		return width;
	}

	/* Returns -1 if no image has been opened. */
	public int getHeight() {
		return height;
	}

	/* Opens an image from a given path. */
	public boolean open(String path) {
		try {
			img = ImageIO.read(new File(path));
			imgName = path;
			width = img.getWidth();
			height = img.getHeight();
		} catch (IOException e) {
			System.out.println("Failed to open image!");
			return false;
		}

		return true;
	}

	public void displayImgInNewWindow() {
		displayImgInNewWindow(img, imgName);
	}

	/* Displays the image in a new window */
	public static void displayImgInNewWindow(Image img, String windowName) {
		if (img != null) {
			/* Create a display window. */
			JFrame frame = new JFrame(windowName);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			/* Create an imageIcon and label. */
			ImageIcon imgIcon = new ImageIcon(img);
			JLabel label = new JLabel();

			/* Put the icon on the label, and add it to the frame. */
			label.setIcon(imgIcon);
			frame.getContentPane().add(label, BorderLayout.CENTER);

			/* Display the image. */
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}
	}

	/*
		Splits the image in a number of smaller images, which combined together
		appropriately, make the original image. The minimum values for width and
		height are 1, and the maximum values are the width and height of the
		image, respectively. Columns are interpreted as being the height, and
		rows being the width.
	*/
	public BufferedImage[] splitImg(int cols, int rows) {
		/* Bounds check... */
		if (cols < 1 || cols > height || rows < 1 || rows > width) {
			return null;
		}

		/* What if the width and height are not evenly divisible by cols and rows? */
		// TODO: MURT!
		int parts = cols * rows;
		int partWidth = width / cols;
		int partHeight = height / cols;
		// System.out.println("Parts = " + parts + ", width = " + partWidth +
		// 	", height = " + partHeight);

		/*
			Create an array of BufferedImages and store a part of each image in
			each element of it, then return that array.
		*/
		int index = 0;
		BufferedImage[] splittedImages = new BufferedImage[parts];

		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < cols; y++) {
				/* Split the image into parts and display them. */
				splittedImages[index] = new BufferedImage(partWidth, partHeight, img.getType());

				/* Fill the splitted image with a part of the original image. */
				Graphics2D graphics = splittedImages[index].createGraphics();
				graphics.drawImage(img,
					0, 0, partWidth, partHeight,
					partWidth * y, partHeight * x, partWidth * (y + 1), partHeight * (x + 1),
					null);
				graphics.dispose();

				/* Optionally, display the image. */
				// displayImgInNewWindow(splittedImages[index], "Img" + index);
				index++;
			}
		}

		return splittedImages;
	}

	/* Assembles a split image back into its original version. */
	public static BufferedImage assemble(BufferedImage[] imgs, int cols, int rows) {
		/* Checks... */
		if (imgs == null || imgs.length != cols * rows || cols < 1 || rows < 1) {
			return null;
		}

		/* What if the width and height are not evenly divisible by cols and rows? */
		// TODO: MURT!
		int parts = cols * rows;
		int partWidth = imgs[0].getWidth();
		int partHeight = imgs[0].getHeight();
		// System.out.println("Parts = " + parts + ", width = " + partWidth +
		// 	", height = " + partHeight);

		/* Re-create the final image with the correct size. */
		BufferedImage finalImg = new BufferedImage(partWidth * cols, partHeight * rows,
			imgs[0].getType());

		/* Fill the final image with all the parts. */
		int index = 0;

		/* Make sure to reverse the order used in splitting... */
		for (int y = 0; y < cols; y++) {
			for (int x = 0; x < rows; x++) {
				Graphics2D graphics = finalImg.createGraphics();
				graphics.drawImage(imgs[index],
					partWidth * x, partHeight * y, partWidth * (x + 1), partHeight * (y + 1),
					0, 0, partWidth, partHeight,
					null);
				graphics.dispose();
				index++;
			}
		}

		/* Optionally, display the assembled image. */
		displayImgInNewWindow(finalImg, "Assembled image");
		return finalImg;
	}


	/* Opens a test image stress.png, and splits it in 3x3 and assembles it back. */
	public static void main(String[] args) throws IOException {
		System.out.println("Displaying image...");
		ImageHandler test = new ImageHandler();

		String path = "stress.png";
		String filename = "stress";
		String ext = "png";

		/* Parse optional commandline argument containing file. */
		if (args.length == 1) {
			path = args[0];
			String[] temp = path.split("\\.");
			ext = temp[temp.length - 1];
			filename = path.replace("." + ext, "");
		}

		if (!test.open(path)) {
			return;
		}

		test.displayImgInNewWindow();

		System.out.println("Splitting image...");
		BufferedImage[] imgs = test.splitImg(3, 3);
		System.out.println("Done, saving to files...");

		/* Optionally, save each separate image. */
		if (imgs != null) {
			for (int i = 0; i < imgs.length; i++) {
				try {
					ImageIO.write(imgs[i], ext, new File(filename + "_splitted_" + i + "." + ext));
				} catch(IOException e) {
					System.out.println("Failed to write output file!\n" + e.getMessage());
				}
			}
		}

		System.out.println("Done, re-assembling image...");
		BufferedImage img = ImageHandler.assemble(imgs, 3, 3);
		System.out.println("MURT!");
	}
}
