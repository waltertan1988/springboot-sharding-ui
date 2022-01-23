package org.apache.shardingsphere.ui.servcie;

import org.apache.shardingsphere.ui.common.domain.CenterConfig;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 数据源缓存服务
 */
public interface DataSourceCacheService {

    /**
     * 删除缓存
     */
    void clearCache();

    /**
     * 缓存获取数据源
     * @param centerConfig
     * @return
     * @throws SQLException
     */
    DataSource getDataSourceWithCache(CenterConfig centerConfig) throws SQLException;

    /**
     * 返回配置中心在缓存中的key
     * @param centerConfig
     * @return
     */
    String getCacheKey(CenterConfig centerConfig);
}
