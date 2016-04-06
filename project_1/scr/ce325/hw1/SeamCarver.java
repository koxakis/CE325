public class SeamCarver{

	public SeamCarver(java.awt.image.BufferedImage image);
	public SeamCarver(java.io.File file);
	public SeamCarver(java.net.URL url);
	public double energy(int row, int col);  // energy of a pixel
	public int[] findHorizontalSeam();       // return horizontal seam
	public int[] findVerticalSeam();         // return vertical seam
	public void removeHorizontalSeam(int[] seam); // remove the seam
	public void removeVerticalSeam(int[] seam);   // remove the seam
	private void scale(int width, int height);    // scale to the optimal
	                                                // dimensions before applying
	                                                 // SeamCarve algorithm


}
