package jp.tinyport.pollinghttp.core;

import android.app.Activity;

import jp.tinyport.pollinghttp.core.di.ActivityComponent;
import jp.tinyport.pollinghttp.core.di.ActivityModule;

public class BaseActivity extends Activity {
    private ActivityComponent sComponent;

    protected ActivityComponent getComponent() {
        if (sComponent == null) {
            sComponent = ((App) getApplication()).getComponent()
                    .plus(new ActivityModule());
        }

        return sComponent;
    }
}
