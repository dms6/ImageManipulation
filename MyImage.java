
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
  
/**
 * Bilateral can take up to a minute, beware
 * You can stack effects if you want
 * edge detection coming soon?
 * @Dillon Shelton
 * @5/26/22
 */

public class MyImage {
    //easy access to main
    public static void main(String args[]) throws IOException
    {
        MyImage img = new MyImage("images/input.png");
        //img.grayscaleBilateral(7,5,10);
        //img.toAscii();
        img.rainbow();
        //img.grayscale();
        //img.bilateral(7,5,10);
        //img.blur(7);
        img.write();
    } 
    
    BufferedImage image = null;
    BufferedImage copy = null; 
    private int width;
    private int height;
    
    public MyImage(String str) throws IOException {
        // READ IMAGE
        try {
            File input_file = new File(str);
            // Reading input file
            image = ImageIO.read(input_file);
            copy = ImageIO.read(input_file);
            System.out.println("Reading complete.");
            
            width = image.getWidth();
            height = image.getHeight();
        }
        catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
    public MyImage() throws IOException {
        this("images/input.png");
    }

    //WRITE IMAGE
    public void write() throws IOException{
        try {
            // Output file path
            File output_file = new File("images/output.png");
            // Writing to file taking type and path as
            ImageIO.write(image, "png", output_file);
            System.out.println("Writing complete.");
        }
        catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
    
    //UPDATE IMAGE COPY
    public void updateCopy(){
      copy = new BufferedImage(image.getColorModel(), image.copyData(null), false, null);
    }

    //ALTER IMAGE
    public void blur(int blur){
        //This method is based off of surrounding pixels, so it needs information of them before they were altered
        updateCopy(); 
        
        //iterate through each pixel in image
        for(int i = 0;i<height;i++){
            for(int j = 0;j<width; j++){
                //iterate through each 'window' surrounding the pixel, adding up its values
                int count = 0;
                int totalRed = 0;
                int totalGreen = 0;
                int totalBlue = 0;
                
                for(int k = i-blur/2;k<=i+blur/2;k++){
                    if(k<0||k>height-1)continue;
                    for(int l = j-blur/2;l<=j+blur;l++){
                        if(l<0||l>width-1)continue;
                        count++;
                        int pixel = copy.getRGB(l,k);
                        totalRed += (pixel >> 16) & 0xff;
                        totalGreen += (pixel >> 8) & 0xff;
                        totalBlue += pixel & 0xff;
                    }
                }
 
                int red = totalRed/count;
                int green = totalGreen/count;
                int blue = totalBlue/count;

                // set the pixel value
                int pixel = (red << 16) | (green << 8) | blue;
                image.setRGB(j, i, pixel);
            }
        }
    }
    
    //https://people.csail.mit.edu/sparis/bf_course/slides/03_definition_bf.pdf Check this out!!
    public void bilateral(int blur, double spatial, double range){
        updateCopy();
        
        for(int i = 0;i<width;i++){
            for(int j = 0;j<height;j++){
                //each pixel now
                int pixel = copy.getRGB(i,j);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                double dRed = 0;
                double dGreen =0;
                double dBlue =0;
                double nRed =0;
                double nGreen=0;
                double nBlue = 0;
                for(int k = i-blur/2;k<=i+blur/2;k++){
                    if(k<0||k>width-1)continue;
                    for(int l = j-blur/2;l<=j+blur;l++){
                        if(l<0||l>height-1)continue;
                        pixel = copy.getRGB(k,l);
                        int cRed = (pixel >> 16) & 0xff;
                        int cGreen = (pixel >> 8) & 0xff;
                        int cBlue = pixel & 0xff;
                        //original * space kernel * color kernal
                        
                        double space = (Math.pow((i-k),2)+Math.pow((j-l),2))/(2*Math.pow(spatial,2));
                        
                        dRed+=Math.exp(-space-(Math.pow((red-cRed),2)/(2*Math.pow(range,2))));
                        dGreen+=Math.exp(-space-(Math.pow((green-cGreen),2)/(2*Math.pow(range,2))));
                        dBlue+=Math.exp(-space-(Math.pow((blue-cBlue),2)/(2*Math.pow(range,2))));
                       
                        nRed+=cRed*Math.exp(-space-(Math.pow((red-cRed),2)/(2*Math.pow(range,2))));
                        nGreen+=cGreen*Math.exp(-space-(Math.pow((green-cGreen),2)/(2*Math.pow(range,2))));
                        nBlue+=cBlue*Math.exp(-space-(Math.pow((blue-cBlue),2)/(2*Math.pow(range,2))));
                        
                    }
                }
                //normalize
                red = (int)(nRed/dRed);
                blue = (int)(nBlue/dBlue);
                green = (int)(nGreen/dGreen);
     
                pixel = (red << 16) | (green << 8) | blue;
                image.setRGB(i, j, pixel);
                
            }
            
        }
    }
    
    //faster
    public void grayscaleBilateral(int blur, double spatial, double range){
        grayscale();
        updateCopy();
        for(int i = 0;i<width;i++){
            for(int j = 0;j<height;j++){
                //each pixel now
                int pixel = copy.getRGB(i,j);
                int intensity = (pixel >> 16) & 0xff;
                double dIntensity =0;
                double nIntensity = 0;

                for(int k = i-blur/2;k<=i+blur/2;k++){
                    if(k<0||k>width-1)continue;
                    for(int l = j-blur/2;l<=j+blur;l++){
                        if(l<0||l>height-1)continue;
                        pixel = copy.getRGB(k,l);
                        int cIntensity = (pixel >> 16) & 0xff;
                        //original * space kernel * color kernal
                        double weight = Math.exp(-(Math.pow((i-k),2)+Math.pow((j-l),2))/(2*Math.pow(spatial,2))-(Math.pow((intensity-cIntensity),2)/(2*Math.pow(range,2))));
                        dIntensity+=weight;
                        nIntensity+=cIntensity*weight;
                    }
                }
                //normalize
                intensity = (int)(nIntensity/dIntensity);
                pixel = (intensity << 16) | (intensity << 8) | intensity;
                image.setRGB(i, j, pixel);
            }
            
        }
    }
    
    public void grayscale(){
        for(int i = 0;i<height;i++){
            for(int j = 0;j<width; j++){
                int pixel = image.getRGB(j,i);
                int alpha = (pixel >> 24) & 0xff;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                
                int average = (red+green+blue)/3; 
                red = average;
                blue = average;
                green = average;
                // set the pixel value
                pixel = (red << 16) | (green << 8) | blue;
                image.setRGB(j, i, pixel);
            }
        }
    }
    
    public void rainbow(){
        for(int i = 0;i<height;i++){
            for(int j = 0;j<width; j++){
                int pixel = image.getRGB(j,i);
                int alpha = (pixel >> 24) & 0xff;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                //definitely a better way of doing this
                int total = red+green+blue; 
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
                
                // set the pixel value
                pixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(j, i, pixel);
            }
        }
    }
    
    public void toAscii(){
        int newHeight = 47;
        int newWidth = (int)(width*((double)newHeight/height));
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g = resized.createGraphics();
        g.drawImage(image, 0, 0, newWidth, newHeight, 0, 0, width, height, null);
        g.dispose();
        width = resized.getWidth();
        height = resized.getHeight();
        
        //PRINT IMAGE
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i<height;i++){
            for(int j = 0;j<width; j++){
                int pixel = resized.getRGB(j,i);
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
                //sb.append("");
            }
            sb.append("\n");
        }
        System.out.println(sb);
        
    }
} 