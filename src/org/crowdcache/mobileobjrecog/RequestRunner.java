package org.crowdcache.mobileobjrecog;

import de.greenrobot.event.EventBus;
import org.crowdcache.client.AnnotationRequester;
import org.crowdcache.mobileobjrecog.extractors.ORB;

import java.io.*;

/**
 * Created by utsav on 2/10/16.
 */
public class RequestRunner extends Thread
{
    private static final String PATH_TO_IMAGES = "/sdcard/DCIM/Camera/Objects/qlist.txt";
    private static final String LOG = "/sdcard/DCIM/Camera/Objects/log";
    private static final String SERVER_ADDRESS = "192.168.1.13:50505";
    private AnnotationRequester req;
    private ORB orb;
    public RequestRunner()
    {
        req = new AnnotationRequester(SERVER_ADDRESS);
        orb = new ORB("/sdcard/DCIM/Camera/Objects/orb_pars");
    }


    public void requestAnnotations() throws IOException
    {
        this.start();
    }

    public void run()
    {
        try
        {
            BufferedReader imagelist = new BufferedReader(new FileReader(PATH_TO_IMAGES));
            BufferedWriter resultsfile = new BufferedWriter(new FileWriter(LOG));
            String line = imagelist.readLine();
            do
            {
                String[] chunks = line.split(",");
                String img = chunks[0];
                String imgpath = chunks[1];
                Long start = System.currentTimeMillis();
//                String result = req.requestAnnotation(imgpath);
                KeypointDescList kpdesc = orb.extract(imgpath);
                Long end = System.currentTimeMillis();
//                resultsfile.write(img + "," + kpdesc.points.size() + "," + Long.toString(end - start) + "\n");
                EventBus.getDefault().post(new RequestResult(img + "," + kpdesc.points.size() + "," + Long.toString(end - start)));
                line = imagelist.readLine();
            } while (line != null);
            resultsfile.flush();
            resultsfile.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public class RequestResult
    {
        private String result;
        public RequestResult(String s)
        {
            result = s;
        }

        public String getResult()
        {
            return result;
        }
    }
}
