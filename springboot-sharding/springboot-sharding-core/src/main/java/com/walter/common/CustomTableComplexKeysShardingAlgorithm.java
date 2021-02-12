package com.walter.common;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author walter
 * @date 2021/2/10
 *
 * 分表规则：按sharding column对分表总数进行取余
 */
@Slf4j
public class CustomTableComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, ComplexKeysShardingValue<Long> complexKeysShardingValue) {
        log.info("[CustomTableComplexKeysShardingAlgorithm.doSharding] start: {}, {}", availableTargetNames, complexKeysShardingValue);

        List<String> resultList = Lists.newArrayList();

        complexKeysShardingValue.getColumnNameAndShardingValuesMap().values()
                .stream().flatMap(Collection::stream).forEach(shardingValue -> {
            long tableIndex = shardingValue % availableTargetNames.size();
            String tableName = new StringBuilder(complexKeysShardingValue.getLogicTableName()).append(tableIndex).toString();
            if(Objects.nonNull(availableTargetNames)
                    && availableTargetNames.stream().map(String::toLowerCase).anyMatch(Predicate.isEqual(tableName.toLowerCase()))){
                resultList.add(tableName);
            }else{
                throw new RuntimeException("No sharding result found for sharding value: " + shardingValue);
            }
        });

        return resultList;
    }
}
