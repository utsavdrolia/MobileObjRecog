package org.crowdcache.mobileobjrecog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by utsav on 2/21/17.
 */
public abstract class   ExperimentActivity extends OpenCVActivity
{
    private ArrayAdapter<String> mLogAdapter;
    private ArrayList<String> mListLog;
    private ListView mListView;

    private static final String EXPERIMENTINTENT = "org.crowdcache.app.intent.EXPERIMENT";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // To get rid of the "network on main thread" error.
        // This happens because Hyrax talks to jyre over a pipe and the pipe is considered a network socket.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        EventBus.getDefault().register(this);
        mListView = (ListView) findViewById(R.id.log);
        mListLog = new ArrayList<>();
        mLogAdapter = new ArrayAdapter<>(this, R.layout.item_console, mListLog);
        mListView.setAdapter(mLogAdapter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        IntentFilter filter = new IntentFilter(EXPERIMENTINTENT);
        this.registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                if (action.equals(EXPERIMENTINTENT))
                {
                    setLogText("Received Intent");
                    experimentIntentReceived(intent);
                }
            }
        }, filter);
    }

    /**
     * Called when experiment Intent is received.
     * @param intent
     */
    public abstract void experimentIntentReceived(Intent intent);

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RequestResult event)
    {
        setLogText(event.getResult());
    }

    /**
     * Add text to the view log
     *
     * @param text
     */
    public void setLogText(String text)
    {
        mListLog.add(text);
        mLogAdapter.notifyDataSetChanged();
        mListView.setSelection(mListView.getCount() - 1);
    }

    @Override
    public void onStop()
    {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
