package org.crowdcache.mobileobjrecog;

import android.content.Intent;
import edu.cmu.edgecache.objrec.opencv.Util;
import edu.cmu.edgecache.objrec.rpc.Names;
import edu.cmu.edgecache.objrec.rpc.ObjRecClient;

import java.io.IOException;

/**
 * This application for mobile recognition uses a local recognition cache
 */
public class TraceRunnerActivity extends ExperimentActivity
{
    private static final String QUERY = "QUERYPATH";
    private static final String FEATURE = "FEATURETYPE";
    private static final String FEATURE_PARS = "FEATUREPARSPATH";
    private static final String DB_FEATURE_PARS = "DB_FEATUREPARSPATH";
    private static final String DBLIST = "DBLIST_PATH";
    private static final String MATCHER = "MATCHERTYPE";
    private static final String MATCHER_PARS = "MATCHERPARSPATH";
    private static final String FINISHPATH = "FINISHPATH";
    private static final String RESULTPATH = "RESULTPATH";
    private static final String SERVER_ADD = "SERVER_ADD";
    private static final String F_K_PARS = "F_K";
    private static final String RECALL_PARS = "RECALL_K";
    private static final String ALL_OBJECTS = "ALL_OBJECTS";

    private static final String EXPERIMENT_TYPE = "EXP_TYPE";
    private static final String DEVICE_ONLY = "DEVICE_ONLY";
    private static final String SERVER_BASED = "SERVER_BASED";
    private static final String DEVICE_CACHE = "DEVICE_CACHE";
    private static final String PREFETCH_CACHE = "PREFETCH_CACHE";


    private ExperimentRunner mRunner;

    @Override
    public void experimentIntentReceived(Intent intent)
    {
        ObjRecClient objRecClient = null;
        boolean async = true;
        try
        {
            switch (intent.getStringExtra(EXPERIMENT_TYPE))
            {
                case DEVICE_ONLY:
                    objRecClient = Util.createLocalObjRecClient(intent.getIntExtra(FEATURE, Util.ORB),
                                                                intent.getStringExtra(FEATURE_PARS),
                                                                intent.getStringExtra(DB_FEATURE_PARS),
                                                                intent.getIntExtra(MATCHER, Util.BIN_NN),
                                                                intent.getStringExtra(MATCHER_PARS),
                                                                3,
                                                                0.5,
                                                                intent.getStringExtra(DBLIST),
                                                                Names.Device);
                    async = false;
                    break;
                case SERVER_BASED:
                    objRecClient = new ObjRecClient(intent.getStringExtra(SERVER_ADD));
                    break;
                case DEVICE_CACHE:
                    objRecClient = Util.createOptCacheObjRecClient(intent.getIntExtra(FEATURE, Util.ORB),
                                                                   intent.getStringExtra(FEATURE_PARS),
                                                                   intent.getIntExtra(MATCHER, Util.BIN_NN),
                                                                   intent.getStringExtra(MATCHER_PARS),
                                                                   3,
                                                                   0.5,
                                                                   intent.getStringExtra(SERVER_ADD),
                                                                   Names.Device,
                                                                   intent.getStringExtra(F_K_PARS),
                                                                   intent.getStringExtra(RECALL_PARS),
                                                                   intent.getStringExtra(ALL_OBJECTS));
                    break;
                case PREFETCH_CACHE:
                    objRecClient = Util.createPrefetchedObjRecClient(intent.getIntExtra(FEATURE, Util.ORB),
                                                                     intent.getStringExtra(FEATURE_PARS),
                                                                     intent.getIntExtra(MATCHER, Util.BIN_NN),
                                                                     intent.getStringExtra(MATCHER_PARS),
                                                                     3,
                                                                     0.5,
                                                                     intent.getStringExtra(SERVER_ADD),
                                                                     Names.Device,
                                                                     intent.getStringExtra(F_K_PARS),
                                                                     intent.getStringExtra(RECALL_PARS));
                    break;
                default:
                    // Server based
                    objRecClient = new ObjRecClient(intent.getStringExtra(SERVER_ADD));
            }
            mRunner = new ExperimentRunner(
                    objRecClient,
                    intent.getStringExtra(FINISHPATH),
                    intent.getStringExtra(RESULTPATH), intent.getStringExtra(QUERY),
                    async
            );
            mRunner.start();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
