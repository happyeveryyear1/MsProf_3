<template>
  <div class="app-container">
    <!-- 所有服务版本 -->
    <div>
      <el-dialog :title="formTitle" :visible.sync="formVisible" width="70%">
        <el-table
          v-if="versionList.length"
          v-loading="listLoading"
          element-loading-text="拼命加载中"
          element-loading-spinner="el-icon-loading"
          :data="versionList"
          border
          fit
          highlight-current-row
          style="margin-top: 15px"
          ref="multipleTable"
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

    <!-- 特定版本，针对于ts-order-service -->
    <div>
      <el-dialog
        :title="specFormTitle"
        :visible.sync="specFormVisible"
        width="70%"
      >
        <el-table
          v-if="specVersion != -1"
          v-loading="specVersionLoading"
          element-loading-text="拼命加载中"
          element-loading-spinner="el-icon-loading"
          :data="specVersion"
          border
          fit
          highlight-current-row
          style="margin-top: 15px"
          ref="multipleTable"
        >
          <el-table-column label="名称" min-width="150px" align="center">
            <template slot-scope="scope"> ts-order-service </template>
          </el-table-column>
          <el-table-column
            label="版本"
            min-width="50px"
            :show-overflow-tooltip="true"
            align="center"
          >
            <template slot-scope="scope">
              {{ scope.row["ts-order-service"] }}
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </div>

    <!-- 上半部分界面 -->
    <div
      style="
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        padding-bottom: 20px;
        padding-right: 20px;
      "
    >
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
            v-if="list.length"
            v-loading="listLoading"
            element-loading-text="拼命加载中"
            element-loading-spinner="el-icon-loading"
            :data="list"
            border
            fit
            highlight-current-row
            style="margin-top: 15px"
            ref="multipleTable"
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
                {{ scope.row }}
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
      <!-- 第二行 瓶颈分析结果 -->
      <el-row
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
          @click.native="analyse"
          :loading="isAnalyse"
          >瓶颈分析</el-button
        >
      </el-row>
    </div>

    <!-- 下半部分界面 -->
    <div
      style="box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); padding-bottom: 20px"
    >
      <el-row
        type="flex"
        class="row-bg"
        justify="space-between"
        style="margin-top: 20px"
      >
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
            <div class="card-panel-text">服务动态更新</div>
          </el-card>
        </el-col>
      </el-row>
      <el-row
        type="flex"
        class="row-bg"
        justify="center"
        style="margin-top: 50px"
      >
        <div>
          <el-button
            type="primary"
            icon="el-icon-refresh"
            style="margin-left: 20px"
            @click.native="getCurrentVersionFun"
            :loading="versionIsChange"
            >当前版本</el-button
          >
        </div>
        <div>
          <el-button
            type="primary"
            icon="el-icon-refresh"
            style="margin-left: 20px"
            @click.native="getSpecVersionFun"
            :loading="specVersionIsChange"
            >Order版本</el-button
          >
        </div>
        <div>
          <el-input
            v-model="yamlPath"
            style="width: 600px; margin-right: 50px; margin-left: 150px"
            placeholder="新版本yaml地址"
            clearable
          />
          <el-button
            type="primary"
            icon="el-icon-refresh"
            style="margin-left: 20px"
            @click.native="changeVersion"
            :loading="isChange"
            >版本切换</el-button
          >
        </div>
      </el-row>
    </div>
  </div>
</template>

<script src="./problemAnalysis.js"></script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import "src/styles/common.scss";
</style>

