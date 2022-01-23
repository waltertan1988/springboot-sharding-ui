<!--
  - Licensed to the Apache Software Foundation (ASF) under one or more
  - contributor license agreements.  See the NOTICE file distributed with
  - this work for additional information regarding copyright ownership.
  - The ASF licenses this file to You under the Apache License, Version 2.0
  - (the "License"); you may not use this file except in compliance with
  - the License.  You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <el-row class="box-card">
    <el-input :rows="10" v-model="sqlString" type="textarea" class="edit-text"/>
    <div class="btn-group">
      <el-button
        class="btn-plus"
        type="primary"
        icon="el-icon-caret-right"
        @click="execute"
      >{{ $t("sqlCommand.btnTxt") }}</el-button>
    </div>

    <h4>命令：{{ lastSqlStr }}</h4>

    <div class="table-wrap">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column
          v-for="(item, index) in column"
          :key="index"
          :prop="item.prop"
          :label="item.label"
        />
      </el-table>
      <div class="pagination">
        <el-pagination
          :total="total"
          :current-page="currentPage"
          background
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </el-row>
</template>
<script>
import API from '../api'
import clone from 'lodash/clone'
export default {
  name: 'SqlCommand',
  data() {
    return {
      sqlString: '',
      lastSqlStr: '',
      column: [],
      tableData: [],
      cloneTableData: [],
      currentPage: 1,
      pageSize: 10,
      total: null
    }
  },
  methods: {
    handleCurrentChange(val) {
      const data = clone(this.cloneTableData)
      this.tableData = data.splice(val - 1, this.pageSize)
    },
    execute() {
      this.lastSqlStr = this.sqlString

      API.execute({ command: this.sqlString }).then(res => {
        // 处理表头
        const column = []
        res.model.columnNameList.forEach((e, i) => column.push({ label: e, prop: 'column' + i }))
        this.column = column

        // 处理数据
        const rows = []
        res.model.columnValueList.forEach((r) => {
          const row = {}
          r.forEach((e, i) => { row['column' + i] = e })
          rows.push(row)
        })

        this.total = rows.length
        this.cloneTableData = clone(rows)
        this.tableData = rows.splice(0, this.pageSize)
      })
    }
  }
}
</script>
<style lang='scss' scoped>
  .btn-group {
    margin-bottom: 20px;
  }
  .pagination {
    float: right;
    margin: 10px -10px 10px 0;
  }
</style>
