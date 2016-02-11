package org.crowdcache.mobileobjrecog;

import android.app.Activity;
import org.opencv.android.OpenCVLoader;

/**
 * Created by utsav on 2/10/16.
 */
public class OpenCVActivity extends Activity
{
    static {
        if (!OpenCVLoader.initDebug())
        {
            System.out.println("OPENCV NOT LOADED");
        }
        else
        {
            System.out.println("OPENCV LOADED");
        }
    }
}
