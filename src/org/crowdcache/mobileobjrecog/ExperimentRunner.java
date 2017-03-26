package org.crowdcache.mobileobjrecog;

import edu.cmu.edgecache.objrec.opencv.Util;
import edu.cmu.edgecache.objrec.rpc.ObjRecClient;
import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static edu.cmu.edgecache.objrec.opencv.Util.evaluate;
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
    private boolean async = true;
    final static Logger logger = LoggerFactory.getLogger(ExperimentRunner.class);


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
                            String tracepath,
                            boolean async)
    {
        mObjRecClient = objRecClient;
        FINISH = finish;
        this.resultspath = resultspath;
        this.tracepath = tracepath;
        this.async = async;
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

            if(async)
                evaluateAsync(mObjRecClient, tracepath, resultspath, new PrintCallBack());
            else
                evaluate(mObjRecClient, tracepath, resultspath, new PrintCallBack());

            logger.info("Finished evaluation");

            // Create finish file
            BufferedWriter finishfile = new BufferedWriter(new FileWriter(FINISH));
            finishfile.write("1");
            finishfile.close();
            logger.info("Wrote Finish file");
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
