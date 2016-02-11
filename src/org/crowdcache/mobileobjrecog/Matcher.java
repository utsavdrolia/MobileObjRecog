package org.crowdcache.mobileobjrecog;

/**
 * Created by utsav on 2/7/16.
 */
public interface Matcher
{
    /**
     * Match the 2 images and return a match score
     * @param dbImage
     * @param sceneImage
     * @return
     */
    Double match(KeypointDescList dbImage, KeypointDescList sceneImage);
}
