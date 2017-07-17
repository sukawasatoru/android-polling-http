package jp.tinyport.pollinghttp.core;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import jp.tinyport.pollinghttp.core.di.AppComponent;
import jp.tinyport.pollinghttp.core.di.AppModule;
import jp.tinyport.pollinghttp.core.di.DaggerAppComponent;

public class App extends Application {
    private AppComponent mComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        if (LeakCanary.isInAnalyzerProcess(this)) return;

        LeakCanary.install(this);
    }

    public AppComponent getComponent() {
        return mComponent;
    }
}
