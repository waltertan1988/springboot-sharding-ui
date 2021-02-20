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
    <el-input :rows="10" v-model="textarea" type="textarea" class="edit-text"/>
    <div class="btn-group">
      <el-button
        class="btn-plus"
        type="primary"
        icon="el-icon-caret-right"
        @click="add"
      >{{ $t("sqlCommand.btnTxt") }}</el-button>
    </div>
    <div class="table-wrap">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column
          v-for="(item, index) in column"
          :key="index"
          :prop="item.prop"
          :label="item.label"
          :width="item.width"
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
import { mapActions } from 'vuex'
import clone from 'lodash/clone'
import API from '../api'
export default {
  name: 'SqlCommand',
  data() {
    return {
      regustDialogVisible: false,
      editDialogVisible: false,
      column: [
        {
          label: this.$t('configCenter').configDialog.name,
          prop: 'name'
        },
        {
          label: this.$t('configCenter').configDialog.centerType,
          prop: 'instanceType'
        },
        {
          label: this.$t('configCenter').configDialog.address,
          prop: 'serverLists'
        },
        {
          label: this.$t('configCenter').configDialog.namespaces,
          prop: 'namespace'
        },
        {
          label: this.$t('configCenter').configDialog.orchestrationName,
          prop: 'orchestrationName'
        }
      ],
      tableData: [],
      cloneTableData: [],
      currentPage: 1,
      pageSize: 10,
      total: null
    }
  },
  created() {
    this.getRegCenter()
  },
  methods: {
    ...mapActions(['setRegCenterActivated']),
    handleCurrentChange(val) {
      const data = clone(this.cloneTableData)
      this.tableData = data.splice(val - 1, this.pageSize)
    },
    getRegCenter() {
      API.getConfigCenter().then(res => {
        const data = res.model
        this.total = data.length
        this.cloneTableData = clone(res.model)
        this.tableData = data.splice(0, this.pageSize)
      })
    },
    add() {
      alert("Execute")
      this.regustDialogVisible = true
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
