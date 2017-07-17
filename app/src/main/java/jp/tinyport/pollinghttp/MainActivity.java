package jp.tinyport.pollinghttp;

import android.os.Bundle;

import javax.inject.Inject;

import jp.tinyport.pollinghttp.core.BaseActivity;
import jp.tinyport.pollinghttp.core.repository.AppOrma;
import jp.tinyport.mylibrary.MainLibrary;

public class MainActivity extends BaseActivity {
    @Inject
    AppOrma mOrma;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getComponent().inject(this);

        setContentView(R.layout.activity_main);
        MainLibrary.hello();
    }
}
