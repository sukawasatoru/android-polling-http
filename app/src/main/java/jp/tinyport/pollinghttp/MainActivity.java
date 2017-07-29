package jp.tinyport.pollinghttp;

import android.app.job.JobScheduler;
import android.os.Bundle;
import android.util.Log;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import jp.tinyport.pollinghttp.core.BaseActivity;
import jp.tinyport.pollinghttp.core.PollingServiceImpl;
import jp.tinyport.pollinghttp.core.model.UriModel;
import jp.tinyport.pollinghttp.core.repository.UriRepository;

public class MainActivity extends BaseActivity {
    private static final String TAG = "PollingHttp";

    @Inject
    UriRepository mRepo;

    @Inject
    JobScheduler mJobScheduler;

    private CompositeDisposable mDisposables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("Hello");

        super.onCreate(savedInstanceState);

        getComponent().inject(this);

        mDisposables = new CompositeDisposable();

        mJobScheduler.cancelAll();

        mDisposables.add(mRepo.findAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    for (UriModel model : data) {
                        PollingServiceImpl.start(this, mRepo, model.uri, model.method, model.body);
                    }
                }));
    }

    @Override
    protected void onDestroy() {
        log("Bye");

        super.onDestroy();

        mDisposables.dispose();
    }

    @Override
    protected void onRestart() {
        log("onRestart");

        super.onRestart();
    }

    @Override
    protected void onStart() {
        log("onStart");

        super.onStart();
    }

    @Override
    protected void onStop() {
        log("onStop");

        super.onStop();
    }

    @Override
    protected void onResume() {
        log("onResume");

        super.onResume();
    }

    @Override
    protected void onPause() {
        log("onPause");

        super.onPause();
    }

    private static void log(String message) {
        Log.i(TAG, "MainActivity " + message);
    }
}
