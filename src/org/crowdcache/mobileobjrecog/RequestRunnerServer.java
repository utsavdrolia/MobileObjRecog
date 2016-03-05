package org.crowdcache.mobileobjrecog;

import de.greenrobot.event.EventBus;
import org.crowdcache.client.AnnotationRequester;

import java.io.*;

/**
 * Created by utsav on 2/10/16.
 */
public class RequestRunnerServer extends Thread
{
    private static final String PATH_TO_IMAGES = "/sdcard/DCIM/Camera/Objects/qlist.txt";
    private static final String LOG = "/sdcard/DCIM/Camera/Objects/log";
    private static final String FINISH = "/sdcard/DCIM/Camera/Objects/finish";
    private static final String SERVER_ADDRESS = "192.168.1.13:50505";
    private AnnotationRequester req;

    public RequestRunnerServer()
    {
        req = new AnnotationRequester(SERVER_ADDRESS);
    }


    public void requestAnnotations() throws IOException
    {
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
            do
            {
                String[] chunks = line.split(",");
                String img = chunks[0];
                String imgpath = chunks[1];
                Long reqtime = Long.valueOf(chunks[2]);

                while((System.currentTimeMillis() - starttime)/1000 < reqtime)
                    sleep(100);
                Long start1 = System.currentTimeMillis();
                String result = req.requestAnnotation(imgpath);
                Long end2 = System.currentTimeMillis();

                resultsfile.write(img + "," + result + "," + Long.toString(end2 - start1) + "\n");
                EventBus.getDefault().post(new RequestResult(img + "," + result + "," + Long.toString(end2 - start1)));
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
