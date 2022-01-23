package org.apache.shardingsphere.ui.web.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.ShardingResultSet;
import org.apache.shardingsphere.shardingjdbc.orchestration.api.OrchestrationShardingDataSourceFactory;
import org.apache.shardingsphere.ui.common.constant.OrchestrationType;
import org.apache.shardingsphere.ui.common.domain.CenterConfig;
import org.apache.shardingsphere.ui.common.dto.CommandResp;
import org.apache.shardingsphere.ui.common.dto.CommandReq;
import org.apache.shardingsphere.ui.servcie.CenterConfigService;
import org.apache.shardingsphere.ui.web.response.ResponseResult;
import org.apache.shardingsphere.ui.web.response.ResponseResultUtil;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author tyx
 * @date 2021/2/12
 * 执行SQL命令
 */
@Slf4j
@RestController
@RequestMapping("/api/sql-command")
public class ShardingExecutionController {

    @Autowired
    private CenterConfigService centerConfigService;

    @PostMapping(value = "")
    public ResponseResult<CommandResp> executeCommand(@RequestBody CommandReq commandReq) throws SQLException {
        CenterConfig activatedConfig = centerConfigService.loadActivated(OrchestrationType.CONFIG_CENTER.getValue()).orElse(null);
        DataSource dataSource = getDataSource(activatedConfig);
        return ResponseResultUtil.build(execute(dataSource, commandReq.getCommand()));
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

    public CommandResp execute(DataSource dataSource, String sql) throws SQLException {
        CommandResp commandResp = new CommandResp();
        commandResp.setSuccess(false);
        commandResp.setColumnNameList(Lists.newArrayList());
        commandResp.setColumnValueList(Lists.newArrayList());

        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                // 执行单元信息集合
                commandResp.setExecutionUnits(getExecutionUnitSet(rs));

                // 结果集列名
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    commandResp.getColumnNameList().add(rs.getMetaData().getColumnName(i));
                }

                // 结果集数据
                while(rs.next()) {
                    List<String> columnValueList = Lists.newArrayList();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        columnValueList.add(rs.getObject(i).toString());
                    }
                    commandResp.getColumnValueList().add(columnValueList);
                }
            }
            commandResp.setSuccess(true);
        }
        return commandResp;
    }

    private Collection<ExecutionUnit> getExecutionUnitSet(ResultSet rs){
        if(rs instanceof ShardingResultSet){
            return ((ShardingResultSet)rs).getExecutionContext().getExecutionUnits();
        }
        return Lists.newArrayList();
    }
}
