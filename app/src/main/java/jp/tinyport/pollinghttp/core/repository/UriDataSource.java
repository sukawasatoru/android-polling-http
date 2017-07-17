package jp.tinyport.pollinghttp.core.repository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.annotations.CheckReturnValue;
import jp.tinyport.pollinghttp.core.model.UriModel;

public class UriDataSource {
    private final OrmaDatabase mOrma;

    @Inject
    public UriDataSource(OrmaDatabase orma) {
        mOrma = orma;
    }

    @CheckReturnValue
    public Single<List<UriModel>> findAll() {
        return mOrma.selectFromUriModel().executeAsObservable().toList();
    }

    @CheckReturnValue
    public Single<Long> save(UriModel model) {
        // primary key defined on table annotation.
        return mOrma.prepareInsertIntoUriModel().executeAsSingle(model);
    }

    @CheckReturnValue
    public Single<Integer> delete(UriModel model) {
        return mOrma.deleteFromUriModel()
                .uriEq(model.uri)
                .methodEq(model.method)
                .executeAsSingle();
    }
}
