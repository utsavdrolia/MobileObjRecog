package org.crowdcache.mobileobjrecog.matchers;

import org.crowdcache.mobileobjrecog.KeypointDescList;
import org.crowdcache.mobileobjrecog.Matcher;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.highgui.Highgui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by utsav on 2/6/16.
 */
public class BFMatcher_HAM implements Matcher
{
    private static final int NUM_MATCHES_THRESH = 30;

    public Double match(KeypointDescList dbImage, KeypointDescList sceneImage)
    {
        MatOfDMatch matches = new MatOfDMatch();
//        List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
        List<DMatch> good_matches;
//        List<DMatch> good_matches = new ArrayList<DMatch>();
        List<Point> good_dbkp = new ArrayList<Point>();
        List<Point> good_scenekp = new ArrayList<Point>();
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        matcher.match(dbImage.descriptions, sceneImage.descriptions, matches);
//        matcher.knnMatch(dbImage.descriptions, sceneImage.descriptions, matches, 2);
        Mat inliers = new Mat();
        good_matches = matches.toList();
        Collections.sort(good_matches, new Comparator<DMatch>()
        {
            public int compare(DMatch o1, DMatch o2)
            {
                return (int) (o1.distance - o2.distance);
            }
        });
//        for(MatOfDMatch dmatch: matches)
//        {
//            DMatch[] arr = dmatch.toArray();
//            DMatch m = arr[0];
//            DMatch n = arr[1];
//            if(m.distance < 0.6*n.distance)
//                good_matches.add(m);
//        }

        if(good_matches.size() > NUM_MATCHES_THRESH)
        {
            List<DMatch> best_matches = good_matches.subList(0, NUM_MATCHES_THRESH);
            for(DMatch match:best_matches)
            {
                good_dbkp.add(dbImage.points.get(match.queryIdx).pt);
                good_scenekp.add(sceneImage.points.get(match.trainIdx).pt);
            }

            MatOfPoint2f good_dbpoints = new MatOfPoint2f();
            good_dbpoints.fromList(good_dbkp);

            MatOfPoint2f good_scenepoints = new MatOfPoint2f();
            good_scenepoints.fromList(good_scenekp);

            Calib3d.findHomography(good_dbpoints, good_scenepoints, Calib3d.RANSAC, 5.0, inliers);
//            System.out.println("Good Matches:" + good_matches.size() + " Inliers:" + Core.sumElems(inliers).val[0]);
            return Core.sumElems(inliers).val[0]/best_matches.size();
        }
        return 0.0;
    }
}
