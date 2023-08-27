<template>
  <div class="app-container">
    <!-- 所有服务版本 -->
    <div>
      <el-dialog :title="formTitle" :visible.sync="formVisible" width="70%">
        <el-table
          v-if="versionList.length"
          ref="multipleTable"
          v-loading="listLoading"
          element-loading-text="拼命加载中"
          element-loading-spinner="el-icon-loading"
          :data="versionList"
          border
          fit
          highlight-current-row
          style="margin-top: 15px"
        >
          <el-table-column label="名称" min-width="150px" align="center">
            <template slot-scope="scope">
              <!-- {{ scope.$index+(listQuery.page-1)*listQuery.limit+1 }} -->
              {{ scope.row.a }}
            </template>
          </el-table-column>
          <el-table-column
            label="版本"
            min-width="50px"
            :show-overflow-tooltip="true"
            align="center"
          >
            <template slot-scope="scope">
              {{ scope.row.b }}
            </template>
          </el-table-column>
          <el-table-column label="名称" min-width="150px" align="center">
            <template slot-scope="scope">
              <!-- {{ scope.$index+(listQuery.page-1)*listQuery.limit+1 }} -->
              {{ scope.row.c }}
            </template>
          </el-table-column>
          <el-table-column
            label="版本"
            min-width="50px"
            :show-overflow-tooltip="true"
            align="center"
          >
            <template slot-scope="scope">
              {{ scope.row.d }}
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </div>

    <div>
      <el-dialog :title="analyzeIng" :visible.sync="analysingVisible" width="70%" />
    </div>

    <!-- 瓶颈及版本 -->
    <div>
      <el-dialog :title="listTitle" :visible.sync="listVisible" width="70%">
        <el-table
          v-if="list.length"
          ref="multipleTable"
          v-loading="listLoading"
          element-loading-text="拼命加载中"
          element-loading-spinner="el-icon-loading"
          :data="list"
          border
          fit
          highlight-current-row
          style="margin-top: 15px"
        >
          <el-table-column label="排序" min-width="30px" align="center">
            <template slot-scope="scope">
              <!-- {{ scope.$index+(listQuery.page-1)*listQuery.limit+1 }} -->
              {{ scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column
            label="名称"
            min-width="200px"
            :show-overflow-tooltip="true"
            align="center"
          >
            <template slot-scope="scope">
              {{ scope.row.name }}
            </template>
          </el-table-column>
          <el-table-column
            label="版本"
            max-width="100px"
            :show-overflow-tooltip="true"
            align="center"
          >
            <template slot-scope="scope">
              {{ scope.row.version }}
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </div>

    <!-- 特定版本，针对于ts-order-service -->
    <div>
      <el-dialog
        :title="specFormTitle"
        :visible.sync="specFormVisible"
        width="70%"
      >
        <el-table
          v-if="specVersion != -1"
          ref="multipleTable"
          v-loading="specVersionLoading"
          element-loading-text="拼命加载中"
          element-loading-spinner="el-icon-loading"
          :data="specVersion"
          border
          fit
          highlight-current-row
          style="margin-top: 15px"
        >
          <!-- <el-table-column label="名称" min-width="150px" align="center">
            <template slot-scope="scope"> ts-order-service </template>
          </el-table-column> -->
          <el-table-column
            label="名称"
            min-width="200px"
            :show-overflow-tooltip="true"
            align="center"
          >
            <template slot-scope="scope">
              {{ scope.row.svc_name }}
            </template>
          </el-table-column>
          <el-table-column
            label="版本"
            min-width="100px"
            :show-overflow-tooltip="true"
            align="center"
          >
            <template slot-scope="scope">
              {{ scope.row.svc_version }}
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </div>

    <!-- 上半部分界面 -->
    <el-col :gutter="10" type="flex">
      <el-row :span="10">
        <el-card style="margin-bottom:20px">
          <!-- 第一行 瓶颈分析 | 列表-->
          <el-row type="flex" class="row-bg" justify="space-between">
            <el-col :span="4">
              <el-card
                class="box-card"
                style="
              margin-top: 15px;
              margin-left: 20px;
              text-align: center;
              background: #dcdfe6;
            "
              >
                <div class="card-panel-text">性能瓶颈分析</div>
              </el-card>
            </el-col>
            <el-col :span="19">
              <el-table
                v-loading="listLoading"
                :data="taskList"
                element-loading-text="Loading"
                border
                fit
                highlight-current-row
              >
                <el-table-column label="序号" min-width="50px" align="center">
                  <template slot-scope="scope">
                    {{ scope.$index +1 }}
                  </template>
                </el-table-column>
                <el-table-column
                  label="测试活动名"
                  min-width="150px"
                  header-align="center"
                  :show-overflow-tooltip="true"
                >
                  <template slot-scope="scope">
                    {{ scope.row.testactivityName }}
                  </template>
                </el-table-column>
                <el-table-column
                  label="任务名"
                  min-width="150px"
                  header-align="center"
                  :show-overflow-tooltip="true"
                >
                  <template slot-scope="scope">
                    <!-- {{ scope.row.projectName }} -->
                    {{ scope.row.taskName }}
                  </template>
                </el-table-column>
                <el-table-column
                  label="创建时间"
                  min-width="200px"
                  header-align="center"
                  :show-overflow-tooltip="true"
                >
                  <template slot-scope="scope">
                    {{ scope.row.createTime }}
                  </template>
                </el-table-column>
                <el-table-column
                  label="瓶颈分析结果"
                  min-width="100px"
                  header-align="center"
                  :show-overflow-tooltip="true"
                >
                  <template slot-scope="scope" >
                    <el-button size="mini" style = "margin-left: 20px;  text-align: center; " :loading="isAnalysing(scope.$index)" @click="clickCheckorAnalyze(scope.$index)">{{ getName(scope.$index) }}</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
          <!-- 第二行 瓶颈分析结果 -->
          <!-- <el-row
            type="flex"
            class="row-bg"
            justify="center"
            style="margin-top: 50px"
          >
            <el-input
              v-model="analyseResult"
              style="width: 430px; margin-right: 50px"
              placeholder="瓶颈分析结果"
              clearable
              :disabled="true"
            />
            <el-button
              type="primary"
              icon="el-icon-search"
              style="margin-left: 20px"
              :loading="isAnalyse"
              @click.native="analyse"
            >瓶颈分析</el-button>
          </el-row> -->
        </el-card>
      </el-row>
      <!-- </el-row>
    <el-row style="span=50"> -->
      <!-- 下半部分界面 -->
      <el-row :span="10">
        <el-card style="margin-bottom:20px">
          <!-- <el-row
            type="flex"
            class="row-bg"
            justify="space-between"
            style="margin-top: 20px"
          > -->
          <el-col :span="4">
            <el-card
              class="box-card"
              style="
              margin-top: 15px;
              margin-left: 20px;
              margin-bottom: 20px;
              text-align: center;
              background: #dcdfe6;
            "
            >
              <div class="card-panel-text">服务动态更新</div>
            </el-card>
          </el-col>
          <div
            style="
              margin-top: 25px;
              margin-left: 0px;
              margin-bottom: 20px;

            "
          >
            <el-input
              v-model="yamlPath"
              style="width: 600px; margin-right: 50px; margin-left: 70px"
              placeholder="新版本yaml地址"
              clearable
            />
            <el-button
              type="primary"
              icon="el-icon-refresh"
              style="margin-left: 5px"
              :loading="isChange"
              @click.native="changeVersion"
            >版本切换</el-button>
            <el-button
              type="primary"
              icon="el-icon-refresh"
              style="margin-left: 30px"
              :disabled="updateSvcVersionDisable"
              :loading="updateSvcVersionIsChange"
              @click.native="getCurrentUpdateVersionFun"
            >当前更新版本</el-button>
            <el-button
              type="primary"
              icon="el-icon-refresh"
              style="margin-left: 30px"
              :loading="versionIsChange"
              @click.native="getCurrentVersionFun"
            >当前所有版本</el-button>
          </div>
          <!-- </el-row> -->
        </el-card>
      </el-row>
    </el-col>
  </div></template>

<script src="./problemAnalysis.js"></script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import "src/styles/common.scss";
</style>

