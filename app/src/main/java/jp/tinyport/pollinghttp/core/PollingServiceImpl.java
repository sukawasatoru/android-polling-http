package jp.tinyport.pollinghttp.core;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import jp.tinyport.pollinghttp.core.di.ServiceModule;
import jp.tinyport.pollinghttp.core.model.UriModel;
import jp.tinyport.pollinghttp.core.repository.UriRepository;

public class PollingServiceImpl extends JobService {
    private static final String TAG = "PollingHttp";
    private static final String KEY_URI = "uri";
    private static final String KEY_METHOD = "method";
    private static final String KEY_BODY = "body";

    private static ComponentName sComponentName;
    private static SparseArray<String> sUrisHash;
    private static int sCounter;

    @Inject
    UriRepository mRepo;

    private CompositeDisposable mDisposables;

    public static void start(
            Context context, UriRepository repo, String uri, String method, String body) {
        int id;
        final ComponentName componentName;
        synchronized (PollingServiceImpl.class) {
            if (sComponentName == null) {
                sComponentName = new ComponentName(context, PollingServiceImpl.class);
            }
            componentName = sComponentName;

            if (sUrisHash == null) {
                sUrisHash = new SparseArray<>();
            }

            final String hash = generateUriHash(uri, method);
            id = sUrisHash.indexOfValue(hash);
            if (id < 0) {
                id = sCounter++;
                sUrisHash.put(id, hash);
            }
        }

        final JobScheduler js =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        final PersistableBundle bundle = new PersistableBundle(1);
        bundle.putString(KEY_URI, uri);
        bundle.putString(KEY_METHOD, method);
        bundle.putString(KEY_BODY, body);
        final int result = js.schedule(new JobInfo.Builder(id, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setExtras(bundle)
                .build());

        if (result == JobScheduler.RESULT_SUCCESS) {
            log("success");
        } else {
            log("failed " + result);
        }

        final UriModel model = new UriModel();
        model.uri = uri;
        model.method = method;
        model.body = body;

        Completable.fromAction(() -> repo.save(model).toCompletable().subscribe())
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public static void stop(Context context, UriRepository repo, String uri, String method) {
        final int id;
        synchronized (PollingServiceImpl.class) {
            id = sUrisHash.indexOfValue(generateUriHash(uri, method));
        }

        if (id < 0) {
            log("id not found. uri: " + uri + ", method: " + method);
            return;
        }


        final JobScheduler js =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        js.cancel(id);

        final UriModel model = new UriModel();
        model.uri = uri;
        model.method = method;
        repo.delete(model).toCompletable().subscribe();
    }

    @Override
    public void onCreate() {
        log("onCreate");

        ((App) getApplication()).getComponent().plus(new ServiceModule()).inject(this);

        mDisposables = new CompositeDisposable();
    }

    @Override
    public void onDestroy() {
        log("onDestroy");

        mDisposables.dispose();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        log("onStartJob");

        final String uriString = params.getExtras().getString(KEY_URI);
        if (uriString == null) {
            // sticky?
            log("TODO: uri is null. id: " + params.getJobId());
            return false;
        }

        mDisposables.add(("post".equals(params.getExtras().getString(KEY_METHOD)) ?
                createPostObservable(params) : createGetObservable(params))
                .toList()
                .map(list -> {
                    final StringBuilder builder = new StringBuilder();
                    for (String string : list) {
                        builder.append(string);
                    }
                    return builder.toString();
                })
                .doOnSuccess(data -> log("doOnSuccess " + new Date() + data))
                .delay(1, TimeUnit.MINUTES)
                .repeat()
                .retry(3)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(data -> log("onNext " + data), // onNext will never call.
                        throwable -> log("failed " + Log.getStackTraceString(throwable)),
                        () -> {
                            log("onComplete");
                            jobFinished(params, false);
                        },
                        subscription -> log("subscription")));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        log("onStopJob");

        mDisposables.clear();
        final PersistableBundle bundle = params.getExtras();
        start(this, mRepo, bundle.getString(KEY_URI),
                bundle.getString(KEY_METHOD), bundle.getString(KEY_BODY));
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        log("onConfigurationChanged");
    }

    @Override
    public void onTrimMemory(int level) {
        log("onTrimMemory " + level);
    }

    private Observable<String> createGetObservable(JobParameters params) {
        return Observable.create(emitter -> {
            final String uri = params.getExtras().getString(KEY_URI);
            log("create " + uri);
            final URL url = new URL(uri);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(30));
            connection.setReadTimeout((int) TimeUnit.MINUTES.toMillis(1));
            connection.setInstanceFollowRedirects(true);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(),
                            Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    emitter.onNext(line);
                }
                emitter.onComplete();
            }
        });
    }

    private Observable<String> createPostObservable(JobParameters params) {
        return Observable.create(emitter -> {
            final PersistableBundle bundle = params.getExtras();
            final String uri = bundle.getString(KEY_URI);
            final String body = bundle.getString(KEY_BODY);
            log("create uri: " + uri + ", body: " + body);
            final URL url = new URL(params.getExtras().getString(KEY_URI));
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(30));
            connection.setReadTimeout((int) TimeUnit.MINUTES.toMillis(1));
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(),
                            Charset.defaultCharset().name()))) {
                writer.write(body);
                writer.flush();
                writer.close();
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(),
                            Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    emitter.onNext(line);
                }
                emitter.onComplete();
            }
        });
    }

    private static void log(String message) {
        Log.i(TAG, "PollingServiceImpl " + message);
    }

    private static String generateUriHash(String uri, String method) {
        return uri + "-" + method;
    }
}
