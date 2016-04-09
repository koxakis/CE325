public interface SeamCarverInterface {

	//Deez iz da methods we haz to implement!!!

	//Constractors for different input methods
	public SeamCarver(java.awt.image.BufferedImage image);
	public SeamCarver(java.io.File file);
	public SeamCarver(java.net.URL url);

	 // energy of a pixel
	public double energy(int row, int col);

	 // return horizontal seam
	public int[] findHorizontalSeam();

	// return vertical seam
	public int[] findVerticalSeam();

	// remove the seam
	public void removeHorizontalSeam(int[] seam);

	// remove the seam
	public void removeVerticalSeam(int[] seam);

	/* scale to the optimal
	dimensions before applying
	SeamCarve algorithm */
	private void scale(int width, int height);
}
