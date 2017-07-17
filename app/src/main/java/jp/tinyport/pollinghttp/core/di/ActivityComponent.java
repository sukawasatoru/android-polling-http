package jp.tinyport.pollinghttp.core.di;

import dagger.Subcomponent;
import jp.tinyport.pollinghttp.MainActivity;
import jp.tinyport.pollinghttp.core.di.scope.ActivityScope;

@ActivityScope
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {
    void inject(MainActivity activity);
}
