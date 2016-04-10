package ce325.hw1;

import java.io.*;
import java.net.*;

public class SeamCarver {

	public int[][] image;

	//Constractors for different input methods
	public SeamCarver(java.awt.image.BufferedImage image) throws IOException{
		//Take BufferedImage and create RGB and energy tables
	};
	public SeamCarver(java.io.File file) throws IOException{
		//Use file io to open the image
		//Convert File->BufferedImage and call said Constractor with this
		System.out.println("Hello from File");
	};

	public SeamCarver(java.net.URL url) throws IOException{
		//Download image from site
		//Convert url->BufferedImage and call said Constractor with this
		System.out.println("Hello from URL");
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

		try{

			java.net.URL userInput = new java.net.URL(args[0]);
			SeamCarver userImage = new SeamCarver(userInput);
		}catch(Exception ex){
			ex.printStackTrace();
			try{
				java.io.File userInput = new java.io.File(args[0]);
				SeamCarver userImage = new SeamCarver(userInput);
			}catch(Exception ex2){
				ex2.printStackTrace();
				System.out.println("No valid input given");
				//Loop for user input
			}
		}

	}
}
