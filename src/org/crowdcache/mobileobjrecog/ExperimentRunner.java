package org.crowdcache.mobileobjrecog;

import edu.cmu.edgecache.objrec.opencv.Util;
import edu.cmu.edgecache.objrec.rpc.ObjRecClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Runs the trace based experiments
 * Created by utsav on 2/10/16.
 */
public class ExperimentRunner extends Thread
{
    private static final String REQUEST = "request";
    private static final String STARTTIME = "start";
    private static final String ENDTIME = "end";
    private static final String RESPONSE = "response";
    private static final String LATENCIES = "latencies";
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
            File finish = new File(FINISH);
            finish.delete();


            ConcurrentLinkedQueue<Util.EvaluateCallback> evaluateCallbacks = new ConcurrentLinkedQueue<>();
            BufferedReader trace = new BufferedReader(new FileReader(tracepath));
            BufferedWriter resultsfile = new BufferedWriter(new FileWriter(resultspath));

            Integer count = 0;
            String line = trace.readLine();
            Long procstart = System.currentTimeMillis();
            do
            {
                // Parse
                String[] chunks = line.split(",");
                String img = chunks[0];
                String imgpath = chunks[1];
                Long req_time = Long.valueOf(chunks[2]);

                // Create callback
                Util.EvaluateCallback cb = new Util.EvaluateCallback(System.currentTimeMillis(), img);

                // Wait till assigned time
                while((System.currentTimeMillis() - procstart) < req_time)
                    sleep(100l);
                mObjRecClient.recognize(imgpath, cb);
                evaluateCallbacks.add(cb);

//            resultsfile.write(img.split("_")[0] + "," + resultMap.get(img).annotation + "," + (1 - (resultMap.get(img).time.size() - 1)) + "," + "\n");
                line = trace.readLine();
                count++;
            } while ((line != null));
//        System.out.println("Results:\n" + resultMap.toString());

            Long procend = System.currentTimeMillis() - procstart;
            System.out.println("Time:" + procend + " Count:" + count);

            // Write results to file
            writeResultFile(evaluateCallbacks, resultsfile);
            resultsfile.flush();
            resultsfile.close();

            // Create finish file
            BufferedWriter finishfile = new BufferedWriter(new FileWriter(FINISH));
            finishfile.write("1");
            finishfile.close();
        }
        catch (IOException | InterruptedException | JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Writes JSON file. Format
     * [{REQUEST:"",
     *   START:ms,
     *   END:ms,
     *   RESPONSE:"",
     *   LATENCIES:{DEVICE:{QUEUE:ms, COMPUTE:ms, NEXT:ms}, ... }},
     *   {REQUEST:"", ...}
     *  ]
     * @param evaluateCallbacks
     * @param resultsfile
     * @throws IOException
     */
    private void writeResultFile(Collection<Util.EvaluateCallback> evaluateCallbacks, BufferedWriter resultsfile) throws IOException, JSONException
    {
        JSONArray output_array = new JSONArray();
        for(Util.EvaluateCallback callback: evaluateCallbacks)
        {
            // Ensure callback is processed
            while(!callback.isDone());
            Util.Result result = callback.getResult();
            JSONObject json = new JSONObject();

            Map<String, Map<String, Integer>> map = result.getLatencies();
            json.put(REQUEST, callback.getQuery());
            json.put(STARTTIME, callback.getStartime());
            json.put(ENDTIME, callback.getEndtime());
            json.put(RESPONSE, result.getAnnotation());
            json.put(LATENCIES, map);

            output_array.put(json);
        }

        resultsfile.write(output_array.toString());
    }


}
