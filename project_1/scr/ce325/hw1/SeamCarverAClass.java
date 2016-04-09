package ce325.hw1;

public abstract class SeamCarverAClass {

	//Deez iz da methods we haz to implement!!!

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
	private void scale(int width, int height){};
}
