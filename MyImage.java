package Projects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
  
public class MyImage {
    public static void main(String args[])
        throws IOException
    {
        // For storing image in RAM
        BufferedImage image = null;
  
        // READ IMAGE
        try {
            File input_file = new File("Projects/images/input.png");
            // Reading input file
            image = ImageIO.read(input_file);
            
            System.out.println("Reading complete.");
           
        }
        catch (IOException e) {
            System.out.println("Error: " + e);
        }
        int width = image.getWidth();
        int height = image.getHeight();
        int greater = 0, less = 0;;
        
        for(int i = 0;i<height;i++){
            for(int j = 0;j<width; j++){
                int pixel = image.getRGB(j,i);
                // get alpha
                int alpha = (pixel >> 24) & 0xff;
                // get red
                int red = (pixel >> 16) & 0xff;
                // get green
                int green = (pixel >> 8) & 0xff;
                // get blue
                int blue = pixel & 0xff;
                //alter stuff
                int total = (red+green+blue); 
                int choice = (int)(Math.random()*6);
                switch(choice){
                    case 0: 
                        red=Math.min((int)(Math.random()*total),255);
                        blue = Math.min((int)(Math.random()*(total-red)),255);
                        green = total-red-blue;
                        break;
                    case 1: 
                        red=Math.min((int)(Math.random()*total),255);
                        green = Math.min((int)(Math.random()*(total-red)),255);
                        blue = total-green-red;
                        break;
                    case 2: 
                        blue=Math.min((int)(Math.random()*total),255);
                        red = Math.min((int)(Math.random()*(total-blue)),255);
                        green = total-red-blue;
                        break;
                    case 3: 
                        blue=Math.min((int)(Math.random()*total),255);
                        green = Math.min((int)(Math.random()*(total-blue)),255);
                        red = total-green-blue;
                        break;
                    case 4: 
                        green=Math.min((int)(Math.random()*total),255);
                        blue = Math.min((int)(Math.random()*(total-green)),255);
                        red = total-blue-green;
                        break;
                    case 5: 
                        green=Math.min((int)(Math.random()*total),255);
                        red = Math.min((int)(Math.random()*(total-green)),255);
                        blue = total-green-red;
                        break;
                }
                
                
                if(red>256||blue>256||green>256)greater++;
                if(red<0||blue<0||green<0)less++;
                
                
                
                // set the pixel value
                pixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(j, i, pixel);
            }
        }
        //System.out.println(greater);
        //System.out.println(less);
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