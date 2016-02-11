package org.crowdcache.mobileobjrecog;

/**
 * Created by utsav on 2/3/16.
 */

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.KeyPoint;

import java.util.List;

/**
 * Container for Image Keypoints and Descriptors
 */
public class KeypointDescList
{
    public final List<KeyPoint> points;
    public final Mat descriptions;

    public KeypointDescList(MatOfKeyPoint points, Mat descriptions)
    {
        this.points = points.toList();
        this.descriptions = descriptions;
    }
}