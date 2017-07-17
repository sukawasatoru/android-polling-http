package jp.tinyport.pollinghttp.core.di;

import dagger.Subcomponent;
import jp.tinyport.pollinghttp.PollingService;
import jp.tinyport.pollinghttp.core.PollingServiceImpl;
import jp.tinyport.pollinghttp.core.di.scope.ServiceScope;

@ServiceScope
@Subcomponent(modules = ServiceModule.class)
public interface ServiceComponent {
    void inject(PollingService service);

    void inject(PollingServiceImpl service);
}
