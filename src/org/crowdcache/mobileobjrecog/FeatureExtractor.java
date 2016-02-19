package org.crowdcache.mobileobjrecog;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

/**
 * Interface for Extracting features from given image and returning a datastructure that is used by the Associator
 * Created by utsav on 2/5/16.
 */
public abstract class FeatureExtractor
{
    /**
     * Extract features from an image
     * @param image A {@link Mat} representing the image
     * @return {@link KeypointDescList} containing the keypoints and descriptors
     */
    public abstract KeypointDescList extract(Mat image);

    /**
     * Extract feature from the image
     * @param inputFile path to the image
     * @return
     */
    public KeypointDescList extract(String inputFile)
    {
        return extract(Highgui.imread(inputFile, Highgui.CV_LOAD_IMAGE_GRAYSCALE));
    }

    /**
     * Extract feature from the image
     * @param data the image
     * @return
     */
    public KeypointDescList extract(byte[] data)
    {
        return extract(Highgui.imdecode(new MatOfByte(data), Highgui.CV_LOAD_IMAGE_GRAYSCALE));
    }
}
