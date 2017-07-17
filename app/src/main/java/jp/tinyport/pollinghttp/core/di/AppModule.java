package jp.tinyport.pollinghttp.core.di;

import android.app.Application;
import android.app.job.JobScheduler;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import jp.tinyport.pollinghttp.core.repository.OrmaDatabase;

@Module
public class AppModule {
    private final Context mContext;

    public AppModule(Application app) {
        mContext = app;
    }

    @Provides
    public Context provideContext() {
        return mContext;
    }

    @Provides
    public JobScheduler provideJobScheduler(Context context) {
        return (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @Singleton
    @Provides
    public OrmaDatabase provideOrma(Context context) {
        return OrmaDatabase.builder(context)
                .name("app.db")
                .build();
    }
}
