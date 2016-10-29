/**
 * BitImage.java
 */

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.Point;
import java.awt.image.WritableRaster;
import java.awt.image.SampleModel;
import java.io.*;
import javax.imageio.*;

class BitImage{

	private static SampleModel sampleModel;

	public static void main(String [] args) throws IOException, RuntimeException{

		String filename = "im1-c.bmp";
		System.out.println("Reading an bitmap image" + filename);
		System.out.println("Printing the file in [0-255] range " + filename);
		printPixelArray(getPixelArray(filename));
		System.out.println("Modifying the file(for every value that is below 100 we add 15)");
		System.out.println("Printing the modified file in [0-255] range");
		printPixelArray(modifyPixelArray(getPixelArray(filename)));
		System.out.println("Writing to an file- output.bmp");
		writeToImage(modifyPixelArray(getPixelArray(filename)));

	}


  // Getting pixels from an array
	public static int [][] getPixelArray(String filename) throws IOException{

    	BufferedImage image = ImageIO.read(new File(filename));
    	Raster raster = image.getData();
    	sampleModel = raster.getSampleModel();

    	int [][] array2D =  new int[image.getWidth()][image.getHeight()];

		for (int j = 0; j < image.getWidth(); j++) {
    		for (int k = 0; k < image.getHeight(); k++) {
        		array2D[j][k] = raster.getSample(j, k, 0);
    		}
		}

		return array2D;
	}


	// Modify test
	public static int[][] modifyPixelArray(int [][] arr){
		for (int i = 0; i < arr.length; i++){
			for (int j = 0; j < arr[i].length; j++){
				if (arr[i][j] < 100){
					arr[i][j] += 15;
				}
			}
		}

		return arr;
	}


  // Printing pixels
	public static void printPixelArray(int [][] array2D){

    	for (int x = 0; x < array2D.length; x++)
    	{
        	for (int y = 0; y < array2D[x].length; y++)
        	{
            	System.out.print(array2D[x][y] + " ");
        	}
        	System.out.println();
    	}

	}


  // New Output Image
	public static void writeToImage(int [][] arr) throws IOException, RuntimeException{

		WritableRaster raster= Raster.createWritableRaster(sampleModel, new Point(0,0));
    	for(int i=0;i<arr.length;i++)
    	{
        	for(int j=0;j<arr[i].length;j++)
        	{
            	raster.setSample(i,j,0,arr[i][j]);
        	}
    	}

    	BufferedImage image=new BufferedImage(arr.length,arr[0].length,BufferedImage.TYPE_BYTE_GRAY);
    	image.setData(raster);

    	File file = new File("output.bmp");

    	if (file.createNewFile()){
    		System.out.println("File creation successful");
    	} else {
    		System.out.println("File already exists.");
    	}

		if (!ImageIO.write(image, "BMP", new File("output.bmp"))) {
  			throw new RuntimeException("Unexpected error writing image");
		}
	}
}
