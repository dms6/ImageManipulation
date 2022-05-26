package Projects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
  
/**
 * Blurs an image, takes a blur parameter (manually change it)
 * @Dillon Shelton
 * @5/26/22
 */

public class BlurImage {
    public static void main(String args[]) throws IOException
    {
        // For storing image in RAM
        BufferedImage image = null;
        BufferedImage copy = null;
  
        // READ IMAGE
        try {
            File input_file = new File("Projects/images/input.png");
            // Reading input file
            image = ImageIO.read(input_file);
            copy = ImageIO.read(input_file);
            System.out.println("Reading complete.");
           
        }
        catch (IOException e) {
            System.out.println("Error: " + e);
        }
        
        //ALTER IMAGE
        int width = copy.getWidth();
        int height = copy.getHeight();
        
        int blur = 9; //must be odd
        //iterate through each pixel in image
        for(int i = 0;i<height;i++){
            for(int j = 0;j<width; j++){
                //iterate through each 'window' surrounding the pixel, adding up its values
                int count = 0;
                int totalRed = 0;
                int totalGreen = 0;
                int totalBlue = 0;
                int totalAlpha = 0;
                for(int k = i-blur/2;k<=i+blur/2;k++){
                    if(k<0||k>height-1)continue;
                    for(int l = j-blur/2;l<=j+blur;l++){
                        if(l<0||l>width-1)continue;
                        count++;
                        int pixel = copy.getRGB(l,k);
                        totalAlpha += (pixel >> 24) & 0xff;
                        totalRed += (pixel >> 16) & 0xff;
                        totalGreen += (pixel >> 8) & 0xff;
                        totalBlue += pixel & 0xff;
                    }
                }
                int alpha = totalAlpha/count;
                int red = totalRed/count;
                int green = totalGreen/count;
                int blue = totalBlue/count;

                // set the pixel value
                int pixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(j, i, pixel);
                
            }
        }

        //WRITE IMAGE
        try {
            // Output file path
            File output_file = new File("Projects/images/output.png");
            // Writing to file taking type and path as
            ImageIO.write(image, "png", output_file);
            System.out.println("Writing complete.");
        }
        catch (IOException e) {
            System.out.println("Error: " + e);
        }
    } 
} 