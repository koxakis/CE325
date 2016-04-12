package ce325.hw1;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.*;
import java.awt.*;
import java.io.*;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import javax.annotation.*;
import javax.imageio.ImageIO;
import java.util.Arrays;

public class SeamCarver {

	public static BufferedImage newInput;
	public int[] pixelMap;
	public double[][] energyMap;

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
	};

	public SeamCarver(java.net.URL url) throws IOException{
		//Download image from site
		//Convert url->BufferedImage and call said Constractor with this
		BufferedImage input = ImageIO.read(url);
		newInput = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D temp = newInput.createGraphics();
		temp.drawImage(input, 0, 0, null);
		temp.dispose();

	};

	// energy of a pixel
	public double energy(int row, int col){

		int posN, posS, posE, posW;
		double energyX, energyY, energyOverall;

		posW = (row*newInput.getWidth()) + ( ( (col + newInput.getWidth() ) - 1) % newInput.getWidth());
		posE = (row*newInput.getWidth()) + ( (col + 1) % newInput.getWidth() );
		posN = ( ( ( (row + newInput.getHeight() ) - 1) % newInput.getHeight() ) * newInput.getWidth() ) + col ;
		posS = ( ( (row + 1) % newInput.getHeight() ) * newInput.getWidth() ) + col ;

		//E​x(i,j) = [R(i,j+1) - R(i,j-­1)]^2​+ [G(i,j+1) ­- G(i,j-­1)]^2​ + [B(i,j+1) ­- B(i,j­-1)]^2​
		energyX = Math.pow( ((pixelMap[posW] >> 16) & 0xFF) - ((pixelMap[posE] >> 16) & 0xFF), 2 ) +
					Math.pow( ((pixelMap[posW] >> 8) & 0xFF) - ((pixelMap[posE] >> 8) & 0xFF), 2 ) +
					Math.pow( ((pixelMap[posW] >> 0) & 0xFF) - ((pixelMap[posE] >> 0) & 0xFF), 2 );

		//E​y(i,j) = [R(i+1,j) - R(i-1,j)]^2​+ [G(i+1,j) - G(i-1,j)]^2​ + [B(i+1,j) - B(i-1,j)]^2​
		energyY = Math.pow( ((pixelMap[posS] >> 16) & 0xFF) - ((pixelMap[posN] >> 16) & 0xFF), 2 ) +
					Math.pow( ((pixelMap[posS] >> 8) & 0xFF) - ((pixelMap[posN] >> 8) & 0xFF), 2 ) +
					Math.pow( ((pixelMap[posS] >> 0) & 0xFF) - ((pixelMap[posN] >> 0) & 0xFF), 2 );

		energyOverall = energyX + energyY;

		return energyOverall;
	};

	// return horizontal seam
	public int[] findHorizontalSeam(){
		int[] seam = null;
		int i, j;
		double minEnergy;

		minEnergy = energyMap[0][0];
		seam[0] = 0;
		for( i=1; i<newInput.getHeight() ; i++){
			if (energyMap[i][0] < minEnergy) {
				minEnergy = energyMap[i][0];
				seam[0] = i;
			}
		}

		return seam;
	};

	// return vertical seam
	public int[] findVerticalSeam(){
		int[] seam = new int[newInput.getHeight()];
		int i, j;
		double minEnergy;

		minEnergy = energyMap[0][0];
		seam[0] = 0;
		for( j=1; j<newInput.getWidth() ; j++){
			if (energyMap[0][j] < minEnergy) {
				minEnergy = energyMap[0][j];
				seam[0] = j;
			}
		}

		for( i=1; i<newInput.getHeight(); i++){
			minEnergy = energyMap[i][(seam[i-1] + newInput.getWidth() - 1) % newInput.getWidth()];
			seam[i] = (seam[i-1] + newInput.getWidth() - 1) % newInput.getWidth();
			j=seam[i-1];
			while(j != (seam[i-1] + 1) % newInput.getWidth()){
				if (energyMap[i][j] < minEnergy) {
					minEnergy = energyMap[i][j];
					seam[0] = j;
				}
				j = (j + 1) % newInput.getWidth();
			}
		}

		return seam;
	};

	// remove the seam
	public void removeHorizontalSeam(int[] seam){};

	// remove the seam
	public void removeVerticalSeam(int[] seam){};

	/* scale to the optimal
	dimensions before applying
	SeamCarve algorithm */
	private void scale(int width, int height){
		BufferedImage oldImage = newInput;
		newInput = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D temp = newInput.createGraphics();
		temp.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		temp.drawImage(oldImage, 0, 0, width, height,null);
		temp.dispose();

		//Oputputs a buffered image to a file Use for final output
		try{
			File outputFile = new File("saved.png");
			ImageIO.write(newInput, "png", outputFile);
		} catch(IOException e) {

		}


	};

	/* Apply scale first
	and seam carve afterwards */
	public void seamCarve(int width, int height){
		double ratio;
		int optimalWidth, optimalHeight;
		int cuttingDimension, currDimension;
		int[] seam;

		energyMap = new double[newInput.getWidth()][newInput.getHeight()];

		ratio = (double) newInput.getWidth() / (double) newInput.getHeight();
		//System.out.println("The ratio is: " + ratio);

		if( ((int) ( width/ratio )) < height ) {
			optimalWidth = (int) (ratio * height);
			System.out.println("\nScaling down to " + optimalWidth + "x" + height + " for optimal resaults");
			this.scale(optimalWidth,height);
		} else {
			optimalHeight = (int) ( width/ratio );
			System.out.println("\nScaling down to " + width + "x" + optimalHeight + " for optimal resaults");
			this.scale(width,optimalHeight);
		}

		if (newInput.getWidth() > width){
			cuttingDimension = width;
			currDimension = newInput.getWidth();
		} else {
			cuttingDimension = height;
			currDimension = newInput.getHeight();
		}

		while ( currDimension > cuttingDimension ) {
			pixelMap = ((DataBufferInt)newInput.getRaster().getDataBuffer()).getData();

			for (int i=0; i < newInput.getHeight(); i++){
				for (int j=0; j < newInput.getWidth(); j++){
					energyMap[i][j] = this.energy(i,j);
					//System.out.println(energyMap[i][j]);
				}
			}

			if (newInput.getWidth() > width){
				seam = this.findVerticalSeam();
				System.out.println("Seam " + Arrays.toString(seam));
				//this.removeVerticalSeam(seam);
				//currDimension = newInput.getWidth();
			} else {
				seam = this.findHorizontalSeam();
				System.out.println("Seam " + seam);
				//this.removeHorizontalSeam(seam);
				//currDimension = newInput.getHeight();
			}
			currDimension--;


		}//While end

	};

	public static void main(String[] args) {

		boolean flag = true;
		int newWidth, newHeight;
		SeamCarver userImage = null;
		File targetFile;

		Scanner userInput = new Scanner(System.in);
		String path = new String();

		System.out.print("Welcome to Image Resizer 3000 \n");

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
		System.out.print("\nEnter resizing dimensions\nEnter width: ");
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

		System.out.print("\nEnter a file name for the resized image (ending in *.png): ");
		path = userInput.next();

		while (!path.toLowerCase().endsWith(".png")) {
			System.out.print("\nFile name entered is not a *.png\nPlease enter new file name: ");
			path = userInput.next();
		}

		targetFile = new File(path);

		if ( targetFile.exists() ){
			System.out.println("\nFile name " + targetFile.getName() + " already exists");
		}else{
			userImage.seamCarve(newWidth, newHeight);
		}

	}//Main end
}//Class end
