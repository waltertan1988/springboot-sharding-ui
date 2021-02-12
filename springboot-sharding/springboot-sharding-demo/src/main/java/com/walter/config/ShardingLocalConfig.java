package com.walter.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Deprecated
@Slf4j
@Configuration
@ConditionalOnMissingClass("org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration")
public class ShardingLocalConfig {
    @Autowired
    private ShardingDataSourceProperties shardingDataSourceProperties;

    @Bean
    public DataSource dataSource() {
        log.info("Configure ShardingSphere with local yml file");

        final int DS_COUNT = shardingDataSourceProperties.getHosts().size();
        final int TABLE_COUNT = shardingDataSourceProperties.getTableCount();

        Map<String, DataSource> dataSourceMap = new HashMap<>(DS_COUNT);
        String dsName = shardingDataSourceProperties.getName();
        // 配置真实数据源
        for (int i = 0; i < DS_COUNT; i++) {
            String dsHost = shardingDataSourceProperties.getHosts().get(i);
            dataSourceMap.put(dsName + i, createSubDataSource(dsHost, i));
        }

        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        // 配置默认的分片规则
        shardingRuleConfig.setDefaultDataSourceName(dsName + 0);
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(newDefaultDatabaseShardingStrategyConfiguration(dsName, DS_COUNT, "user_id"));
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

        // 设置额外属性
        Properties props = new Properties();
        props.setProperty("sql.show", "true");
        try {
            // 获取数据源对象
            return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, props);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * 配置表规则
     * @param dsName
     * @param dsCount
     * @param dsShardingCoulumn
     * @param tableName
     * @param tableCount
     * @param tableShardingColumn
     * @param dsDefault
     * @return
     */
    private TableRuleConfiguration newTableRuleConfiguration(String dsName, int dsCount, String dsShardingCoulumn, String tableName, int tableCount, String tableShardingColumn, boolean dsDefault){
        // 配置表规则（ds${0..1}.t_order${0..1}）
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration(tableName, String.format("%s%s{0..%s}.%s%s{0..%s}", dsName, "$", dsCount-1, tableName, "$", tableCount-1));
        if(!dsDefault){
            // 配置分库策略（ds${user_id % 2}），不配置则使用默认的分库策略
            String dsShardingExpression = String.format(SHARDING_STRATEGY_EXPRESSION, dsName, "$", dsShardingCoulumn, dsCount);
            tableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration(dsShardingCoulumn, dsShardingExpression));
        }
        // 配置分表策略（t_order${order_id % 2}）
        String tableShardingExpression = String.format(SHARDING_STRATEGY_EXPRESSION, tableName, "$", tableShardingColumn, tableCount);
        tableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration(tableShardingColumn, tableShardingExpression));

        return tableRuleConfig;
    }

    /**
     * 默认分库规则
     * @param dsName
     * @param dsCount
     * @param dsShardingCoulumn
     * @return
     */
    private ShardingStrategyConfiguration newDefaultDatabaseShardingStrategyConfiguration(String dsName, int dsCount, String dsShardingCoulumn){
        // 配置分库策略（ds${user_id % 2}）
        String dsShardingExpression = String.format(SHARDING_STRATEGY_EXPRESSION, dsName, "$", dsShardingCoulumn, dsCount);
        return new InlineShardingStrategyConfiguration(dsShardingCoulumn, dsShardingExpression);
    }

    private final String SHARDING_STRATEGY_EXPRESSION = "%s%s{%s %% %s}";
}
