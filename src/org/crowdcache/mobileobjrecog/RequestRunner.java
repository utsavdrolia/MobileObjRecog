package org.crowdcache.mobileobjrecog;

import de.greenrobot.event.EventBus;
import org.crowdcache.Cache;
import org.crowdcache.client.AnnotationRequester;
import org.crowdcache.mobileobjrecog.cache.DistObjectRecogCache;
import org.crowdcache.mobileobjrecog.cache.ObjectRecogCache;

import java.io.*;

/**
 * Created by utsav on 2/10/16.
 */
public class RequestRunner extends Thread
{
    private static final String PATH_TO_IMAGES = "/sdcard/DCIM/Camera/Objects/qlist.txt";
    private static final String LOG = "/sdcard/DCIM/Camera/Objects/log";
    private static final String FINISH = "/sdcard/DCIM/Camera/Objects/finish";
    private static final String SERVER_ADDRESS = "192.168.1.13:50505";
    private static final Double CONFIDENCE_THRESHOLD = 63.0;
    private AnnotationRequester req;
    private DistObjectRecogCache cache;

    public RequestRunner()
    {
        req = new AnnotationRequester(SERVER_ADDRESS);
        cache = new DistObjectRecogCache(10, CONFIDENCE_THRESHOLD, 2000l);
    }


    public void requestAnnotations(int intExtra) throws IOException
    {
        cache.setTIMEOUT(intExtra);
        this.start();
    }

    public void run()
    {
        try
        {
            File finish = new File(FINISH);
            finish.delete();
            BufferedReader imagelist = new BufferedReader(new FileReader(PATH_TO_IMAGES));
            BufferedWriter resultsfile = new BufferedWriter(new FileWriter(LOG));
            String line = imagelist.readLine();
            Long starttime = System.currentTimeMillis();
            Byte cachehit = 0;
            do
            {
                String[] chunks = line.split(",");
                String img = chunks[0];
                String imgpath = chunks[1];
                Long reqtime = Long.valueOf(chunks[2]);
                String result = "None";
                cachehit = 0;

                while((System.currentTimeMillis() - starttime)/1000 < reqtime)
                    sleep(100);
                Long end2;
                Long start1 = System.currentTimeMillis();
//                // Extract
//                KeypointDescList kpdesc = orb.extract(imgpath);
                // Check Cache
                Cache.Result<String> res = cache.get(imgpath);
                Long end1 = System.currentTimeMillis();
                if(res.confidence > CONFIDENCE_THRESHOLD)
                {
                    // If not in Cache get from server
                    Long start2 = System.currentTimeMillis();
                    result = req.requestAnnotation(imgpath);
                    end2 = System.currentTimeMillis();

                    String name = result.split(",")[0];
                    if (!name.equals("None"))
                        cache.put(imgpath, name);
                }
                else
                {
                    result = res.value;
                    end2 = System.currentTimeMillis();
                    cachehit = 1;
                }

                resultsfile.write(img + "," + result + "," + Long.toString(end2 - start1) + "," + cachehit + "\n");
                EventBus.getDefault().post(new RequestResult(img + "," + res.value + "," + Long.toString(end1 - start1) + "," + result + "," + Long.toString(end2 - end1) + "," + Long.toString(end2 - start1)));
                line = imagelist.readLine();
            } while (line != null);
            resultsfile.flush();
            resultsfile.close();
            BufferedWriter finishfile = new BufferedWriter(new FileWriter(FINISH));
            finishfile.write("1");
            finishfile.close();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
