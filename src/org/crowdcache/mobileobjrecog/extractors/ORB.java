package org.crowdcache.mobileobjrecog.extractors;

import org.crowdcache.mobileobjrecog.FeatureExtractor;
import org.crowdcache.mobileobjrecog.KeypointDescList;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

/**
 * Created by utsav on 2/8/16.
 */
public class ORB extends FeatureExtractor
{
    private FeatureDetector detector;
    private DescriptorExtractor extractor;

    public ORB(String pars)
    {
        System.out.println(pars);
        //Init detector
        detector = FeatureDetector.create(FeatureDetector.ORB);
        // Read the settings file for detector
//        detector.read(pars);
        extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
//        extractor.read(pars);
    }

    public KeypointDescList extract(Mat image)
    {
        //Keypoints
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        detector.detect(image, keypoints);

        extractor.compute(image, keypoints, descriptors);

        return new KeypointDescList(keypoints, descriptors);
    }

    /**
     * Extract from image at given path
     * @param image
     * @return
     */
    public KeypointDescList extract(String image)
    {
        return extract(Highgui.imread(image, Highgui.CV_LOAD_IMAGE_GRAYSCALE));
    }
}
