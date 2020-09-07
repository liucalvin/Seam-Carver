import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    
    private int[][] pixels;
    private int width;
    private int height;
    private boolean inverted;
    
    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        checkNull(picture);
        width = picture.width();
        height = picture.height();
        pixels = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[i][j] = picture.getRGB(i, j);
            }
        }
    }
    
    //  unit testing (optional)
    public static void main(String[] args) {
        SeamCarver seamCarver = new SeamCarver(
                new Picture("C:\\Users\\User\\Desktop\\siamese cat.jpg"));
        seamCarver.picture().show();
        System.out.println(seamCarver.width() + " by " + seamCarver.height());
        for (int i = 0; i < 200; i++) {
            int[] temp = seamCarver.findHorizontalSeam();
            seamCarver.removeHorizontalSeam(temp);
        }
        System.out.println(seamCarver.width() + " by " + seamCarver.height());
        seamCarver.picture().show();
        
    }
    
    // current picture
    public Picture picture() {
        if (inverted) {
            invertPicture();
        }
        Picture picture = new Picture(width, height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                picture.setRGB(i, j, pixels[i][j]);
            }
        }
        return picture;
    }
    
    private void invertPicture() {
        inverted = !inverted;
        
        int[][] temp = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                temp[i][j] = pixels[j][i];
            }
        }
        
        // switch dimensions
        int p = width;
        width = height;
        height = p;
        pixels = temp;
    }
    
    // width of current picture
    public int width() {
        if (inverted) {
            return height;
        } else {
            return width;
        }
    }
    
    // height of current picture
    public int height() {
        if (inverted) {
            return width;
        } else {
            return height;
        }
    }
    
    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x >= width) {
            throw new IllegalArgumentException(String.format(
                    "Col number %d must be a number between %d and %d", x, 0, width));
        }
        if (y < 0 || y >= height)
            throw new IllegalArgumentException(String.format(
                    "Col number %d must be a number between %d and %d", y, 0, height));
        // border pixels have energy = 1000
        if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
            return 1000;
        }
        return Math.sqrt(getGradientDifference(x + 1, y, x - 1, y) + getGradientDifference(x, y + 1, x, y - 1));
    }
    
    private double getGradientDifference(int x1, int y1, int x2, int y2) {
        int rgb1 = pixels[x1][y1];
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = (rgb1) & 0xFF;
        int rgb2 = pixels[x2][y2];
        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = (rgb2) & 0xFF;
        int r = r1 - r2;
        int g = g1 - g2;
        int b = b1 - b2;
        return r * r + g * g + b * b;
    }
    
    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        if (!inverted) {
            invertPicture();
        }
        return findVerticalSeam();
    }
    
    // sequence of indices for vertical seam
    // to map from 2D grid to 1D array, use arr[i][j] = arr[width * i + j], where 0 < j < width (and 0 < i < height)
    public int[] findVerticalSeam() {
        // if width is one, the vertical seam is the only pixels available, all at index[i][0], so the seam is all zeros
        if (width == 1) {
            return new int[height];
        }
        width = pixels.length;
        height = pixels[0].length;
        double[] energy = new double[width * height];
        
        // calculate energy of each pixel
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                energy[(width * i) + j] = energy(j, i);
            }
        }
        
        int[] pixelTo = new int[width * height];
        double[] distTo = new double[width * height];
        // initialize weights of all pixels to max value
        for (int i = 0; i < width * height; i++) {
            distTo[i] = Integer.MAX_VALUE;
        }
        // initialize weights of border pixels to 1000
        for (int i = 0; i < width; i++) {
            distTo[i] = 1000;
        }
        
        // consider the pixels in topological order (no need to compute this order)
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = width * i + j;
                int adj;
                // relax each neighbouring pixel downwards
                // if pixel is on bottom border, don't search
                if (((pixel - (pixel % width)) / width) >= (height - 1)) {
                    continue;
                }
                // if pixel is not on the leftmost two pixels, search downwards left pixel
                if (((pixel % width) != 0) && ((pixel % width) != 1)) {
                    adj = pixel + width - 1;
                    if (distTo[adj] > distTo[pixel] + energy[pixel]) {
                        distTo[adj] = distTo[pixel] + energy[pixel];
                        pixelTo[adj] = pixel;
                    }
                }
                // if pixel is not on the rightmost two pixels, search downwards right pixel
                if (((pixel % width) != (width - 1)) && ((pixel % width) != (width - 2))) {
                    adj = pixel + width + 1;
                    if (distTo[adj] > distTo[pixel] + energy[pixel]) {
                        distTo[adj] = distTo[pixel] + energy[pixel];
                        pixelTo[adj] = pixel;
                    }
                }
                // if pixel is not on the leftmost or rightmost pixel, search directly downwards
                if (((pixel % width) != 0) && ((pixel % width) != (width - 1))) {
                    adj = pixel + width;
                    if (distTo[adj] > distTo[pixel] + energy[pixel]) {
                        distTo[adj] = distTo[pixel] + energy[pixel];
                        pixelTo[adj] = pixel;
                    }
                }
            }
        }
//        System.out.println(Arrays.toString(distTo));
        // find seam by retracing path from the bottom pixel with least cost
        int[] seam = new int[height];
        double minWeight = Double.POSITIVE_INFINITY;
        int minPixel = 0;
        for (int i = 0; i < width; i++) {
            int index = width * (height - 1) + i;
            if (distTo[index] < minWeight) {
                minWeight = distTo[index];
                minPixel = index;
            }
        }
        int ind = minPixel;
        for (int i = height - 1; i >= 0; i--) {
//            System.out.printf("ind: %d, ind %% width: %d, pixelTo[ind]: %d\n", ind, ind % width, pixelTo[ind]);
            seam[i] = ind % width;
            ind = pixelTo[ind];
        }
        return seam;
    }
    
    // remove horizontal seam from current picture
    // horizontal seams are removed by inverting the picture and removing a vertical seam
    public void removeHorizontalSeam(int[] seam) {
        if (!inverted) {
            invertPicture();
        }
        removeVerticalSeam(seam);
    }
    
    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        // should have length of height and max entry of width
        checkSeam(seam, height, width);
        // set pixel to remove to -1
        int[][] changed = new int[width - 1][height];
        for (int i = 0; i < height; i++) {
            int remove = seam[i];
            boolean removed = false;
            int y = 0;
            for (int j = 0; j < width; j++) {
                if (y != remove || removed) {
                    changed[y][i] = pixels[j][i];
                    y++;
                } else {
                    removed = true;
                }
            }
        }
        width--;
        pixels = changed;
    }
    
    // seams must be paths from one end of an image to the other
    private void checkSeam(int[] seam, int length, int range) {
        checkNull(seam);
        if (seam.length != length) throw new IllegalArgumentException("Length of seam incorrect");
        int lastIndex = seam[0];
        for (int value : seam) {
            // two adjacent entries can differ by no more than 1
            if (Math.abs(lastIndex - value) > 1) throw new IllegalArgumentException("Invalid seam");
            lastIndex = value;
            if (value < 0 || value >= range) throw new IllegalArgumentException(String.format(
                    "Seam entry outside of prescribed range: %d, in range between %d and %d", value, 0, range));
        }
    }
    
    private <T> void checkNull(T t) {
        if (t == null) {
            throw new IllegalArgumentException("Cannot be null");
        }
    }
}
