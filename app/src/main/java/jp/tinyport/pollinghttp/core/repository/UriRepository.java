package jp.tinyport.pollinghttp.core.repository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.schedulers.Schedulers;
import jp.tinyport.pollinghttp.core.model.UriModel;

@Singleton
public class UriRepository {
    private final UriDataSource mDataSource;

    @Inject
    public UriRepository(UriDataSource dataSource) {
        mDataSource = dataSource;
    }

    @CheckReturnValue
    public Single<List<UriModel>> findAll() {
        return mDataSource.findAll()
                .subscribeOn(Schedulers.io());
    }

    @CheckReturnValue
    public Single<Long> save(UriModel model) {
        return mDataSource.save(model)
                .subscribeOn(Schedulers.io());

    }

    @CheckReturnValue
    public Single<Integer> delete(UriModel model) {
        return mDataSource.delete(model)
                .subscribeOn(Schedulers.io());

    }
}
