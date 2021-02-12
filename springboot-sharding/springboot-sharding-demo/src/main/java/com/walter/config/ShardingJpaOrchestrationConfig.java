package com.walter.config;

import com.google.common.collect.Maps;
import com.walter.common.CustomDbComplexKeysShardingAlgorithm;
import com.walter.common.CustomTableComplexKeysShardingAlgorithm;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.shardingjdbc.orchestration.api.OrchestrationShardingDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Configuration
@ConditionalOnClass(name = "org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration")
public class ShardingJpaOrchestrationConfig {
    @Autowired
    private ShardingDataSourceProperties shardingDataSourceProperties;

    @Bean("shardingDataSource")
    public DataSource shardingDataSource() throws SQLException {

        log.info("Configure ShardingSphere with zookeeper");

        Map<String, DataSource> dataSourceMap = dataSourceMap();

        ShardingRuleConfiguration shardingRuleConfiguration = shardingRuleConfiguration(
                shardingDataSourceProperties.getName(),
                shardingDataSourceProperties.getHosts().size(),
                shardingDataSourceProperties.getTableCount());

        Properties props = properties();

        // 首次加载，需要设置dataSourceMap, shardingRuleConfiguration, props把配置初始化到zookeeper中，后续就可以将它们设为null了
        return OrchestrationShardingDataSourceFactory.createDataSource(null, null,
                null, new OrchestrationConfiguration(createCenterConfigurationMap()));
    }

    private Properties properties(){
        Properties props = new Properties();
        props.setProperty("sql.show", "true");
        return props;
    }

    private Map<String, DataSource> dataSourceMap(){
        final int DS_COUNT = shardingDataSourceProperties.getHosts().size();

        Map<String, DataSource> dataSourceMap = new HashMap<>(DS_COUNT);
        String dsName = shardingDataSourceProperties.getName();
        // 配置真实数据源
        for (int i = 0; i < DS_COUNT; i++) {
            String dsHost = shardingDataSourceProperties.getHosts().get(i);
            dataSourceMap.put(dsName + i, createSubDataSource(dsHost, i));
        }
        return dataSourceMap;
    }

    private DataSource createSubDataSource(String host, int dsNum){
        HikariConfig jdbcConfig = new HikariConfig();
        String poolName = String.format("HikariPool-%s-%s", shardingDataSourceProperties.getName(), dsNum);
        jdbcConfig.setPoolName(poolName);
        jdbcConfig.setDriverClassName(shardingDataSourceProperties.getDriverClassName());
        String jdbcUrl = shardingDataSourceProperties.getUrlPattern()
                .replace("{HOST}", host)
                .replace("{DS_NUM}", String.valueOf(dsNum));
        jdbcConfig.setJdbcUrl(jdbcUrl);
        jdbcConfig.setUsername(shardingDataSourceProperties.getUsername());
        jdbcConfig.setPassword(shardingDataSourceProperties.getPassword());
        return new HikariDataSource(jdbcConfig);
    }

    private ShardingRuleConfiguration shardingRuleConfiguration(String dsName, int DS_COUNT, int TABLE_COUNT){
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        // 配置默认的分片规则
        shardingRuleConfig.setDefaultDataSourceName(dsName + 0);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(newDefaultDatabaseShardingStrategyConfiguration("user_id"));
        // 设置绑定表
        shardingRuleConfig.setBindingTableGroups(Arrays.asList("t_order,t_order_item"));

        // 配置Order的分片规则
        TableRuleConfiguration orderTableRuleConfig = newTableRuleConfiguration(dsName, DS_COUNT, "user_id",
                "t_order", TABLE_COUNT, "order_id", true);
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);

        // 配置OrderItem的分片规则
        TableRuleConfiguration orderItemTableRuleConfig = newTableRuleConfiguration(dsName, DS_COUNT, "user_id",
                "t_order_item", TABLE_COUNT, "order_id", true);
        shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);

        return shardingRuleConfig;
    }

    private Map<String, CenterConfiguration> createCenterConfigurationMap() {
        Map<String, CenterConfiguration> instanceConfigurationMap = Maps.newHashMap();
        CenterConfiguration config = createCenterConfiguration();
        instanceConfigurationMap.put("orchestration-sharding-data-source", config);
        return instanceConfigurationMap;
    }

    private CenterConfiguration createCenterConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("overwrite", "false");
        CenterConfiguration result = new CenterConfiguration("zookeeper", properties);
        result.setServerLists("localhost:2181");
        result.setNamespace("sharding-sphere-orchestration");
        result.setOrchestrationType("registry_center,config_center");
        return result;
    }

    private ShardingStrategyConfiguration newDefaultDatabaseShardingStrategyConfiguration(String dsShardingCoulumn){
        // 配置默认分库策略
        return new ComplexShardingStrategyConfiguration(dsShardingCoulumn,
                new CustomDbComplexKeysShardingAlgorithm(shardingDataSourceProperties.getName()));
    }

    private TableRuleConfiguration newTableRuleConfiguration(String dsName, int dsCount, String dsShardingCoulumn, String tableName, int tableCount, String tableShardingColumn, boolean dsDefault){
        // 配置表规则（ds${0..1}.t_order${0..1}）
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration(tableName, String.format("%s%s{0..%s}.%s%s{0..%s}", dsName, "$", dsCount-1, tableName, "$", tableCount-1));
        if(!dsDefault){
            // 配置分库策略（ds${user_id % 2}），不配置则使用默认的分库策略
            String dsShardingExpression = String.format(SHARDING_STRATEGY_EXPRESSION, dsName, "$", dsShardingCoulumn, dsCount);
            tableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration(dsShardingCoulumn, dsShardingExpression));
        }

        // 使用自定义复合表分片策略
        tableRuleConfig.setTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration(tableShardingColumn,
                new CustomTableComplexKeysShardingAlgorithm()));

        return tableRuleConfig;
    }

    private final String SHARDING_STRATEGY_EXPRESSION = "%s%s{%s %% %s}";

}
