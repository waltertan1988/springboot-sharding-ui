package org.apache.shardingsphere.ui.servcie.impl;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.shardingjdbc.orchestration.api.OrchestrationShardingDataSourceFactory;
import org.apache.shardingsphere.ui.common.domain.CenterConfig;
import org.apache.shardingsphere.ui.servcie.DataSourceCacheService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author tyx
 * @date 2022/1/23
 * 数据源缓存服务实现
 */
@Slf4j
@Service
public class DataSourceCacheServiceImpl implements DataSourceCacheService {

    private Map<String, DataSource> dataSourceCacheMap = Maps.newConcurrentMap();

    @Override
    @Async
    @Scheduled(cron = "0 * * * * *")
    public void clearCache() {
        log.info("clearing dataSourceCacheMap");
        dataSourceCacheMap.clear();
    }

    @Override
    public DataSource getDataSourceWithCache(CenterConfig centerConfig) throws SQLException {
        String cacheKey = getCacheKey(centerConfig);
        DataSource ds = dataSourceCacheMap.get(cacheKey);
        if(ds == null){
            ds = getDataSource(centerConfig);
            dataSourceCacheMap.put(cacheKey, ds);
        }
        return ds;
    }

    private DataSource getDataSource(CenterConfig activatedConfig) throws SQLException {
        Map<String, CenterConfiguration> configurationMap = createCenterConfigurationMap(activatedConfig);
        OrchestrationConfiguration orchestrationConfiguration = new OrchestrationConfiguration(configurationMap);
        return OrchestrationShardingDataSourceFactory.createDataSource(orchestrationConfiguration);
    }

    private Map<String, CenterConfiguration> createCenterConfigurationMap(CenterConfig activatedConfig) {
        Map<String, CenterConfiguration> instanceConfigurationMap = Maps.newHashMap();
        CenterConfiguration config = createCenterConfiguration(activatedConfig);
        instanceConfigurationMap.put(activatedConfig.getOrchestrationName(), config);
        return instanceConfigurationMap;
    }

    private CenterConfiguration createCenterConfiguration(CenterConfig activatedConfig) {
        CenterConfiguration result = new CenterConfiguration("zookeeper", activatedConfig.getProps());
        result.setServerLists(activatedConfig.getServerLists());
        result.setNamespace(activatedConfig.getNamespace());
        result.setOrchestrationType("registry_center,config_center");
        return result;
    }

    @Override
    public String getCacheKey(CenterConfig centerConfig) {
        return new StringBuilder()
                .append(centerConfig.getName()).append("|")
                .append(centerConfig.getInstanceType()).append("|")
                .append(centerConfig.getServerLists()).append("|")
                .append(centerConfig.getNamespace()).append("|")
                .append(centerConfig.getOrchestrationName())
                .toString();
    }
}
