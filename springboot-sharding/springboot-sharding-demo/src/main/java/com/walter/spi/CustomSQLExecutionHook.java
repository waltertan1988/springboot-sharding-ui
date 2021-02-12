package com.walter.spi;

import com.walter.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.spi.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.underlying.executor.hook.SQLExecutionHook;

import java.util.List;
import java.util.Map;

/**
 * @author walter
 * @date 2021/2/12
 *
 */
@Slf4j
public class CustomSQLExecutionHook implements SQLExecutionHook {

    private ThreadLocal<Long> elapseTime = new ThreadLocal<>();

    @Override
    public void start(String dataSourceName, String sql, List<Object> parameters, DataSourceMetaData dataSourceMetaData, boolean isTrunkThread, Map<String, Object> shardingExecuteDataMap) {
        log.info("[start] : {}, {}, {}", dataSourceName, sql, JsonUtil.toJson(parameters));
        elapseTime.set(System.currentTimeMillis());
    }

    @Override
    public void finishSuccess() {
        log.info("[finishSuccess] cost ms: {}", System.currentTimeMillis() - elapseTime.get());
        elapseTime.remove();
    }

    @Override
    public void finishFailure(Exception cause) {
        log.error("[finishFailure] cost ms: {}, {}", System.currentTimeMillis() - elapseTime.get(), cause);
        elapseTime.remove();
    }
}
