package org.crowdcache.mobileobjrecog.cache;

import org.crowdcache.Cache;
import org.crowdcache.LRUCache;
import org.crowdcache.mobileobjrecog.FeatureExtractor;
import org.crowdcache.mobileobjrecog.KeypointDescList;
import org.crowdcache.mobileobjrecog.Matcher;
import org.crowdcache.mobileobjrecog.extractors.BRISK;
import org.crowdcache.mobileobjrecog.extractors.FREAK;
import org.crowdcache.mobileobjrecog.extractors.ORB;
import org.crowdcache.mobileobjrecog.matchers.BFMatcher_HAM;
import org.crowdcache.mobileobjrecog.matchers.BFMatcher_HAM2;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;


/**
 * Created by utsav on 2/9/16.
 */
public class ObjectRecogCache implements Cache<KeypointDescList, String>
{
    protected final FeatureExtractor extractor;
    private final LRUCache<String, KeypointDescList> cache;
    private Matcher matcher;
    private ExecutorService executorService;

    public ObjectRecogCache(Integer size)
    {
        this.extractor = new ORB("/sdcard/DCIM/Camera/Objects/orb_pars");
        this.matcher = new BFMatcher_HAM2();
        this.cache = new LRUCache<>(size);
        this.executorService = Executors.newFixedThreadPool(size);
    }

    /**
     * Store the keypoints and descriptors along with the annotation
     *
     * @param key
     * @param value
     */
    public void put(KeypointDescList key, String value)
    {
            cache.put(value, key);
    }

    /**
     * Get the best value for this key along with a confidence metric
     *
     * @param key
     * @return
     */
    public Result<String> get(KeypointDescList key)
    {
        return parallelMatch(key);
    }

    /**
     * Match input list to all known lists
     * @param inputKDlist
     * @return
     */
    public Result<String> parallelMatch(final KeypointDescList inputKDlist)
    {
            HashMap<String, Future<Double>> matches = new HashMap<String, Future<Double>>(cache.size(), 4.0f);
            Double score = Double.MAX_VALUE;
            String ret = "None";

            HashMap<String, KeypointDescList> copy = new HashMap<>(cache);
            //-- Match against all DB --
            for (final Map.Entry<String, KeypointDescList> entry : copy.entrySet())
            {
                matches.put(entry.getKey(), executorService.submit(new Callable<Double>()
                {
                    public Double call() throws Exception
                    {
                        return matcher.match(entry.getValue(), inputKDlist);
                    }
                }));
            }

            for (Map.Entry<String, Future<Double>> future : matches.entrySet())
            {
                try
                {
                    Double matchscore = future.getValue().get();
                    if (matchscore < score)
                    {
                        score = matchscore;
                        ret = future.getKey();
                    }
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
            }

            return new Result<>(score, ret);
    }

    public Result<String> get(String imgpath)
    {
        Mat newpath = reduce(imgpath);
        return this.get(this.extractor.extract(newpath));
    }

    public Result<String> get(byte[] imgpath)
    {
        return this.get(this.extractor.extract(imgpath));
    }

    protected Mat reduce(String imgpath)
    {
        Mat img = Highgui.imread(imgpath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat dst = new Mat();
        Imgproc.resize(img, dst, new Size(img.width() / 2, img.height() / 2));
//        MatOfByte bytemat = new MatOfByte();
//        Highgui.imencode(".jpg", dst, bytemat, new MatOfInt(Highgui.CV_IMWRITE_JPEG_QUALITY, 30));
        return dst;
    }

    public void put(String imgpath, String value)
    {
        Mat newpath = reduce(imgpath);
        this.put(this.extractor.extract(newpath), value);
    }

    public Result<String> get(Mat mat)
    {
        return this.get(this.extractor.extract(mat));
    }
}
