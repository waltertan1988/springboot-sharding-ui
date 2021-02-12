package org.apache.shardingsphere.ui.common.dto;

import lombok.Data;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author tyx
 * @date 2021/2/12
 *
 */

@Data
public class CommandResp implements Serializable {

    private Boolean success;

    /** 分片执行单元信息 */
    private Collection<ExecutionUnit> executionUnits;
    /** 结果集列名 */
    private List<String> columnNameList;
    /** 结果集数据 */
    private List<List<String>> columnValueList;
}
