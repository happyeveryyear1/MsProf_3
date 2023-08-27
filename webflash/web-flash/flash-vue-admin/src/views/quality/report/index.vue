<template>
  <div class="dashboard-editor-container">

    <panel-group :proj_id="proj_id" :ver_tag="ver_tag" />

    <el-row>
      <!-- <el-col :xs="24" :sm="24" :lg="12"> -->
        <div class="chart-wrapper">
          <raddar-chart @handleSetLineChartData="handleSetLineChartData" :proj_id="proj_id" :ver_tag="ver_tag" />
        </div>
      <!-- </el-col> -->
      <!-- <el-col :xs="24" :sm="24" :lg="12"> -->
    </el-row>

    <el-row :gutter="8">
      <el-col :xs="24" :sm="24" :lg="24">
        <div class="chart-wrapper">
          <tree :proj_id="proj_id" :ver_tag="ver_tag" />
        </div>
      </el-col>
    </el-row>

    <el-row>
        <div class="chart-wrapper">
          <line-chart :qualityaspect="qualityaspect" :proj_id="proj_id" :ver_tag="ver_tag" />
        </div>
      <!-- </el-col> -->
    </el-row>
    
  </div>
</template>

<script src="./report.js"></script>

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