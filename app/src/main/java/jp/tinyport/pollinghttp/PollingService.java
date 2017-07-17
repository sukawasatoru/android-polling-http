package jp.tinyport.pollinghttp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import javax.inject.Inject;

import jp.tinyport.pollinghttp.core.App;
import jp.tinyport.pollinghttp.core.PollingServiceImpl;
import jp.tinyport.pollinghttp.core.di.ServiceModule;
import jp.tinyport.pollinghttp.core.repository.UriRepository;

public class PollingService extends Service {
    private static final String TAG = "PollingHttp";

    @Inject
    UriRepository mRepo;

    @Override
    public void onCreate() {
        log("onCreate");

        ((App) getApplication()).getComponent().plus(new ServiceModule()).inject(this);
    }

    @Override
    public void onDestroy() {
        log("onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            log("restart not supported");
            stopSelf();
            return START_NOT_STICKY;
        }

        final String uri = intent.getStringExtra("uri");
        if (uri == null) {
            log(getUsage());
            stopSelf();
            return START_NOT_STICKY;
        }

        final String action = intent.getStringExtra("action");
        if ("start".equals(action)) {
            String method = intent.getStringExtra("method");
            if (method == null) {
                method = "get";
            }
            String body = intent.getStringExtra("body");
            if (body == null) {
                body = "";
            }
            PollingServiceImpl.start(this, mRepo, uri, method, body);
        } else if ("stop".equals(action)) {
            String method = intent.getStringExtra("method");
            if (method == null) {
                method = "get";
            }
            PollingServiceImpl.stop(this, mRepo, uri, method);
        } else {
            log(getUsage());
        }

        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind");

        return null;
    }

    private static void log(String message) {
        Log.i(TAG, "Starter " + message);
    }

    private static String getUsage() {
        return "action=[start|stop] uri=<uri> [method=[get|post]] [body=<body>]";
    }
}
