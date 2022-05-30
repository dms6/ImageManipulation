import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Scanner;

/**
 * https://en.wikipedia.org/wiki/Canny_edge_detector 
 * 1. Apply Gaussian filter to smooth the image in order to remove the noise
 * 2. Find the intensity gradients of the image
 * 3. Apply lower bound cut-off suppression to get rid of spurious response to edge detection
 * 4. Apply double threshold to determine potential edges
 * 5. Finalize the detection of edges by suppressing all edges that are weak and not connected to strong edges.
 * 
 * if you get white results, disable alpha channel: https://stackoverflow.com/questions/26171739/remove-alpha-channel-in-an-image
 * if you get a runtime error, make sure input.png is inside the images folder
 * 
 * @author Dillon Shelton
 * @version 5/27/22
 */
public class EdgeDetect {
    
    BufferedImage image = null;
    BufferedImage copy = null; 
    private int width;
    private int height;
    //stores direction of edge 
    int[][] dir;
    //stores weak and strong pixels
    int[][] val;
    int[][] grad;
    
    // READ IMAGE
    public EdgeDetect(String str) throws IOException {
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

    public static void main(String[] args)  throws IOException{
        Scanner input = new Scanner(System.in);
        System.out.println("enter sigma, lower, upper. \n Try 1.2 0.1 0.2" );
        double sigma = input.nextDouble();
        double lower = input.nextDouble();
        double upper = input.nextDouble();
        EdgeDetect ed = new EdgeDetect("images/input.png");
        
        
        ed.grayscale();
        //the higher the number, the more noise is reduced. 
        ed.gaussian(sigma);
        //ed.write("images/1-gaussian.png");
        ed.intensityGradient();
        //ed.write("images/2-gradient.png");
        ed.suppression();
        //ed.write("images/3-suppression.png");
        //Any pixel under lower% is turned black, any number less than upper% is considered weak. 
        ed.doubleThreshold(lower, upper);
        //ed.write("images/4-threshold.png");
        ed.hysteresis();
        ed.write("images/final.png");
        
    }
    
    public void updateCopy(){
      copy = new BufferedImage(image.getColorModel(), image.copyData(null), false, null);
    }
    
    //turns image to grayscale
    public void grayscale(){
        for(int i = 0;i<height;i++){
            for(int j = 0;j<width; j++){
                int pixel = image.getRGB(j,i);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                
                int average = (red+green+blue)/3; 

                // set the pixel value
                pixel = (average << 16) | (average << 8) | average;
                image.setRGB(j, i, pixel);
            }
        }
    }
    
    //5x5 blue, but the further away a pixel is the less weight it has on the original pixel
    public void gaussian(double sigma){
        if(sigma==0)return;
        updateCopy();
        double weight[][] = new double[5][5];
        double sum=0;
        //create gaussian matrix
        for(int i = 0;i < 5;i++){
            for(int j = 0;j<5;j++){
                weight[i][j]=Math.exp(-(Math.pow((2-i),2)+Math.pow((2-j),2))/(2*Math.pow(sigma,2)))/(2*Math.PI*sigma*sigma);
                sum+=weight[i][j];
            }
        }
        //Print matrix
        for(int i = 0;i< 5;i++){
            for(int j = 0;j<5;j++){
                weight[i][j]/=sum;
                System.out.print((Math.round(weight[i][j]*100))/100.0 +" ");
            }
            System.out.println();
        }

        for(int i = 0;i<width;i++){
            for(int j = 0;j<height;j++){
                int pixel = copy.getRGB(i,j);
                int intensity = (pixel >> 16) & 0xff;
                double nIntensity = 0;

                for(int k = i-2;k<=i+2;k++){
                    if(k<0||k>width-1)continue;
                    for(int l = j-2;l<=j+2;l++){
                        if(l<0||l>height-1)continue;
                        pixel = copy.getRGB(k,l);
                        int cIntensity = (pixel >> 16) & 0xff;

                        nIntensity+=weight[k-i+2][l-j+2]*cIntensity;
                    }
                }
                
                pixel = ((int)nIntensity << 16) | ((int)nIntensity << 8) | (int)nIntensity;
                image.setRGB(i, j, pixel);
            }
        }
    }
    
    //https://en.wikipedia.org/wiki/Sobel_operator
    public void intensityGradient(){
        updateCopy();
        int[][] x = {{1, 0, -1},{2, 0, -2},{1, 0, -1}};        
        int[][] y = {{1, 2, 1},{0, 0, 0},{-1,-2,-1}};   
        dir = new int[height][width];
        grad = new int[height][width];
        int maxGradient = -1;
        for(int i = 1;i<width-1;i++){
            for(int j = 1;j<height-1;j++){
                int pixel = copy.getRGB(i,j);
                double Gx = 0;
                double Gy = 0;
                for(int k = 0;k<3;k++){
                    for(int l = 0;l< 3;l++){
                        pixel = copy.getRGB(i-1+k,j-1+l);
                        int cIntensity = (pixel >> 16) & 0xff;
                        Gx+=x[l][k]*cIntensity;
                        Gy+=y[l][k]*cIntensity;
                    }
                }
                int G = (int)Math.sqrt(Gx*Gx+Gy*Gy); 
                grad[j][i] = G;
                 if(G>maxGradient) {
                    maxGradient = G;
                }

                double theta = Math.atan2(Gy,Gx);
                if(theta<=22.5 ||theta>=157.5) dir[j][i] = 0;
                else if(theta<=45||theta>=135) dir[j][i] = 45;
                else if(theta<=67.5||theta>=112.5) dir[j][i] = 90;
                else dir[j][i] = 135;
            }
        }
        
        double scale = 255.0/maxGradient;
        for(int i = 1;i<width-1;i++){
            for(int j = 1;j<height-1;j++){
                grad[j][i]*=scale;
                
                int pixel = (grad[j][i] << 16) | (grad[j][i] << 8) | grad[j][i];
                image.setRGB(i, j, pixel);
            }
        }
    }
    
    //If the edge strength of the current pixel is the largest compared to the other pixels in the mask with the same direction 
    //(e.g., a pixel that is pointing in the y-direction will be compared to the pixel above and below it in the vertical axis), 
    //the value will be preserved. Otherwise, the value will be suppressed.
    public void suppression(){
        updateCopy();
        //screw borders, just ignore them
        for(int i = 0;i<width;i++){
            for(int j = 1;j<height;j++){
                
                int pixel = copy.getRGB(i,j);
                int C = (pixel >> 16) & 0xff; //current pixel

                //E and W
                if(dir[j][i]==0){
                    int E =0;
                    int W =0;
                    if(i+1<width)E = (copy.getRGB(i+1,j) >> 16) & 0xff;
                    if(i-1>=0) W = (copy.getRGB(i-1,j) >> 16) & 0xff;
                    if(C<=E||C<=W) C=0;
                }
                //NE and SW
                else if(dir[j][i]==45){
                    int NE = 0,SW=0;
                    if(i+1<width&&j+1<height)NE = (copy.getRGB(i+1,j+1) >> 16) & 0xff;
                    if(i-1>=0&&j-1>=0)SW = (copy.getRGB(i-1,j-1) >> 16) & 0xff;
                    if(C<=NE||C<=SW) C=0;
                }
                //N and S
                else if(dir[j][i]==90){
                    int N=0,S=0;
                    if(j-1>=0) N = (copy.getRGB(i,j-1) >> 16) & 0xff;
                    if(j+1<height) S = (copy.getRGB(i,j+1) >> 16) & 0xff;
                    if(C<=N||C<=S) C=0;
                }
                //NW and SE
                else if(dir[j][i]==135){
                    int NW=0,SE=0;
                    if(i-1>=0&&j-1>=0)NW = (copy.getRGB(i-1,j-1) >> 16) & 0xff;
                    if(i+1<width&&j+1<height)SE = (copy.getRGB(i+1,j+1) >> 16) & 0xff;
                    if(C<=NW||C<=SE) C=0;
                }
                else System.out.print("Something went wrong");
                
                pixel = (C<< 16) | (C << 8) | C;
                image.setRGB(i, j, pixel);
            }
        }
    }
    
    //If an edge pixel’s gradient value is higher than the high threshold value, it is marked as a strong edge pixel. 
    //If an edge pixel’s gradient value is smaller than the high threshold value and larger than the low threshold value, it is marked as a weak edge pixel.
    //If an edge pixel's gradient value is smaller than the low threshold value, it will be suppressed. 
    public void doubleThreshold(double lower, double upper){
        updateCopy();
        val = new int[height][width];
        for(int i = 0;i<width;i++){
            for(int j = 0;j<height;j++){
                int pixel = copy.getRGB(i,j);
                int C = (pixel >> 16) & 0xff;
                if(C<lower*255) C=0;
                //1 is weak, 2 is strong
                else if(C<upper*255) val[j][i] = 1;
                else val[j][i] = 2;
                
                pixel = (C << 16) | (C << 8) | C;
                image.setRGB(i, j, pixel);
            }
        }
    }
    
    /*Usually a weak edge pixel caused from true edges will be connected to a strong edge pixel while noise responses 
     *are unconnected. To track the edge connection, blob analysis is applied by looking at a weak edge pixel 
     *and its 8-connected neighborhood pixels. As long as there is one strong edge pixel that is involved in the blob,
     *that weak edge point can be identified as one that should be preserved.
     */
    public void hysteresis(){
        //updateCopy();
        for(int i = 0;i<width;i++){
            for(int j = 0;j<height;j++){
                
                
                boolean strongPresent = false;
                SCAN_WINDOW: for(int k = i-1;k<=i+1;k++){
                    if(k<0||k>width-1)continue;
                    for(int l = j-1;l<=j+1;l++){
                        if(l<0||l>height-1)continue;
                        if(val[l][k]==2) strongPresent = true;
                        break SCAN_WINDOW;
                    }
                }
                
                if(strongPresent) {
                    int pixel = (255 << 16) | (255 << 8) | 255;
                    image.setRGB(i, j, pixel);
                    continue;
                }
                
                int pixel = (0 << 16) | (0 << 8) | 0;
                image.setRGB(i, j, pixel);
                
            }
        }
    }
    
    //WRITE IMAGE
    public void write(String fileName) throws IOException{
        try {
            // Output file path
            File output_file = new File(fileName);
            // Writing to file taking type and path as
            ImageIO.write(image, "png", output_file);
            System.out.println(fileName+" complete.");
        }
        catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
}
