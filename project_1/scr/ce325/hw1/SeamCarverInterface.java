public interface SeamCarverInterface {

	//Deez iz da methods we haz to implement!!!

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
