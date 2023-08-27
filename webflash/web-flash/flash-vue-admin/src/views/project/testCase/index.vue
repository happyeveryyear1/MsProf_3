<template>
  <div class="app-container">
    <el-table
      v-loading="listLoading"
      :data="tableData"
      row-key="rowId"
      element-loading-text="Loading"
      fit
      default-expand-all
      :row-class-name="tableRowClassName"
      :header-cell-style="{background:'#e5e7e4',color:'#727471'}"
      :tree-props="{children: 'children'}"
    >
      <el-table-column label="" min-width="10px" />
      <el-table-column label="测试用例" prop="testcaseName" header-align="center">
        <template slot-scope="scope">
          {{ scope.row.testcaseName }}
        </template>
      </el-table-column>
      <el-table-column label="执行完成时间" prop="executionTime" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.role === 'parent'">
            <i class="el-icon-time" />
            {{ scope.row.executionTime.split(' ')[0] }}
          </span>
          <span v-else>
            {{ scope.row.executionTime }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="执行结果" prop="executionResult" align="center">
        <template slot-scope="scope">
          <span>
            <i class="el-icon-check" style="color:green" />
            {{ scope.row.executionResult.split(',')[0] }} ，
            <i class="el-icon-close" style="color:red" />
            {{ scope.row.executionResult.split(',')[1] }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="耗时" prop="costTime" align="center">
        <template slot-scope="scope">
            {{ scope.row.costTime }} ms
        </template>
      </el-table-column>
      <el-table-column label="详细信息" prop="detailedInfo" align="center">
        <template slot-scope="scope">
          <div :class="detailedInfoClass(scope.row.role)">
            <el-tooltip class="item" effect="dark" content="测试报告" placement="top-start">
              <i class="el-icon-s-order" style="margin-right:20px;cursor:pointer" @click="viewReport(scope.row.logId)" />
            </el-tooltip>
            <el-tooltip class="item" effect="dark" content="错误截图" placement="top-start">
              <i v-if="scope.row.executionResult==='0,1'" class="el-icon-camera-solid" style="margin-right:20px;cursor:pointer" @click="viewScreenShot(scope.row.picPath) " />
              <i v-else class="el-icon-camera-solid" style="margin-right:20px;color:rgb(213, 216, 213)" />
            </el-tooltip>
            <el-tooltip class="item" effect="dark" content="录制过程" placement="top-start">
              <i class="el-icon-video-play" style="cursor:pointer"  @click="viewVideo(scope.row.videoPath)" />
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      :title="formTitle_report"
      :visible.sync="formVisible_report"
      width="100%"
      @close="closeDialog"
    >
     <pre id="log" style="width:100%"></pre>
    </el-dialog>

    <el-dialog
      :title="formTitle_screenShot"
      :visible.sync="formVisible_screenShot"
      width="70%"
      @close="closeDialog"
    >
      <img :src="screenShotSrc" width=100% height=100% />
    </el-dialog>

    <el-dialog
      :title="formTitle_video"
      :visible.sync="formVisible_video"
      width="70%"
      @close="closeDialog"
    >
      <video class="video" id="video" :src="videoSrc" width=100% controls>
      </video>
    </el-dialog>

  </div>
</template>

<script src="./testCase.js"></script>

<style rel="stylesheet/scss" lang="scss">
    @import "src/styles/common.scss";
    .el-table .parent {
    background: #f0f9eb;
    font-weight: bold;
    font-family:'Lucida Sans', 'Lucida Sans Regular', 'Lucida Grande', 'Lucida Sans Unicode', Geneva, Verdana, sans-serif
  }
  
    .parentDetailedInfo {
      display: none;
    }
</style>

