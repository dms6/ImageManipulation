package Projects;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
//Results vary based on size of image and size of terminal font. Font can be rescaled by pressing Cmd- on code then restarting bluej.
//Do not use greater than 100x100
public class ImageToAscii {
    public static void main(String args[]) throws IOException
    {
        BufferedImage image = null;
        // READ IMAGE
        try {
            File input_file = new File("Projects/images/input.jpeg");
            // Reading input file
            image = ImageIO.read(input_file);
            
            System.out.println("Reading complete.");
        }
        catch (IOException e) {
            System.out.println("Error: " + e);
        }
        
        int width = image.getWidth();
        int height = image.getHeight();
        StringBuilder sb = new StringBuilder();
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
                int average = (red+green+blue)/3; 
                if(average<25.5)sb.append("@");
                else if(average<25.5*2)sb.append("%");
                else if(average<25.5*3)sb.append("#");
                else if(average<25.5*4)sb.append("*");
                else if(average<25.5*5)sb.append("+");
                else if(average<25.5*6)sb.append("=");
                else if(average<25.5*7)sb.append("-");
                else if(average<25.5*8)sb.append(":");
                else if(average<25.5*9)sb.append(".");
                else if(average<=25.5*10)sb.append(" ");
                sb.append(" ");
            }
            sb.append("\n");
        }
        System.out.println(sb);
    } 
} 
