package jp.tinyport.pollinghttp.core.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Index;
import com.github.gfx.android.orma.annotation.Table;

@Table(constraints = "PRIMARY KEY (uri, method) ON CONFLICT REPLACE",
        indexes = @Index({"uri", "method"}))
public class UriModel {
    @Column(helpers = Column.Helpers.CONDITIONS)
    public String uri;

    @Column(helpers = Column.Helpers.CONDITIONS)
    public String method;

    @Column
    public String body;
}
