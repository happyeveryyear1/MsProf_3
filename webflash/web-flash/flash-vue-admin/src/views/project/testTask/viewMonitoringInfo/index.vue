<template>
  <div style="padding:20px">
    <div style="margin-bottom:10px">
      <el-button-group>
        <el-button size="mini" type="primary" :disabled="shrinkStatus" @click="reduceColSize">
          <i v-if="expandLevel==='1'" class="el-icon-d-arrow-left el-icon--left" />
          <i v-if="expandLevel==='0'" class="el-icon-arrow-left el-icon--left" />
          收起
        </el-button>
        <el-button size="mini" type="primary" :disabled="expandStatus" @click="enlargeColSize">展开
          <i v-if="expandLevel==='0'" class="el-icon-arrow-right el-icon--right" />
          <i v-if="expandLevel==='-1'" class="el-icon-d-arrow-right el-icon--right" /></el-button>
      </el-button-group>
      <el-button size="mini" type="primary" style="float:right" @click="openUrl">全屏显示</el-button>
    </div>

    <el-row type="flex" class="row-bg" justify="space-between">
      <el-col :span="ListColSize">
        <div id="container">
          <el-menu :default-active="activeIndex" class="el-menu-demo" mode="horizontal" style="height:45px;margin-bottom:10px">
            <el-menu-item index="1" style="height:100%;width:100%;text-align:center" @click="viewCases">用例</el-menu-item>
            <!-- <el-menu-item index="2" style="height:100%;width:50%;text-align:center" @click="viewPages" :disabled="pageTab">分页</el-menu-item> -->
          </el-menu>
          <el-table
            v-loading="listLoading"
            :data="tableData"
            stripe
            element-loading-text="Loading"
            fit
            border
            highlight-current-row
            height="645px"
            :show-header="false"
          >
            <el-table-column label="序号" width="50px" align="center">
              <template slot-scope="scope">
                {{ scope.$index+1 }}
              </template>
            </el-table-column>
            <el-table-column prop="List" :show-overflow-tooltip="true">
              <template slot-scope="scope">
                <span v-if="viewCase" style="margin-left:10px;cursor:pointer" @click="viewMonitoringInfo_case(scope.row.testcaseName,scope.row.executionTime,scope.row.costTime)"> {{ scope.row.testcaseName }} </span>
                <span v-else-if="scope.row.hasInfo===1" class="validUrl" @click="viewMonitoringInfo_page(scope.row.pageUrl)"> {{ scope.row.pageUrl }} </span>
                <span v-else style="margin-left:10px"><i class="el-icon-loading" /></span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="InfoColSize">
        <el-tabs v-model="activeName" type="border-card" style="height:700px" @tab-click="handleClick">
          <el-tab-pane label="har" name="har" :disabled="isPage">
            <iframe v-show="har" :src="har" width="100%" height="630px" />
          </el-tab-pane>
          <!-- <el-tab-pane label="pinpoint" name="pinpoint" :disabled="isPage">
            <iframe v-show="pinpoint" :src="pinpoint" width="100%" height="630px"></iframe>
          </el-tab-pane>
          <el-tab-pane label="sitespeed" name="sitespeed" :disabled="isCase">
            <iframe v-show="pageInfo" :src="pageInfo" width="100%" height="630px"></iframe>
          </el-tab-pane> -->
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script src="./viewMonitoringInfo.js"></script>

<style>
#container {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
  color: rgb(228, 226, 226);
}

.validUrl {
  margin-left:10px;
  cursor:pointer;
  text-decoration:none
}
</style>
