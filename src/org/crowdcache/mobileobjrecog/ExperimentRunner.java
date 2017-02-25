package org.crowdcache.mobileobjrecog;

import edu.cmu.edgecache.objrec.opencv.Util;
import edu.cmu.edgecache.objrec.rpc.ObjRecClient;
import org.greenrobot.eventbus.EventBus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static edu.cmu.edgecache.objrec.opencv.Util.evaluateAsync;

/**
 * Runs the trace based experiments
 * Created by utsav on 2/10/16.
 */
public class ExperimentRunner extends Thread
{
    private final String FINISH;
    private final String resultspath;
    private ObjRecClient mObjRecClient;
    private String tracepath;

    /**
     *
     * @param objRecClient
     * @param finish
     * @param resultspath
     * @param tracepath - Path to trace file.
     *                  Format for trace -
     *                  <ObjectID>,<PathToRequestImage>,<RequestTime in ms>
     */
    public ExperimentRunner(ObjRecClient objRecClient,
                            String finish,
                            String resultspath,
                            String tracepath)
    {
        mObjRecClient = objRecClient;
        FINISH = finish;
        this.resultspath = resultspath;
        this.tracepath = tracepath;
    }

    /**
     * Runs the given trace.
     */
    public void run()
    {
        try
        {
            // Delete any old finish file
            File finish = new File(FINISH);
            finish.delete();

            evaluateAsync(mObjRecClient, tracepath, resultspath, new PrintCallBack());

            // Create finish file
            BufferedWriter finishfile = new BufferedWriter(new FileWriter(FINISH));
            finishfile.write("1");
            finishfile.close();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private class PrintCallBack extends Util.AppCallBack
    {
        @Override
        public void call(String req, String annotation, long latency)
        {
            EventBus.getDefault().post(new RequestResult(req + "," + annotation + "," + latency));
        }
    }
}
