package ce325.hw1;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.annotation.*;
import javax.imageio.ImageIO;

public class SeamCarver {

	public static BufferedImage newInput;
	public int[][] image;

	//Constractors for different input methods
	public SeamCarver(java.awt.image.BufferedImage image) throws IOException{
		//Take BufferedImage and create RGB and energy tables
		newInput = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D temp = newInput.createGraphics();
		temp.drawImage(image, 0, 0, null);
		temp.dispose();
	};

	public SeamCarver(java.io.File file) throws IOException{
		//Use file io to open the image
		//Convert File->BufferedImage and call said Constractor with this
		BufferedImage input = ImageIO.read(file);
		newInput = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D temp = newInput.createGraphics();
		temp.drawImage(input, 0, 0, null);
		temp.dispose();
		//System.out.println("Hello from File " + input.getType() + " " + newInput.getType() );
	};

	public SeamCarver(java.net.URL url) throws IOException{
		//Download image from site
		//Convert url->BufferedImage and call said Constractor with this
		BufferedImage input = ImageIO.read(url);
		newInput = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D temp = newInput.createGraphics();
		temp.drawImage(input, 0, 0, null);
		temp.dispose();

		//Oputputs a buffered image to a file Use for final output
		/*File outputFile = new File("saved.png");
		ImageIO.write(newInput, "png", outputFile); */

		//System.out.println("Hello from URL " + input.getType() + " " + newInput.getType());
	};

	// energy of a pixel
	public double energy(int row, int col){
		return 0;
	};

	// return horizontal seam
	public int[] findHorizontalSeam(){
		int a[]={0};
		return a;
	};

	// return vertical seam
	public int[] findVerticalSeam(){
		int a[]={0};
		return a;
	};

	// remove the seam
	public void removeHorizontalSeam(int[] seam){};

	// remove the seam
	public void removeVerticalSeam(int[] seam){};

	/* scale to the optimal
	dimensions before applying
	SeamCarve algorithm */
	private void scale(int width, int height){

	};

	/* Apply scale first
	and seam carve afterwards */
	public void seamCarve(int width, int height){
		double ratio;
		int optimalWidth, optimalHeight;

		ratio = newInput.getWidth() / newInput.getHeight();

		if( ((int) ( width/ratio )) < height ) {
			optimalWidth = (int) (ratio * height);
			this.scale(optimalWidth,height);
		} else {
			optimalHeight = (int) ( width/ratio );
			this.scale(width,optimalHeight);
		}

	};

	public static void main(String[] args) {

		boolean flag = true;
		int newWidth, newHeight;
		SeamCarver userImage = null;

		System.out.print("Welcome to Image Resizer 3000 \n");

		Scanner userInput = new Scanner(System.in);
		String path = new String();
		try{
			path = args[0];
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.print("Please enter a valid path or URL: ");
			path = userInput.next();
		}
		while(flag){
			try {
				java.net.URL imageInput = new java.net.URL(path);
				userImage = new SeamCarver(imageInput);
				flag = false;
			} catch(Exception ex) {
				//ex.printStackTrace();
				System.out.println("\nNo URL detected scanning for file (" + ex.getMessage() + ")" );
				try {
					java.io.File imageInput = new java.io.File(path);
					userImage = new SeamCarver(imageInput);
					flag = false;
				} catch(Exception ex2) {
					//ex2.printStackTrace();
					System.out.println("No file detected (" + ex2.getMessage() + ")" + "\n");
					System.out.print("Couln't retrieve an image from either source\nPlease provide new path to an image: ");
					path = userInput.next();
					//Loop for user input
				}//2nd catch end
			}//1st catch end
		}//While flag end

		System.out.println("The image you imported is: " + newInput.getWidth() + "x" + newInput.getHeight());
		System.out.print("Enter resizing dimensions\nEnter width: ");
		newWidth = userInput.nextInt();

		while(newWidth <= 0){
			System.out.print("Please enter correct width (width > 0): ");
			newWidth = userInput.nextInt();
		}

		System.out.print("Enter height: ");
		newHeight = userInput.nextInt();

		while(newHeight <= 0){
			System.out.print("Please enter correct height (height > 0): ");
			newHeight = userInput.nextInt();
		}

		//SeamCarver.findScale(newWidth, newHeight);
		userImage.seamCarve(newWidth, newHeight);

	}//Main end
}//Class end
