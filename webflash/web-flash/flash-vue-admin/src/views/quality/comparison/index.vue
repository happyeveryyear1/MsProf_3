<template>
  <div class="dashboard-editor-container">

    <el-row :gutter="32">
      <el-col :xs="24" :sm="24" :lg="24">
        <div class="chart-wrapper">
          <raddar-chart :projs="projs" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="8">
      <el-col :xs="24" :sm="24" :lg="24">
        <el-table v-loading="listLoading" :data="list" border fit highlight-current-row style="width: 100%">
          <el-table-column min-width="200px" label="项目">
            <template slot-scope="{row}">
              <span>{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column min-width="100px" label="总体评价" sortable sort-by="['sscore']">
            <template slot-scope="{row}">
              <span><el-rate v-model="row.score" disabled show-score score-template="{value}" ></el-rate></span>
            </template>
          </el-table-column>
          <el-table-column
            v-for="(item,index) in headers"
            :key="index"
            :prop="item.prop"
            :label="item.col"
            sortable
          >
            <template slot-scope="scope">
              {{ scope.row[scope.column.property] }}
            </template>
          </el-table-column>
          <el-table-column v-if="vs" prop="sscore" min-width="100px" label="总体评价" >
            <template slot-scope="{row}">
              <span>{{ row.score }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script src="./comparison.js"></script>


<style lang="scss" scoped>
.dashboard-editor-container {
  padding: 32px;
  background-color: rgb(240, 242, 245);
  position: relative;

  .chart-wrapper {
    background: #fff;
    padding: 16px 16px 0;
    margin-bottom: 32px;
  }
}

@media (max-width:1024px) {
  .chart-wrapper {
    padding: 8px;
  }
}
</style>