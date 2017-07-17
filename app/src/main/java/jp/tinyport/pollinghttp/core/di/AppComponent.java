package jp.tinyport.pollinghttp.core.di;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    ActivityComponent plus(ActivityModule module);

    ServiceComponent plus(ServiceModule module);
}
