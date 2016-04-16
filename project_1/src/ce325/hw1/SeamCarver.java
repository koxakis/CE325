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
import java.util.InputMismatchException;
import javax.annotation.*;
import javax.imageio.ImageIO;
import java.util.Arrays;

public class SeamCarver {

	public static BufferedImage importedImage;
	public int[] pixelMap;
	public double[][] energyMap;

	//Constructors for different input methods
	public SeamCarver(java.awt.image.BufferedImage image) throws IOException{
		//Take BufferedImage and create RGB and energy tables
		importedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

		//Drawing image to a buffered image format
		Graphics2D temp = importedImage.createGraphics();
		temp.drawImage(image, 0, 0, null);
		temp.dispose();
	};

	public SeamCarver(java.io.File file) throws IOException{
		//Use file io to open the image
		BufferedImage input = ImageIO.read(file);
		importedImage = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

		//Drawing image to a buffered image format
		Graphics2D temp = importedImage.createGraphics();
		temp.drawImage(input, 0, 0, null);
		temp.dispose();
	};

	public SeamCarver(java.net.URL url) throws IOException{
		//Download image from site
		BufferedImage input = ImageIO.read(url);
		importedImage = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

		//Drawing image to a buffered image format
		Graphics2D temp = importedImage.createGraphics();
		temp.drawImage(input, 0, 0, null);
		temp.dispose();

	};

	//Find the energy of a pixel and return overall energy
	public double energy(int row, int col){

		int posN, posS, posE, posW;
		double energyX, energyY, energyOverall;

		//Using modulo in order to transverse each line/column circularly
		posW = (row*importedImage.getWidth()) + ( ( (col + importedImage.getWidth() ) - 1) % importedImage.getWidth());
		posE = (row*importedImage.getWidth()) + ( (col + 1) % importedImage.getWidth() );
		posN = ( ( ( (row + importedImage.getHeight() ) - 1) % importedImage.getHeight() ) * importedImage.getWidth() ) + col ;
		posS = ( ( (row + 1) % importedImage.getHeight() ) * importedImage.getWidth() ) + col ;

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

	//Return horizontal seam
	public int[] findHorizontalSeam(){
		int[] seam = new int[importedImage.getWidth()];
		int[] minSeam = new int[importedImage.getWidth()];
		int i, j, k, flag = 0 , flag2 = 0;
		double minEnergy, seamSum, minSeamSum = 0;

		//Sets the seam's starting point starting from every pixel on the first row
		for (i=0; i<importedImage.getHeight(); i++) {
			minEnergy = energyMap[i][0];
			seam[0] = i;
			seamSum = energyMap[i][0];

			//Chooses the pixel with the least energy according to the Seam Carving algorithm
			for( j=1; j<importedImage.getWidth(); j++){

				//Flag2 dictates if the pixel is located to either end of the energy matrix
				flag2 = 0;
				minEnergy = energyMap[seam[j-1]][j];
				seam[j] = seam[j-1];
				if ( seam[j]-1 < 0 ){
					if( energyMap[seam[j-1]+1][j] < minEnergy){
						minEnergy = energyMap[seam[j-1]+1][j];
						seam[j] = seam[j-1] + 1;
					}
					flag2 = 1;
				}
				if ( seam[j]+1 > importedImage.getHeight()){
					if( energyMap[seam[j-1]-1][j] < minEnergy ){
						minEnergy = energyMap[seam[j-1]-1][j];
						seam[j] = seam[j-1] - 1;
					}
					flag2 = 1;
				}
				if (flag2 == 0){
					if( energyMap[seam[j-1]+1][j] < minEnergy){
						minEnergy = energyMap[seam[j-1]+1][j];
						seam[j] = seam[j-1] + 1;
					}
					if( energyMap[seam[j-1]-1][j] < minEnergy ){
						minEnergy = energyMap[seam[j-1]-1][j];
						seam[j] = seam[j-1] - 1;
					}
				}

				seamSum = seamSum + minEnergy;

			}

			//Flag dictates first iteration
			if ((seamSum < minSeamSum) && (flag == 1)) {
				minSeamSum = seamSum;
				minSeam = seam;
			}

			if (flag == 0) {
				minSeamSum = seamSum;
				minSeam = seam;
				flag = 1;
			}
		}//End of outside for loop

		return minSeam;
	};

	//Return vertical seam
	public int[] findVerticalSeam(){
		int[] seam = new int[importedImage.getHeight()];
		int[] minSeam = new int[importedImage.getHeight()];
		int i, j, k, flag = 0 , flag2 = 0;
		double minEnergy, seamSum, minSeamSum = 0;

		//Sets the seam's starting point starting from every pixel on the first row
		for (j=0; j<importedImage.getWidth(); j++) {
			minEnergy = energyMap[0][j];
			seam[0] = j;
			seamSum = energyMap[0][j];

			//Chooses the pixel with the least energy according to the Seam Carving algorithm
			for( i=1; i<importedImage.getHeight(); i++){

				//Flag2 dictates if the pixel is located to either end of the energy matrix
				flag2 = 0;
				minEnergy = energyMap[i][seam[i-1]];
				seam[i] = seam[i-1];
				if ( seam[i]-1 < 0 ){
					if( energyMap[i][seam[i-1]+1] < minEnergy){
						minEnergy = energyMap[i][seam[i-1] + 1];
						seam[i] = seam[i-1] + 1;
					}
					flag2 = 1;
				}
				if ( seam[i]+1 > importedImage.getWidth()){
					if( energyMap[i][seam[i-1]-1] < minEnergy ){
						minEnergy = energyMap[i][seam[i-1] - 1];
						seam[i] = seam[i-1] - 1;
					}
					flag2 = 1;
				}
				if (flag2 == 0){
					if( energyMap[i][seam[i-1]+1] < minEnergy){
						minEnergy = energyMap[i][seam[i-1] + 1];
						seam[i] = seam[i-1] + 1;
					}
					if( energyMap[i][seam[i-1]-1] < minEnergy ){
						minEnergy = energyMap[i][seam[i-1] - 1];
						seam[i] = seam[i-1] - 1;
					}
				}

				seamSum = seamSum + minEnergy;

			}

			//Flag dictates first iteration
			if ((seamSum < minSeamSum) && (flag == 1)) {
				minSeamSum = seamSum;
				minSeam = seam;
			}

			if (flag == 0) {
				minSeamSum = seamSum;
				minSeam = seam;
				flag = 1;
			}
		}//End of outside for loop

		return minSeam;
	};

	//Remove the seam
	public void removeHorizontalSeam(int[] seam){

		int i, j, k;
		int[] tempPixelMap = new int[(importedImage.getWidth()*importedImage.getHeight()) - importedImage.getWidth()];
		BufferedImage reconstructedImage = new BufferedImage(importedImage.getWidth(), importedImage.getHeight()-1, BufferedImage.TYPE_INT_ARGB);

		//Creates new pixelMap without copying received seam
		j = 0;
		k = 0;
		for (i=0; i<pixelMap.length; i++) {
			if(i == j*importedImage.getWidth() + seam[j] ) {
				if( j < importedImage.getWidth() - 1){
					j++;
				}
			} else {
				tempPixelMap[k] = pixelMap[i];
				if( k < tempPixelMap.length - 1){
					k++;
				}
			}
		}

		pixelMap = tempPixelMap;

		//Reconstructs image based on the new pixelMap
		k = 0;
		for (i=0; i<reconstructedImage.getHeight(); i++) {
			for (j=0; j<reconstructedImage.getWidth(); j++) {

				reconstructedImage.setRGB(j, i, pixelMap[k]);
				k++;
			}
		}

		importedImage = reconstructedImage;


	};

	//Remove the seam
	public void removeVerticalSeam(int[] seam){

		int i, j, k;
		int[] tempPixelMap = new int[(importedImage.getWidth()*importedImage.getHeight()) - importedImage.getHeight()];
		BufferedImage reconstructedImage = new BufferedImage(importedImage.getWidth()-1, importedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

		//Creates new pixelMap without copying received seam
		j = 0;
		k = 0;
		for (i=0; i<pixelMap.length; i++) {
			if(i == j*importedImage.getWidth() + seam[j] ) {
				if( j < importedImage.getHeight() - 1){
					j++;
				}
			} else {
				tempPixelMap[k] = pixelMap[i];
				if( k < tempPixelMap.length - 1){
					k++;
				}
			}
		}

		pixelMap = tempPixelMap;

		//Reconstructs image based on the new pixelMap
		k = 0;
		for (i=0; i<reconstructedImage.getHeight(); i++) {
			for (j=0; j<reconstructedImage.getWidth(); j++) {

				reconstructedImage.setRGB(j, i, pixelMap[k]);
				k++;
			}
		}

		importedImage = reconstructedImage;
	};

	/* scale to the optimal
	dimensions before applying
	SeamCarve algorithm */
	private void scale(int width, int height){
		BufferedImage oldImage = importedImage;
		importedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D temp = importedImage.createGraphics();
		temp.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		temp.drawImage(oldImage, 0, 0, width, height,null);
		temp.dispose();

	};

	/* Apply scale first
	and seam carve afterwards */
	public void seamCarve(int width, int height){
		double ratio;
		int optimalWidth, optimalHeight;
		int cuttingDimension, currDimension;
		int[] seam;

		energyMap = new double[importedImage.getHeight()][importedImage.getWidth()];

		ratio = (double) importedImage.getWidth() / (double) importedImage.getHeight();

		//Decide if the image needs scaling and by how much and applies it
		if( ((int) ( width/ratio )) < height ) {
			optimalWidth = (int) (ratio * height);
			System.out.println("\nScaling down to " + optimalWidth + "x" + height + " for optimal resaults");
			this.scale(optimalWidth,height);
		} else {
			optimalHeight = (int) ( width/ratio );
			System.out.println("\nScaling down to " + width + "x" + optimalHeight + " for optimal resaults");
			this.scale(width,optimalHeight);
		}

		//Decide which dimension to reduce
		if (importedImage.getWidth() > width){
			cuttingDimension = width;
			currDimension = importedImage.getWidth();
		} else {
			cuttingDimension = height;
			currDimension = importedImage.getHeight();
		}

		//Removes seams until we reach desired resolution
		while ( currDimension > cuttingDimension ) {
			pixelMap = ((DataBufferInt)importedImage.getRaster().getDataBuffer()).getData();

			//Creating energyMap for the image
			for (int i=0; i < importedImage.getHeight(); i++){
				for (int j=0; j < importedImage.getWidth(); j++){
					energyMap[i][j] = this.energy(i,j);
				}
			}

			//Call corresponding seam retrieval and removal method
			if (importedImage.getWidth() > width){
				seam = this.findVerticalSeam();

				this.removeVerticalSeam(seam);
				currDimension = importedImage.getWidth();
			} else {
				seam = this.findHorizontalSeam();

				this.removeHorizontalSeam(seam);
				currDimension = importedImage.getHeight();
			}


		}//While end

	};

	public static void main(String[] args) {

		boolean flag = true;
		int newWidth = 0, newHeight = 0 ;
		SeamCarver userImage = null;
		File targetFile;

		Scanner userInput = new Scanner(System.in);
		String path = new String();

		System.out.print("Welcome to Image Resizer 3000 \nNow using the all-new Seam Carving formula!!!\n");

		//Parce user input and call the apropriate constructor
		try{
			path = args[0];
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.print("\nPlease enter a valid path or URL: ");
			path = userInput.next();
		}

		//Require new input from user if no valid input is given
		while(flag){
			try {

				//Atempt to create a java.net.URL object to check if input is a valid URL
				java.net.URL imageInput = new java.net.URL(path);
				userImage = new SeamCarver(imageInput);
				flag = false;
			} catch(IOException ex) {

				System.out.println("\nNo URL detected scanning for file (" + ex.getMessage() + ")" );
				try {

					//Atempt to create a java.io.File object to check if input is a valid local path
					java.io.File imageInput = new java.io.File(path);
					userImage = new SeamCarver(imageInput);
					flag = false;
				} catch(IOException ex2) {

					//At this point no valid input was given. Require user to insert new
					System.out.println("No file detected (" + ex2.getMessage() + ")" + "\n");
					System.out.print("Couln't retrieve an image from either source\nPlease provide new path to an image: ");
					path = userInput.next();
					//Loop for user input
				} catch(Exception ex3) {

					System.out.println("Something went horobly wrong " + ex3.getMessage());
				}//finally end
			}//1st catch end
		}//While flag end

		//Print imported image data
		System.out.println("The image you imported is: " + importedImage.getWidth() + "x" + importedImage.getHeight());

		//Require new image resolution form user. Ask for new ones if no valid is given
		System.out.print("\nEnter resizing dimensions\nEnter width: ");

		flag = true;
		do{
			do{
				try{
					newWidth = userInput.nextInt();
					if(newWidth <= 0){
						System.out.print("Please enter correct width (width > 0 && type integer): ");
					}
					flag = false;
				}catch(InputMismatchException ex3){
					System.out.print("Please enter correct width (width > 0 && type integer): ");
					userInput.next();
				}
			}while(newWidth <= 0);
		}while(flag);

		System.out.print("Enter height: ");
		flag = true;
		do{
			do{
				try{
					newHeight = userInput.nextInt();
					if(newHeight <= 0){
						System.out.print("Please enter correct height (height > 0 && type integer): ");
					}
					flag = false;
				}catch(InputMismatchException ex4){
					System.out.print("Please enter correct height (height > 0 && type integer): ");
					userInput.next();
				}
			}while(newHeight <= 0);
		}while(flag);

		System.out.print("\nEnter a file name for the resized image (ending in *.png): ");
		path = userInput.next();

		//Check for valid file extension
		while (!path.toLowerCase().endsWith(".png")) {
			System.out.print("\nFile name entered is not a *.png\nPlease enter new file name: ");
			path = userInput.next();
		}

		targetFile = new File(path);

		//Check if target file exists in output directory
		if ( targetFile.exists() ){
			System.out.println("\nFile name " + targetFile.getName() + " already exists");
		}else{
			userImage.seamCarve(newWidth, newHeight);

			//Outputs a buffered image to a file used for final output
			try{
				ImageIO.write(importedImage, "png", targetFile);
			} catch(IOException e) {

			}
		}

	}//Main end
}//Class end
