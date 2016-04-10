package ce325.hw1;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.annotation.*;
import javax.imageio.ImageIO;

public class SeamCarver {

	public int[][] image;

	//Constractors for different input methods
	public SeamCarver(java.awt.image.BufferedImage image) throws IOException{
		//Take BufferedImage and create RGB and energy tables
		BufferedImage newInput = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D temp = newInput.createGraphics();
		temp.drawImage(image, 0, 0, null);
		temp.dispose();
	};
	public SeamCarver(java.io.File file) throws IOException{
		//Use file io to open the image
		//Convert File->BufferedImage and call said Constractor with this
		BufferedImage input = ImageIO.read(file);
		BufferedImage newInput = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D temp = newInput.createGraphics();
		temp.drawImage(input, 0, 0, null);
		temp.dispose();
		System.out.println("Hello from File " + input.getType() + " " + newInput.getType() );
	};

	public SeamCarver(java.net.URL url) throws IOException{
		//Download image from site
		//Convert url->BufferedImage and call said Constractor with this
		BufferedImage input = ImageIO.read(url);
		BufferedImage newInput = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D temp = newInput.createGraphics();
		temp.drawImage(input, 0, 0, null);
		temp.dispose();

		//Oputputs a buffered image to a file Use for final output
		/*File outputFile = new File("saved.png");
		ImageIO.write(newInput, "png", outputFile); */

		System.out.println("Hello from URL " + input.getType() + " " + newInput.getType());
	};

	public double energy(int row, int col){
		return 0;
	};  // energy of a pixel
	public int[] findHorizontalSeam(){
		int a[]={0};
		return a;
	};       // return horizontal seam
	public int[] findVerticalSeam(){
		int a[]={0};
		return a;
	};         // return vertical seam
	public void removeHorizontalSeam(int[] seam){}; // remove the seam
	public void removeVerticalSeam(int[] seam){};   // remove the seam
	private void scale(int width, int height){};    // scale to the optimal
	                                                // dimensions before applying
	                                                 // SeamCarve algorithm

	public static void main(String[] args) {

		boolean flag = true;

		System.out.print("Welcome to Image Resizer 3000 \n");

		Scanner errorInput = new Scanner(System.in);
		String path = new String();
		try{
			path = args[0];
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.print("Please enter a valid path or URL: ");
			path = errorInput.next();
		}
		while(flag){
			try {
				java.net.URL userInput = new java.net.URL(path);
				SeamCarver userImage = new SeamCarver(userInput);
				flag = false;
			} catch(Exception ex) {
				//ex.printStackTrace();
				System.out.println("\nNo URL detected scanning for file (" + ex.getMessage() + ")" );
				try {
					java.io.File userInput = new java.io.File(path);
					SeamCarver userImage = new SeamCarver(userInput);
					flag = false;
				} catch(Exception ex2) {
					//ex2.printStackTrace();
					System.out.println("No file detected (" + ex2.getMessage() + ")" + "\n");
					System.out.print("Couln't retrieve an image from either source\nPlease provide new path to an image: ");
					path = errorInput.next();
					//Loop for user input
				}
			}
		}

	}
}
