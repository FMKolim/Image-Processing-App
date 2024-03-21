import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;


public class Demo extends Component implements ActionListener {
    //************************************
    // List of the options(Original, Negative); correspond to the cases:
    //************************************
    String descs[] = {
        "Original", 
        "Negative",
        "Undo",
        "Resize",
        "Value Shift",
        "Resize + Shift",
        "2 Pictures Arithmetic",
        "NOT operator",
        "Lab 3 AND",
        "Lab 3 OR",
        "Lab 3 XOR",
        "ROI",
        "Log Function",
        "Power Function",
        "LUT",
        "Bit Plane",
        "Convolution",
        "Salt and Pepper",
        "Min Filter",
        "Max Filter",
        "Mid Filter",
        "Average Filter",
        "Histogram",
        "Normalised Histogram",
        "Equalised Histogram Picture",
        "Mean And Standard Deviation",
        "Thresholding",
        "Automatic Thresholding",
    };

    int opIndex;  // option index for selected image processing operation
    int lastOp;

    private BufferedImage bi, bitwo, biFiltered;
    int w, h, w2, h2;

    private Stack<BufferedImage> historyStack;

    public Demo() {

        historyStack = new Stack<>();

        System.out.println("Stack Created");

        // Constructor: Initialize the component and load default image
        try {
            bi = ImageIO.read(new File("default.jpg")); // Load default image
            w = bi.getWidth(null); // Get image width
            h = bi.getHeight(null); // Get image height

            bitwo = ImageIO.read(new File("default2.jpg")); 
            w2 = bitwo.getWidth(null); 
            h2 = bitwo.getHeight(null); 

            System.out.println(bi.getType());

            if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics big = bi2.getGraphics();
                big.drawImage(bi, 0, 0, null);
                biFiltered = bi = bi2;
            }

            if (bitwo.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage bitwo2 = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_RGB);
                Graphics big = bitwo2.getGraphics();
                big.drawImage(bitwo, 0, 0, null);
                bitwo = bitwo2;
            }


        } catch (IOException e) {
            // Handle image loading errors
            System.out.println("Image could not be read");
            System.exit(1); // Exit program
        }
    }

    public Dimension getPreferredSize() {
        int preferredWidth;
        if (bi.getWidth() > bitwo.getWidth()) {
            preferredWidth = bi.getWidth() + bitwo.getWidth();
        } else {
            preferredWidth = bi.getWidth() + bitwo.getWidth() + (bitwo.getWidth() - bi.getWidth());
        }
        int preferredHeight = Math.max(bi.getHeight(), bitwo.getHeight());
        return new Dimension(preferredWidth, preferredHeight);
    }
    

    String[] getDescriptions() {
        // Get descriptions of available image processing options
        return descs;
    }

    // Return the formats sorted alphabetically and in lower case
    public String[] getFormats() {
        String[] formats = {"bmp","gif","jpeg","jpg","png"};
        TreeSet<String> formatSet = new TreeSet<String>();
        for (String s : formats) {
            formatSet.add(s.toLowerCase());
        }
        return formatSet.toArray(new String[0]);
    }

    void setOpIndex(int i) {
        // Set the index of the selected image processing option
        opIndex = i;
    }

    public void paint(Graphics g) {
        // Override paint() to repaint the component
        filterImage(); // Apply image processing
        
        // Draw the filtered image
        g.drawImage(biFiltered, 0, 0, null); 
    
        // Determine the x-coordinate for drawing bitwo based on image sizes
        int xCoordinate;
        if (biFiltered.getWidth() > bitwo.getWidth()) {
            xCoordinate = bi.getWidth(); // Draw bitwo to the right of biFiltered
        } else {
            xCoordinate = bitwo.getWidth(); // Draw bitwo to the right of biFiltered
        }
    
        // Draw bitwo at the determined x-coordinate
        g.drawImage(bitwo, xCoordinate, 0, null);
    }

    //************************************
    //  Convert the Buffered Image to Array
    //************************************
    private static int[][][] convertToArray(BufferedImage image){
        // Convert BufferedImage to a 3D integer array representing pixel values
        int width = image.getWidth();
        int height = image.getHeight();
        int[][][] result = new int[width][height][4];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = image.getRGB(x,y);
                int a = (p>>24)&0xff;
                int r = (p>>16)&0xff;
                int g = (p>>8)&0xff;
                int b = p&0xff;

                result[x][y][0]=a;
                result[x][y][1]=r;
                result[x][y][2]=g;
                result[x][y][3]=b;
            }
        }
        return result;
    }

    //************************************
    //  Convert the  Array to BufferedImage
    //************************************
    public BufferedImage convertToBimage(int[][][] TmpArray){
        // Convert a 3D integer array representing pixel values to a BufferedImage
        int width = TmpArray.length;
        int height = TmpArray[0].length;
        BufferedImage tmpimg=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                int a = TmpArray[x][y][0];
                int r = TmpArray[x][y][1];
                int g = TmpArray[x][y][2];
                int b = TmpArray[x][y][3];
                
                // Set RGB value
                int p = (a<<24) | (r<<16) | (g<<8) | b;
                tmpimg.setRGB(x, y, p);
            }
        }
        return tmpimg;
    }

    //************************************
    //  Example:  Image Negative
    //************************************
    public BufferedImage ImageNegative(BufferedImage timg){
        // Perform image negative operation
        int width = timg.getWidth();
        int height = timg.getHeight();
        int[][][] ImageArray = convertToArray(timg); // Convert image to array

        // Image Negative Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = 255-ImageArray[x][y][1];  //r
                ImageArray[x][y][2] = 255-ImageArray[x][y][2];  //g
                ImageArray[x][y][3] = 255-ImageArray[x][y][3];  //b
            }
        }
        
        return convertToBimage(ImageArray); // Convert the array to BufferedImage
    }

    public BufferedImage ImageUndo() {

        if (!historyStack.isEmpty()) {

            return historyStack.pop();

        }

        return biFiltered; 
    }

    public BufferedImage rescale(BufferedImage timg, double factor) {

        int width = timg.getWidth();
        int height = timg.getHeight();
        int[][][] ImageArray = convertToArray(timg);

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                ImageArray[x][y][1] = (int) Math.round(ImageArray[x][y][1] * factor);  //r
                ImageArray[x][y][2] = (int) Math.round(ImageArray[x][y][2] * factor);  //g
                ImageArray[x][y][3] = (int) Math.round(ImageArray[x][y][3] * factor);  //b
                
                ImageArray[x][y][1] = Math.max(0, Math.min(ImageArray[x][y][1], 255));
                ImageArray[x][y][2] = Math.max(0, Math.min(ImageArray[x][y][2], 255));
                ImageArray[x][y][3] = Math.max(0, Math.min(ImageArray[x][y][3], 255));
            }
        }

        return convertToBimage(ImageArray);

    }

    
    public BufferedImage ValueShifting(BufferedImage timg, int value) {

        int width = timg.getWidth();
        int height = timg.getHeight();
        int[][][] ImageArray = convertToArray(timg);

        for(int y = 0; y < height; y++) {

            for(int x = 0; x < width; x++) {

                ImageArray[x][y][1] = (int) Math.round(ImageArray[x][y][1] + value);  //r
                ImageArray[x][y][2] = (int) Math.round(ImageArray[x][y][2] + value);  //g
                ImageArray[x][y][3] = (int) Math.round(ImageArray[x][y][3] + value);  //b
                
                ImageArray[x][y][1] = Math.max(0, Math.min(ImageArray[x][y][1], 255));
                ImageArray[x][y][2] = Math.max(0, Math.min(ImageArray[x][y][2], 255));
                ImageArray[x][y][3] = Math.max(0, Math.min(ImageArray[x][y][3], 255));
            }
        }

        return convertToBimage(ImageArray);

    }

    public BufferedImage RescaleShifting(BufferedImage timg) {

        int width = timg.getWidth();
        int height = timg.getHeight();
        int[][][] ImageArray = convertToArray(timg);
    
        int[][] addingmatrix = new int[width][height];
        Random rand = new Random();
    
        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;
    
        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                addingmatrix[x][y] = rand.nextInt(256);
                
            }
        }
    
        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                for (int i = 0; i < 3; i++) {

                    ImageArray[x][y][i] = ImageArray[x][y][i] + addingmatrix[x][y];
        
                    minimum = Math.min(minimum, ImageArray[x][y][i]);
                    maximum = Math.max(maximum, ImageArray[x][y][i]);

                }
            }
        }
    
        if (minimum != maximum) {

            for (int y = 0; y < height; y++) {

                for (int x = 0; x < width; x++) {

                    for (int i = 0; i < 3; i++) {

                        ImageArray[x][y][i] = (int) Math.round((ImageArray[x][y][i] - minimum) * (255.0 / (maximum - minimum)));

                    }
                }
            }
        }
    
        return convertToBimage(ImageArray);
    }

    public BufferedImage twoPicsArithmetic(BufferedImage timg, BufferedImage timg2) {

        int width1 = timg.getWidth();
        int height1 = timg.getHeight();
        int width2 = timg2.getWidth();
        int height2 = timg2.getHeight();
    
        int maxWidth = Math.max(width1, width2);
        int maxHeight = Math.max(height1, height2);
    
        BufferedImage resizedImage1 = resizeImage(timg, maxWidth, maxHeight);
        BufferedImage resizedImage2 = resizeImage(timg2, maxWidth, maxHeight);
    
        int[][][] imageArray1 = convertToArray(resizedImage1);
        int[][][] imageArray2 = convertToArray(resizedImage2);
        int[][][] newImage = new int[maxWidth][maxHeight][4];
    
        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;
    
        for (int y = 0; y < maxWidth; y++) {
            for (int x = 0; x < maxHeight; x++) {
                for (int i = 0; i < 4; i++) {
                    newImage[y][x][i] = imageArray1[y][x][i] + imageArray2[y][x][i];
                    minimum = Math.min(minimum, newImage[y][x][i]);
                    maximum = Math.max(maximum, newImage[y][x][i]);
                }
            }
        }
    
        for (int y = 0; y < maxWidth; y++) {
            for (int x = 0; x < maxHeight; x++) {
                for (int i = 0; i < 4; i++) { 
                    newImage[y][x][i] = (int) Math.round((newImage[y][x][i] - minimum) * (255.0 / (maximum - minimum)));
                }
            }
        }

        repaint();
    
        return convertToBimage(newImage);
    }
    
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
    
        BufferedImage newResizedPicture = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = newResizedPicture.createGraphics();
        graphics2D.drawImage(originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);
        graphics2D.dispose();
    
        return newResizedPicture;
    }
    

    public BufferedImage NotOperator(BufferedImage timg) {

        int height = timg.getHeight();
        int width = timg.getWidth();

        int [][][] ImageArray = convertToArray(timg);

        int [][][] ImageArray2 = new int[width][height][4];

        for (int x = 0; x < width; x++) {

            for (int y = 0; y < height; y++) {

                int r = ImageArray[x][y][1];
                int g = ImageArray[x][y][2];
                int b = ImageArray[x][y][3];

                ImageArray2[x][y][1] = (~r) &0xFF;
                ImageArray2[x][y][2] = (~g) &0xFF;
                ImageArray2[x][y][3] = (~b) &0xFF;

            }

        }

        return convertToBimage(ImageArray2);

    }

    public BufferedImage AndImage(BufferedImage timg, BufferedImage timg2) {
    
        int maxWidth = Math.max(timg.getWidth(), timg2.getWidth());
        int maxHeight = Math.max(timg.getHeight(), timg2.getHeight());
    
        BufferedImage resizedImage1 = resizeImage(timg, maxWidth, maxHeight);
        BufferedImage resizedImage2 = resizeImage(timg2, maxWidth, maxHeight);
    
        int[][][] ImageArray = convertToArray(resizedImage1);
        int[][][] ImageArray2 = convertToArray(resizedImage2);

        int[][][] AndImage = new int[maxWidth][maxHeight][4];
    
        for (int a = 0; a < maxWidth; a++) {

            for (int b = 0; b < maxHeight; b++) {

                int[] image1 = ImageArray[a][b];
                int[] image2 = ImageArray2[a][b];
                
                int[] andimage = new int[4];

                for (int x = 0; x < 4; x++) {

                    andimage[x] = image1[x] & image2[x];

                }

                for (int x = 0; x < 4; x++) {

                    AndImage[a][b][x] = Math.max(0, Math.min(andimage[x], 255));

                }
            
            }
        }

        return convertToBimage(AndImage);

    }

    public BufferedImage OrImage(BufferedImage timg, BufferedImage timg2) {
    
        int maxWidth = Math.max(timg.getWidth(), timg2.getWidth());
        int maxHeight = Math.max(timg.getHeight(), timg2.getHeight());
    
        BufferedImage resizedImage1 = resizeImage(timg, maxWidth, maxHeight);
        BufferedImage resizedImage2 = resizeImage(timg2, maxWidth, maxHeight);
    
        int[][][] ImageArray = convertToArray(resizedImage1);
        int[][][] ImageArray2 = convertToArray(resizedImage2);

        int[][][] OrImage = new int[maxWidth][maxHeight][4];
    
        for (int a = 0; a < maxWidth; a++) {

            for (int b = 0; b < maxHeight; b++) {

                int[] image1 = ImageArray[a][b];
                int[] image2 = ImageArray2[a][b];
                
                int[] orimage = new int[4];

                for (int x = 0; x < 4; x++) {

                    orimage[x] = image1[x] | image2[x];

                }

                for (int x = 0; x < 4; x++) {

                    OrImage[a][b][x] = Math.max(0, Math.min(orimage[x], 255));

                }
            
            }
        }

        return convertToBimage(OrImage);

    }

    public BufferedImage XorImage(BufferedImage timg, BufferedImage timg2) {
    
        int maxWidth = Math.max(timg.getWidth(), timg2.getWidth());
        int maxHeight = Math.max(timg.getHeight(), timg2.getHeight());
    
        BufferedImage resizedImage1 = resizeImage(timg, maxWidth, maxHeight);
        BufferedImage resizedImage2 = resizeImage(timg2, maxWidth, maxHeight);
    
        int[][][] ImageArray = convertToArray(resizedImage1);
        int[][][] ImageArray2 = convertToArray(resizedImage2);

        int[][][] XorImage = new int[maxWidth][maxHeight][4];
    
        for (int a = 0; a < maxWidth; a++) {

            for (int b = 0; b < maxHeight; b++) {

                int[] image1 = ImageArray[a][b];
                int[] image2 = ImageArray2[a][b];
                
                int[] xorimage = new int[4];

                for (int x = 0; x < 4; x++) {

                    xorimage[x] = image1[x] ^ image2[x];

                }

                for (int x = 0; x < 4; x++) {

                    XorImage[a][b][x] = Math.max(0, Math.min(xorimage[x], 255));

                }
            
            }
        }

        return convertToBimage(XorImage);

    }

    public BufferedImage ROIArithmetic(BufferedImage timg, BufferedImage timg2, int xcoords, int ycoords, int boxheight, int boxwidth) {

        int Boxw = boxwidth;
        int Boxh = boxheight;
        int w = timg2.getWidth();
        int h = timg2.getHeight();


        BufferedImage RoiInterest = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        Graphics2D RoiBox = RoiInterest.createGraphics();

        RoiBox.setColor(Color.WHITE);
        RoiBox.fillRect(xcoords, ycoords, Boxw, Boxh);

        BufferedImage resultImage = new BufferedImage(Boxw, Boxh, BufferedImage.TYPE_INT_RGB);
        Graphics2D resultGraphics = resultImage.createGraphics();
        resultGraphics.drawImage(timg2.getSubimage(xcoords, ycoords, Boxw, Boxh), 0, 0, null);
    
        return resultImage;

    }

    public BufferedImage LogFunct(BufferedImage timg, int c) {


        int width = timg.getWidth();
        int height = timg.getHeight();
        int[][][] ImageArray = convertToArray(timg);


        for(int y = 0; y < height; y++){

            for(int x = 0; x < width; x++){

                ImageArray[x][y][1] = (int) (c * Math.log(1 + ImageArray[x][y][1]));
                ImageArray[x][y][2] = (int) (c * Math.log(1 + ImageArray[x][y][2]));
                ImageArray[x][y][3] = (int) (c * Math.log(1 + ImageArray[x][y][3]));

            }

        }
        
        return convertToBimage(ImageArray);

    }

    public BufferedImage PowerFunct(BufferedImage timg, int c, double p) {


        int width = timg.getWidth();
        int height = timg.getHeight();
        int[][][] ImageArray = convertToArray(timg);


        for(int y = 0; y < height; y++){

            for(int x = 0; x < width; x++){

                ImageArray[x][y][1] = (int) (c * Math.pow(ImageArray[x][y][1], p));
                ImageArray[x][y][2] = (int) (c * Math.pow(ImageArray[x][y][2], p));
                ImageArray[x][y][3] = (int) (c * Math.pow(ImageArray[x][y][3], p));

            }

        }
        
        return convertToBimage(ImageArray);

    }

    public BufferedImage LUTLogFunc(BufferedImage timg) {

        int[] LUT = new int[256];

        Random rand = new Random();

        int w = timg.getWidth();
        int h = timg.getHeight();
        int[][][] ImageArray = convertToArray(timg);
        int[][][] ImageArray2 = new int[w][h][4];

        for (int k = 0; k < 256; k++) {

            LUT[k] = rand.nextInt(256);

        }

        for (int x = 0; x < h; x++) {

            for (int y = 0; y < w; y++) {

                int red = ImageArray[y][x][1];

                int green = ImageArray[y][x][2];

                int blue = ImageArray[y][x][3];

                ImageArray2[y][x][1] = LUT[red];
                ImageArray2[y][x][2] = LUT[green];
                ImageArray2[y][x][3] = LUT[blue];

            }

        }

        return convertToBimage(ImageArray2);

    }

    public BufferedImage BitPlane(BufferedImage timg, int k) {

        int w = timg.getWidth();
        int h = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);

        BufferedImage resultImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for(int y = 0; y < h; y++) {

            for(int x = 0; x < w; x++) {

                int red = ImageArray[x][y][1];
                int green = ImageArray[x][y][2];
                int blue = ImageArray[x][y][3];

                int rBit = (red >> k) & 1;
                int gBit = (green >> k) & 1;
                int bBit = (blue >> k) & 1;

                int pixels = (rBit * 255 << 16) | (gBit * 255 << 8) | (bBit * 255);

                resultImage.setRGB(x, y, pixels);
            }

        }

        return resultImage;


    }

    public BufferedImage Convolution(BufferedImage timg, int[][] mask) {

        int w = timg.getWidth();
        int h = timg.getHeight();
        int r;
        int g;
        int b;

        int[][][] ImageArray = convertToArray(timg);

        int[][][] ImageArray2 = new int[w][h][4];

        for (int y = 1; y < h - 1; y++) {

            for (int x = 1; x < w - 1; x++ ) {

                r = 0;
                g = 0;
                b = 0;

                for (int s = -1; s <= 1; s++) {

                    for (int t = -1; t <= 1; t++) {

                        r = r + mask[1-s][1-t] * ImageArray[x+s][y+t][1];

                        g = g + mask[1-s][1-t] * ImageArray[x+s][y+t][2];

                        b = b + mask[1-s][1-t] * ImageArray[x+s][y+t][3];

                    }

                }

                r = Math.min(Math.max(r, 0), 255);
                g = Math.min(Math.max(g, 0), 255);
                b = Math.min(Math.max(b, 0), 255);

                r = Math.round(r);
                g = Math.round(g);
                b = Math.round(b);

                ImageArray2[x][y][1] = r;
                ImageArray2[x][y][2] = g;
                ImageArray2[x][y][3] = b;

            }

        }

        return convertToBimage(ImageArray2);


    }

    public BufferedImage SaltAndPepper(BufferedImage timg, double ratio) {

        int w = timg.getWidth();
        int h = timg.getHeight();
        int[][][] ImageArray = convertToArray(timg);
        int[][][] ImageArray2 = new int[w][h][4];

        Random rand = new Random();

        for (int a = 0; a < h; a++) {

            for (int b = 0; b < w; b++) {

                ImageArray2[b][a][1] = ImageArray[b][a][1];
                ImageArray2[b][a][2] = ImageArray[b][a][2];
                ImageArray2[b][a][3] = ImageArray[b][a][3];
            
            }

        }

        int capacity = (int) (ratio * h * w);

        for (int x = 0; x < capacity; x++) {

            int xcords = rand.nextInt(w);
            int ycord = rand.nextInt(h);

            int saltorpepper = rand.nextBoolean() ? 255 :0;

            ImageArray2[xcords][ycord][1] = saltorpepper;
            ImageArray2[xcords][ycord][2] = saltorpepper; 
            ImageArray2[xcords][ycord][3] = saltorpepper;
        }

        return convertToBimage(ImageArray2);

    }

    public BufferedImage MinFilter(BufferedImage timg, int matrixsize) {

        int w = timg.getWidth();
        int h = timg.getHeight();

        BufferedImage MinFilter = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < w; x++) {

            for (int y = 0; y < h; y++) {

                int halfmatrix = matrixsize / 2;

                int minRed = 255;

                int minGreen = 255;

                int minBlue = 255;

                for (int i = -halfmatrix; i <= halfmatrix; i++) {

                    for (int j = -halfmatrix; j <= halfmatrix; j++) {

                        int px = Math.min(Math.max(x + j, 0), w - 1);

                        int py = Math.min(Math.max(y + i, 0), h - 1);

                        int rgb = timg.getRGB(px, py);

                        int red = (rgb >> 16) & 0xFF;

                        int green = (rgb >> 8) & 0xFF;

                        int blue = rgb & 0xFF;

                        minRed = Math.min(minRed, red);

                        minGreen = Math.min(minGreen, green);

                        minBlue = Math.min(minBlue, blue);

                    }
                }

                int minRGB = (minRed << 16) | (minGreen << 8) | minBlue;

                MinFilter.setRGB(x, y, minRGB);

            }

        }

        return MinFilter;

    }

    public BufferedImage MaxFilter(BufferedImage timg, int matrixSize) {

        int w = timg.getWidth();
        int h = timg.getHeight();

        BufferedImage MaxFilter = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {

            for (int x = 0; x < w; x++) {

                int halfMatrix = matrixSize / 2;

                int maxRed = 0;

                int maxGreen = 0;

                int maxBlue = 0;

                for (int i = -halfMatrix; i <= halfMatrix; i++) {

                    for (int j = -halfMatrix; j <= halfMatrix; j++) {

                        int px = Math.min(Math.max(x + j, 0), w - 1);

                        int py = Math.min(Math.max(y + i, 0), h - 1);

                        int rgb = timg.getRGB(px, py);

                        int red = (rgb >> 16) & 0xFF;

                        int green = (rgb >> 8) & 0xFF;

                        int blue = rgb & 0xFF;

                        maxRed = Math.max(maxRed, red);

                        maxGreen = Math.max(maxGreen, green);

                        maxBlue = Math.max(maxBlue, blue);

                    }
                }

                int maxRGB = (maxRed << 16) | (maxGreen << 8) | maxBlue;

                MaxFilter.setRGB(x, y, maxRGB);
            }

        }

        return MaxFilter;
    }

    public BufferedImage MidFilter(BufferedImage timg, int matrixSize) {

        int w = timg.getWidth();
        int h = timg.getHeight();

        BufferedImage MidFilter = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {

            for (int x = 0; x < w; x++) {

                int halfMatrix = matrixSize / 2;

                int minRed = 255;
                
                int minGreen = 255;

                int minBlue = 255;

                int maxRed = 0;

                int maxGreen = 0;

                int maxBlue = 0;

                for (int i = -halfMatrix; i <= halfMatrix; i++) {

                    for (int j = -halfMatrix; j <= halfMatrix; j++) {

                        int px = Math.min(Math.max(x + j, 0), w - 1);

                        int py = Math.min(Math.max(y + i, 0), h - 1);

                        int rgb = timg.getRGB(px, py);

                        int red = (rgb >> 16) & 0xFF;

                        int green = (rgb >> 8) & 0xFF;

                        int blue = rgb & 0xFF;

                        minRed = Math.min(minRed, red);

                        minGreen = Math.min(minGreen, green);

                        minBlue = Math.min(minBlue, blue);

                        maxRed = Math.max(maxRed, red);

                        maxGreen = Math.max(maxGreen, green);

                        maxBlue = Math.max(maxBlue, blue);
                    }
                }

                int midRed = (minRed + maxRed) / 2;

                int midGreen = (minGreen + maxGreen) / 2;

                int midBlue = (minBlue + maxBlue) / 2;

                int midRGB = (midRed << 16) | (midGreen << 8) | midBlue;

                MidFilter.setRGB(x, y, midRGB);
            }
        }

        return MidFilter;
    }

    public BufferedImage MidPoint(BufferedImage timg, int matrixSize) {
        int w = timg.getWidth();
        int h = timg.getHeight();

        BufferedImage MidFilter = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {

            for (int x = 0; x < w; x++) {

                int halfMatrix = matrixSize / 2;

                int minRed = 255;

                int minGreen = 255;

                int minBlue = 255;

                int maxRed = 0;

                int maxGreen = 0;

                int maxBlue = 0;

                for (int i = -halfMatrix; i <= halfMatrix; i++) {

                    for (int j = -halfMatrix; j <= halfMatrix; j++) {

                        int px = Math.min(Math.max(x + j, 0), w - 1);

                        int py = Math.min(Math.max(y + i, 0), h - 1);

                        int rgb = timg.getRGB(px, py);

                        int red = (rgb >> 16) & 0xFF;

                        int green = (rgb >> 8) & 0xFF;

                        int blue = rgb & 0xFF;

                        minRed = Math.min(minRed, red);

                        minGreen = Math.min(minGreen, green);

                        minBlue = Math.min(minBlue, blue);

                        maxRed = Math.max(maxRed, red);

                        maxGreen = Math.max(maxGreen, green);
                        
                        maxBlue = Math.max(maxBlue, blue);
                    }
                }

                int midRed = (minRed + maxRed) / 2;
                int midGreen = (minGreen + maxGreen) / 2;
                int midBlue = (minBlue + maxBlue) / 2;

                int midRGB = (midRed << 16) | (midGreen << 8) | midBlue;
                MidFilter.setRGB(x, y, midRGB);
            }
        }

        return MidFilter;
    }

    public BufferedImage MedianPoint(BufferedImage timg, int matrixSize) {

        int w = timg.getWidth();
        int h = timg.getHeight();

        BufferedImage MidFilter = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {

            for (int x = 0; x < w; x++) {

                int redSum = 0;

                int greenSum = 0;

                int blueSum = 0;

                for (int i = -matrixSize / 2; i <= matrixSize / 2; i++) {

                    for (int j = -matrixSize / 2; j <= matrixSize / 2; j++) {

                        int px = Math.min(Math.max(x + j, 0), w - 1);

                        int py = Math.min(Math.max(y + i, 0), h - 1);

                        Color color = new Color(timg.getRGB(px, py));

                        redSum += color.getRed();

                        greenSum += color.getGreen();

                        blueSum += color.getBlue();

                    }
                }


                int medianRed = redSum / (matrixSize * matrixSize);
                int medianGreen = greenSum / (matrixSize * matrixSize);
                int medianBlue = blueSum / (matrixSize * matrixSize);

                MidFilter.setRGB(x, y, new Color(medianRed, medianGreen, medianBlue).getRGB());
            
            }
        }

        return MidFilter;

    }

    public String Histogram(BufferedImage timg) {

        int w = timg.getWidth();
        int h = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);
        int[] RedHisto = new int[256];
        int[] GreenHisto = new int[256];
        int[] BlueHisto = new int[256];

        int redcount;
        int greencount;
        int bluecount;

        for (int i = 0; i < 256; i++) {

            RedHisto[i] = 0;
            GreenHisto[i] = 0;
            BlueHisto[i] = 0;

        }

        for (int y = 0; y < h; y++) {

            for (int x = 0; x < w; x++) {

                redcount = ImageArray[x][y][1];

                greencount = ImageArray[x][y][2];

                bluecount = ImageArray[x][y][3];

                RedHisto[redcount]++;

                GreenHisto[greencount]++;

                BlueHisto[bluecount]++;

            }

        }

        StringBuilder RedString = new StringBuilder("Red Histogram:\n");
        StringBuilder GreenString = new StringBuilder("Green Histogram:\n");
        StringBuilder BlueString = new StringBuilder("Blue Histogram:\n");
        StringBuilder CombinedString = new StringBuilder();
    
        for (int i = 0; i < 256; i++) {
            RedString.append("Bin ").append(i).append(": ").append(RedHisto[i]).append("\n");
            GreenString.append("Bin ").append(i).append(": ").append(GreenHisto[i]).append("\n");
            BlueString.append("Bin ").append(i).append(": ").append(BlueHisto[i]).append("\n");
        }
    
        CombinedString.append(RedString).append("\n").append(GreenString).append("\n").append(BlueString);

        return CombinedString.toString();

    }

    public String NormHistogram(BufferedImage timg) {

        int w = timg.getWidth();
        int h = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);
        int[] RedHisto = new int[256];
        int[] GreenHisto = new int[256];
        int[] BlueHisto = new int[256];
        double[] NormRedHisto = new double[256];
        double[] NormGreenHisto = new double[256];
        double[] NormBlueHisto = new double[256];

        int redcount;
        int greencount;
        int bluecount;

        for (int i = 0; i < 256; i++) {

            RedHisto[i] = 0;
            GreenHisto[i] = 0;
            BlueHisto[i] = 0;

        }

        for (int y = 0; y < h; y++) {

            for (int x = 0; x < w; x++) {

                redcount = ImageArray[x][y][1];

                greencount = ImageArray[x][y][2];

                bluecount = ImageArray[x][y][3];

                RedHisto[redcount]++;

                GreenHisto[greencount]++;

                BlueHisto[bluecount]++;

            }

        }

        for (int k = 0; k < 256; k++) {

            NormRedHisto[k] = RedHisto[k] / (double) (h * w);
            NormGreenHisto[k] = GreenHisto[k] / (double) (h * w);
            NormBlueHisto[k] = BlueHisto[k] / (double) (h * w);

        }

        StringBuilder NormRedHistoString = new StringBuilder("Norm Red Histogram:\n");
        StringBuilder NormGreenHistoString = new StringBuilder("Norm Green Histogram:\n");
        StringBuilder NormBlueHistoString = new StringBuilder("Norm Blue Histogram:\n");
        StringBuilder NormCombinedString = new StringBuilder();
    
        for (int i = 0; i < 256; i++) {
            NormRedHistoString.append("Norm Bin ").append(i).append(": ").append(NormRedHisto[i]).append("\n");
            NormGreenHistoString.append("Norm Bin ").append(i).append(": ").append(NormGreenHisto[i]).append("\n");
            NormBlueHistoString.append("Norm Bin ").append(i).append(": ").append(NormBlueHisto[i]).append("\n");
        }
    
        NormCombinedString.append(NormRedHistoString).append("\n").append(NormGreenHistoString).append("\n").append(NormBlueHistoString);

        return NormCombinedString.toString();


    }

    public BufferedImage EqualisedHisto(BufferedImage timg) {

        int w = timg.getWidth();
        int h = timg.getHeight();
        int MaxGreyLevel = 255;

        int[][][] ImageArray = convertToArray(timg);
        BufferedImage newpic = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[] RedHisto = new int[256];
        int[] GreenHisto = new int[256];
        int[] BlueHisto = new int[256];
        double[] NormRedHisto = new double[256];
        double[] NormGreenHisto = new double[256];
        double[] NormBlueHisto = new double[256];
        double[] CumulativeRed = new double[256];
        double[] CumulativeGreen = new double[256];
        double[] CumulativeBlue = new double[256];
        double RedSum = 0, GreenSum = 0, BlueSum = 0;

        int redcount;
        int greencount;
        int bluecount;

        for (int i = 0; i < 256; i++) {

            RedHisto[i] = 0;
            GreenHisto[i] = 0;
            BlueHisto[i] = 0;

        }

        for (int y = 0; y < h; y++) {

            for (int x = 0; x < w; x++) {

                redcount = ImageArray[x][y][1];

                greencount = ImageArray[x][y][2];

                bluecount = ImageArray[x][y][3];

                RedHisto[redcount]++;

                GreenHisto[greencount]++;

                BlueHisto[bluecount]++;

            }

        }

        for (int k = 0; k < 256; k++) {

            NormRedHisto[k] = RedHisto[k] / (double) (h * w);
            NormGreenHisto[k] = GreenHisto[k] / (double) (h * w);
            NormBlueHisto[k] = BlueHisto[k] / (double) (h * w);

        }

        for (int i = 0; i < 256; i++) {

            RedSum = RedSum + NormRedHisto[i];
            GreenSum = GreenSum + NormGreenHisto[i];
            BlueSum = BlueSum + NormBlueHisto[i];

            CumulativeRed[i] = RedSum;
            CumulativeGreen[i] = GreenSum;
            CumulativeBlue[i] = BlueSum;

        }

        
        for (int y = 0; y < h; y++) {

            for (int x = 0; x < w; x++) {

                int red = ImageArray[x][y][1];
                int green = ImageArray[x][y][2];
                int blue = ImageArray[x][y][3];
                int newRed = (int) Math.round(CumulativeRed[red] * MaxGreyLevel);
                int newGreen = (int) Math.round(CumulativeGreen[green] * MaxGreyLevel);
                int newBlue = (int) Math.round(CumulativeBlue[blue] * MaxGreyLevel);
                int rgb = (newRed << 16) | (newGreen << 8) | newBlue;
                newpic.setRGB(x, y, rgb);

            }
    
        }

        return newpic;

    }

    public String MeanStandardDeviation(BufferedImage timg) {

        int w = timg.getWidth();
        int h = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);
        int[] RedHisto = new int[256];
        int[] GreenHisto = new int[256];
        int[] BlueHisto = new int[256];
        double[] NormRedHisto = new double[256];
        double[] NormGreenHisto = new double[256];
        double[] NormBlueHisto = new double[256];
        double MeanRed = 0.0;
        double MeanGreen = 0.0;
        double MeanBlue = 0.0;
        double StandardRed = 0.0;
        double StandardGreen = 0.0;
        double StandardBlue = 0.0;

        int redcount;
        int greencount;
        int bluecount;

        for (int i = 0; i < 256; i++) {

            RedHisto[i] = 0;
            GreenHisto[i] = 0;
            BlueHisto[i] = 0;

        }

        for (int y = 0; y < h; y++) {

            for (int x = 0; x < w; x++) {

                redcount = ImageArray[x][y][1];

                greencount = ImageArray[x][y][2];

                bluecount = ImageArray[x][y][3];

                RedHisto[redcount]++;

                GreenHisto[greencount]++;

                BlueHisto[bluecount]++;

            }

        }

        for (int k = 0; k < 256; k++) {

            NormRedHisto[k] = RedHisto[k] / (double) (h * w);
            NormGreenHisto[k] = GreenHisto[k] / (double) (h * w);
            NormBlueHisto[k] = BlueHisto[k] / (double) (h * w);

        }

        for (int i = 0; i < 256; i++) {

            MeanRed = MeanRed + (i * NormRedHisto[i]);
            MeanGreen = MeanGreen + (i * NormGreenHisto[i]);
            MeanBlue = MeanBlue + (i * NormBlueHisto[i]);

        }

        for (int i = 0; i < 256; i++) {

            StandardRed = Math.sqrt((StandardRed + Math.pow((i - MeanRed), 2) * NormRedHisto[i]));
            StandardGreen = Math.sqrt((StandardGreen + Math.pow((i - MeanGreen), 2) * NormGreenHisto[i]));
            StandardBlue = Math.sqrt((StandardBlue + Math.pow((i - MeanBlue), 2) * NormBlueHisto[i]));

        }

        return "Mean Red: " + MeanRed + ", Mean Green: " + MeanGreen + ", Mean Blue: " + MeanBlue +
        "\nStandard Deviation Red: " + StandardRed + ", Standard Deviation Green: " + StandardGreen +
        ", Standard Deviation Blue: " + StandardBlue;

    }

    public BufferedImage thresholding(BufferedImage timg, int tresh) {

        int w = timg.getWidth();
        int h = timg.getHeight();

        BufferedImage ImageArray2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {

            for (int x = 0; x < w; x++) {

                int focus = timg.getRGB(x, y);

                int red = (focus >> 16) & 0xFF;
                int green = (focus >> 8) & 0xFF;
                int blue = focus & 0xFF;

                int intensity = (int) (0.299 * red + 0.587 * green + 0.114 * blue);

                if (intensity > tresh) {

                    ImageArray2.setRGB(x, y, Color.WHITE.getRGB());

                } else {

                    ImageArray2.setRGB(x, y, Color.BLACK.getRGB());

                }

            }


        }


        return ImageArray2;

    }    

    public BufferedImage AutomaticThresholding(BufferedImage timg) {

        int w = timg.getWidth();
        int h = timg.getHeight();
        int[] histogram = new int[256];
        int HighestPoint = 0;
        int HighestIndex = 0;
        int LowestPoint = Integer.MAX_VALUE;
        int LowestIndex = 0;

        BufferedImage greyscale = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

        greyscale.getGraphics().drawImage(timg, 0, 0, null);

        for (int y = 0; y < h; y++) {

            for (int x = 0; x < w; x++) {

                int focus = timg.getRGB(x, y) & 0xFF;

                histogram[focus]++;
            }

        }

        int histolen = histogram.length;

        for (int i = 0; i < histolen; i++) {

            if (histogram[i] > HighestPoint) {

                HighestPoint = histogram[i];
                
                HighestIndex = i;
            }

        }

        
        for (int i = HighestIndex; i < histolen; i++) {

            if (histogram[i] < LowestPoint) {

                LowestPoint = histogram[i];
                
                LowestIndex = i;
            }

        }

        return thresholding(timg, LowestPoint);


    }



    //************************************
    //  You need to register your function here
    //************************************
    public void filterImage() {
        // Apply selected image processing filter
        if (opIndex == lastOp) {
            return;
        }

        lastOp = opIndex;


        switch (opIndex) {
            case 0: 
                biFiltered = bi; // Original
                return; 
            case 1: 
                historyStack.push(biFiltered);
                biFiltered = ImageNegative(biFiltered);
                System.out.println("Saved to stack");
                return;
            case 2: 
                biFiltered = ImageUndo();
                System.out.println("Loaded");
                return;

            case 3:
                historyStack.push(biFiltered);
                biFiltered = rescale(biFiltered, 2);
                System.out.println("Resized");
                return;
            case 4:
                historyStack.push(biFiltered);
                biFiltered = ValueShifting(biFiltered, 200);
                System.out.println("Scaled");
                return;
            case 5:
                historyStack.push(biFiltered);
                biFiltered = RescaleShifting(biFiltered);
                System.out.println("Scaled and Resized");
                return;
            case 6:
                historyStack.push(biFiltered);
                biFiltered = twoPicsArithmetic(biFiltered, bitwo);
                System.out.println("2 Images into 1");
                return;
            case 7:
                historyStack.push(biFiltered);
                biFiltered = NotOperator(biFiltered);
                System.out.println("NOT Operator");
                return;
            case 8:
                historyStack.push(biFiltered);
                biFiltered = AndImage(biFiltered, bitwo);
                System.out.println("And");
                return;
            case 9:
                historyStack.push(biFiltered);
                biFiltered = OrImage(biFiltered, bitwo);
                System.out.println("Or");
                return;
            case 10:
                historyStack.push(biFiltered);
                biFiltered = XorImage(biFiltered, bitwo);
                System.out.println("XOR");
                return;
            case 11:
                historyStack.push(biFiltered);   
                biFiltered = ROIArithmetic(biFiltered, bitwo, 100, 100, 100, 100); 
                System.out.println("Break");
                return;
            case 12:
                historyStack.push(biFiltered);
                biFiltered = LogFunct(biFiltered, 300);
                System.out.println("Log Function");
                return;
            case 13:
                historyStack.push(biFiltered);
                biFiltered = PowerFunct(biFiltered, 300, 1);
                System.out.println("Power Function");
                return;
            case 14:
                historyStack.push(biFiltered);
                biFiltered = LUTLogFunc(biFiltered);
                System.out.println("Random Look Up Table");
                return;
            case 15:
                historyStack.push(biFiltered);
                biFiltered = BitPlane(biFiltered, 5);
                System.out.println("Bit Plane");
                return;
            case 16:
                historyStack.push(biFiltered);
                int[][] mask = {

                    {0, 0, 0},
                    {0, 0, 1},
                    {0, 1, 0}
                };
                biFiltered = Convolution(biFiltered, mask);
                System.out.println("Convolution");
                return;
            case 17:
                historyStack.push(biFiltered);
                biFiltered = SaltAndPepper(biFiltered, 0.1);
                System.out.println("Salt And Pepper");  
                return;
            case 18:
                historyStack.push(biFiltered);
                biFiltered = MinFilter(biFiltered, 3);
                System.out.println("Min Filter");
                return;
            case 19:
                historyStack.push(biFiltered);
                biFiltered = MaxFilter(biFiltered, 3);
                System.out.println("Max Filter");
                return;
            case 20:
                historyStack.push(biFiltered);
                biFiltered = MidPoint(biFiltered, 3);
                System.out.println("Mid Filter");
                return;
            case 21:
                historyStack.push(biFiltered);
                biFiltered = MedianPoint(biFiltered, 3);
                System.out.println("Average Filter");
                return;
            case 22:
                historyStack.push(biFiltered);
                String histo = Histogram(biFiltered);
                System.out.println(histo);
                return;
            case 23:
                historyStack.push(biFiltered);
                String normhisto = NormHistogram(biFiltered);
                System.out.println(normhisto);
                return;
            case 24:
                historyStack.push(biFiltered);
                biFiltered = EqualisedHisto(biFiltered);
                System.out.println("Equalised Histogram Picture");
                return;
            case 25:
                historyStack.push(biFiltered);
                String MeanAndStandard = MeanStandardDeviation(biFiltered);
                System.out.println(MeanAndStandard);
                return;
            case 26:
                historyStack.push(biFiltered);
                biFiltered = thresholding(biFiltered, 100);
                System.out.println("Thresholding Picture");
                return; 
            case 27:
                historyStack.push(biFiltered);
                biFiltered = AutomaticThresholding(biFiltered);
                System.out.println("Automatic Thresholding Picture");
                return; 

        }

            
    }



    public void actionPerformed(ActionEvent e) {
        // Handle action events from JComboBoxes
        JComboBox cb = (JComboBox)e.getSource();
        if (cb.getActionCommand().equals("SetFilter")) {
            setOpIndex(cb.getSelectedIndex());
            repaint(); // Repaint the component
        } else if (cb.getActionCommand().equals("Formats")) {
            String format = (String)cb.getSelectedItem();
            File saveFile = new File("savedimage."+format);
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(saveFile);
            int rval = chooser.showSaveDialog(cb);
            if (rval == JFileChooser.APPROVE_OPTION) {
                saveFile = chooser.getSelectedFile();
                try {
                    ImageIO.write(biFiltered, format, saveFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String s[]) {
        // Main method: Create JFrame and initialize the demo
        JFrame f = new JFrame("Image Processing Demo");
        f.getContentPane().setBackground(Color.BLACK);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        Demo de = new Demo();
        f.add("Center", de);

        // Create JComboBoxes for selecting image processing options and formats
        JComboBox choices = new JComboBox(de.getDescriptions());
        choices.setActionCommand("SetFilter");
        choices.addActionListener(de);
        JComboBox formats = new JComboBox(de.getFormats());
        formats.setActionCommand("Formats");
        formats.addActionListener(de);

        // Add JComboBoxes to JPanel and add the JPanel to JFrame
        JPanel panel = new JPanel();
        panel.add(choices);
        panel.add(new JLabel("Save As"));
        panel.add(formats);
        f.add("North", panel);

        f.pack(); // Pack the JFrame
        f.setVisible(true); // Make the JFrame visible
    }
}
