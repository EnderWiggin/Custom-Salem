package com.nathantalewis.image;

import java.awt.image.BufferedImage;
import java.util.Comparator;

/**
 *
 * @author Nathan T.A. Lewis
 */
public class SparseBufferedImageComparator implements Comparator<BufferedImage> {

    private final Double accuracy;
    private static final int SAMPLES_PER_DIMENSION = 19;

    /**
     * Get the value of accuracy
     *
     * @return the value of accuracy
     */
    public Double getAccuracy() {
        return accuracy;
    }

    /**
     *
     * @param accuracy
     */
    public SparseBufferedImageComparator(Double accuracy) {
        this.accuracy = accuracy;
    }
    
    /**
     *
     * @param image1
     * @param image2
     * @return
     */
    @Override
    public int compare(BufferedImage image1, BufferedImage image2) {
        int[] image1samples = new int[SAMPLES_PER_DIMENSION*SAMPLES_PER_DIMENSION];
        int[] image2samples = new int[SAMPLES_PER_DIMENSION*SAMPLES_PER_DIMENSION];
        
        for(int j=0; j < SAMPLES_PER_DIMENSION; j++) {
            for(int i=0; i < SAMPLES_PER_DIMENSION; i++) {
                image1samples[j*SAMPLES_PER_DIMENSION + i] = 
                        image1.getRGB(((int) ((j + 0.5) * ((double) image1.getWidth())/SAMPLES_PER_DIMENSION)),
                                      ((int) ((i + 0.5) * ((double) image1.getHeight())/SAMPLES_PER_DIMENSION)));
                image2samples[j*SAMPLES_PER_DIMENSION + i] = 
                        image2.getRGB(((int) ((j + 0.5) * ((double) image2.getWidth())/SAMPLES_PER_DIMENSION)),
                                      ((int) ((i + 0.5) * ((double) image2.getHeight())/SAMPLES_PER_DIMENSION)));
            }
        }
        
        int[] sampleDifferences = new int[SAMPLES_PER_DIMENSION*SAMPLES_PER_DIMENSION];
        for(int i = 0; i < SAMPLES_PER_DIMENSION*SAMPLES_PER_DIMENSION; i++) {
            sampleDifferences[i] = RGBAbsoluteValue(image1samples[i]) - RGBAbsoluteValue(image2samples[i]);
        }
        
        int matchingSum = 0;
        for(int i = 0; i < SAMPLES_PER_DIMENSION*SAMPLES_PER_DIMENSION; i++) {
            matchingSum += sampleDifferences[i] == 0 ? 1 : 0;
        }
        
        int differenceSum = 0;
        for(int i = 0; i < SAMPLES_PER_DIMENSION*SAMPLES_PER_DIMENSION; i++) {
            differenceSum += sampleDifferences[i];
        }
    
        return ((double) matchingSum)/sampleDifferences.length > accuracy ? 0 : differenceSum;
    }
    
    private static int RGBAbsoluteValue(int rgb) {
        int absoluteValue = 0;
        for(int i = 0; i < 4; i++) {
            absoluteValue += (rgb >> i*8) & 0xFF;
        }        
        return absoluteValue;
    }
    
    
    @Override
    public boolean equals(Object o) {
        if(o instanceof SparseBufferedImageComparator) {
            return ((SparseBufferedImageComparator) o).getAccuracy() == this.accuracy;
        } else {
            return super.equals(o);
        }
    }
    
    
}
