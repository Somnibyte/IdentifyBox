/**
 * BitImage.java
 */

/*
Some Notes:

BufferedImage class - Allows us to operate directly with image data (retrieving/setting pixel values)
Raster - data structure that holds image data (allows us to scan image data)
SampleModel - Interface that allows for pixel extraction.
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
		//printPixelArray(getPixelArray(filename));

		//***Histogram process***
		System.out.println("Modifying the file(for every value that is below 100 we add 15)");
		System.out.println("Printing the modified file in [0-255] range");
		//printPixelArray(applyHistogramEqualization(getPixelArray(filename)));
		System.out.println("Writing to an file- output.bmp");
		writeToImage(applyContrast(getPixelArray(filename), 3, 127), "contrast.bmp");

		//***Smoothing process***
		System.out.println("Now applying smoothing filter...");
		writeToImage(applySmoothing(getPixelArray(filename)), "smoothing_output_new.bmp");

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




	// Contrast Adjustment
	public static int[][] applyContrast(int[][] arr, int contrast, int brightness){
		for (int i = 0; i < arr.length; i++){
			for (int j = 0; j < arr[i].length; j++){
				arr[i][j] = contrast*(arr[i][j] - brightness) + brightness;
			}
		}

		return arr;
	}


	// Histogram Equalization
	public static int[][] applyHistogramEqualization(int [][] arr){

		int[] H = new int[256];

		// Initialize the Histogram
		for(int i = 0; i < H.length; i++){
			H[i] = 0;
		}

		// Compute the Histogram
		for (int i = 0; i < arr.length; i++){
			for (int j = 0; j < arr[i].length; j++){
					H[arr[i][j]] += 1;
			}
		}

		// Compute the cumulative Histogram
		for(int i = 1; i < 256; i++){
			H[i] = H[i-1] + H[i];
		}

		// Normalize the cumulative Histogram
		for(int i = 0; i < 256; i++){
			H[i] = H[i] * 255 / (arr.length * arr[0].length);
		}

		//Modify Image
		for (int i = 0; i < arr.length; i++){
			for (int j = 0; j < arr[i].length; j++){
				arr[i][j] = H[arr[i][j]];
			}
		}

		return arr;
	}

	
	/*
	int [][] gaussianKernel = new int[][] {
			{1, 4, 6, 4, 1},
			{4, 16, 25, 16, 4},
			{6, 28, 49, 28, 6},
			{4, 16, 25, 16, 4},
			{1, 4, 6, 4, 1}
		};
	*/
	//Smoothing
	//Smoothing
	public static int[][] applySmoothing(int[][] arr)
	{
		int sum = 0;
		int avg;
		//"frame" refers to small segment being modified at a time
		int frameX = 3;
		int frameY = 3;
		int currentStartRow = 0;
		int currentMaxRow = 3;
		int currentStartColumn = 0;
		int currentMaxColumn = 3;
		int kernelColumn = 0;
		int kernelRow = 0;
		int [][] outputArray = new int[arr.length][arr[0].length];
		/*int[][] weightedKernel = new int[][] {
			{1, 4, 7 , 4, 1},
			{4, 16, 28, 16, 4},
			{7, 28, 49, 28, 7},
			{4, 16, 28, 16, 4},
			{1, 4, 7, 4, 1}
		};*/
		int [][] weightedKernel = new int[][] {
			{1, 2, 1},
			{2, 4, 2},
			{1, 2, 1}
		};

		//operate on rows
		while(currentMaxRow < arr.length)
		{
			//System.out.println("currentStartRow = " + currentStartRow);
			//operate on columns
			while(currentMaxColumn < arr[0].length)
			{
				//System.out.println("currentStartColumn = " + currentStartColumn);
				//gather average of frame
				for(int i=currentStartRow; i<currentMaxRow; i++)
				{
					for(int j=currentStartColumn; j<currentMaxColumn; j++)
					{
						kernelColumn = j%3;
						kernelRow = i%3;
						sum += weightedKernel[kernelRow][kernelColumn]*arr[i][j];
					}
				}

				avg = sum/(16);
				sum = 0;

				if(avg > 255)
					avg = 255;
				else if(avg < 0)
					avg = 0;

				outputArray[currentStartRow+1][currentStartColumn+1] = avg;
				
				currentStartColumn += 1;//frameX;
				currentMaxColumn += 1; //frameX;
			}
			currentStartRow += 1;//frameY;
			currentMaxRow += 1;//frameY;
			currentMaxColumn = 3;//frameX;
			currentStartColumn = 0;
			//System.out.println("adding to row");
		}
		return outputArray;
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
	public static void writeToImage(int [][] arr, String filename) throws IOException, RuntimeException{

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

    	File file = new File(filename);

    	if (file.createNewFile()){
    		System.out.println("File creation successful");
    	} else {
    		System.out.println("File already exists.");
    	}

		if (!ImageIO.write(image, "BMP", new File(filename))) {
  			throw new RuntimeException("Unexpected error writing image");
		}
	}
}
