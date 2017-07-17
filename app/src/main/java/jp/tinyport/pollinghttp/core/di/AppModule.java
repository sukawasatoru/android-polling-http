package jp.tinyport.pollinghttp.core.di;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import jp.tinyport.pollinghttp.core.repository.AppOrma;

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

    @Singleton
    @Provides
    public AppOrma provideAppOrma(Context context) {
        return AppOrma.builder(context)
                .name("app.db")
                .build();
    }
}
