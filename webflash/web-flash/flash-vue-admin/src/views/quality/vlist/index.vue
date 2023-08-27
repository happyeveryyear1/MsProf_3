<template>
  <div class="app-container">
    <span class="textStyle">测试活动：</span>
    <el-select v-model="select_testactivityName" size="mini" filterable placeholder="测试活动" style="margin-bottom:20px;margin-right:20px">
      <el-option
        v-for="item in testactivityList"
        :key="item.testactivityName"
        :label="item.testactivityName"
        :value="item.testactivityName">
      </el-option>
    </el-select>
    <span class="textStyle">任务名：</span>
    <el-input v-model="search_taskName" style="margin-right:20px;width:150px" size="mini" placeholder="任务" clearable />
    <span class="textStyle">评估状态：</span>
    <el-select v-model="select_status" size="mini" filterable placeholder="质量评估状态"  style="margin-right:20px">
      <el-option label="已完成" value="已完成"></el-option>
      <el-option label="未评估" value="未评估"></el-option>
    </el-select>
    <el-button type="primary" size="mini" icon="el-icon-search"  @click.native="search">{{ $t('button.search') }}</el-button>
    <el-button type="primary" size="mini" icon="el-icon-refresh"  @click.native="reset">{{ $t('button.reset') }}</el-button>
    
    <el-table v-loading="listLoading" :data="list" border fit highlight-current-row style="width: 100%">
      <el-table-column label="序号" min-width="30px" align="center">
        <template slot-scope="scope">
          {{ scope.$index+(listQuery.page-1)*listQuery.limit+1 }}
        </template>
      </el-table-column>

      <el-table-column min-width="300px" label="测试任务">
        <template slot-scope="scope">
          <router-link :to="'/project/testTask/'+select_testactivityID+'?taskName='+scope.row.taskName" class="link-type">
            <span>{{ scope.row.taskName }}</span>
          </router-link>
        </template>
      </el-table-column>

      <el-table-column align="center" label="评估" width="120">
        <template slot-scope="scope">
          <el-button v-if="scope.row.evaluateStatus==='-1'" type="primary" size="mini"  @click="measure(scope.row.taskName,scope.$index)">
            执行评估
          </el-button>
          <el-button v-else-if="scope.row.evaluateStatus==='0'" type="primary" size="mini" :disabled="true">
            执行中
          </el-button> 
          <el-button v-else type="info" size="mini" :disabled="true">
            评估完成
          </el-button> 
        </template>
      </el-table-column>

      <el-table-column align="center" label="评估进度" width="120">
        <template slot-scope="scope">
           <el-progress :percentage=percentages[scope.row.taskName] ></el-progress>
        </template>
      </el-table-column>

      <el-table-column align="center" label="质量报告" width="120">
        <template slot-scope="scope">
          <router-link v-if="scope.row.evaluateStatus==='1'" :to="'/quality/report/'+proj_id+'_v_'+scope.row.taskName">
            <el-button type="primary" size="mini" icon="el-icon-edit">
              查看
            </el-button>
          </router-link>
          <span v-else> 报告未生成 </span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      background
      layout="total, sizes, prev, pager, next, jumper"
      :page-sizes="[10, 20, 50, 100,500]"
      :page-size="listQuery.limit"
      :total="total"
      style="margin-top:20px"
      @size-change="changeSize"
      @current-change="fetchPage"
      @prev-click="fetchPrev"
      @next-click="fetchNext"
    />

  </div>
</template>

<script src="./vlist.js"></script>

<style rel="stylesheet/scss" lang="scss" scoped>
    @import "src/styles/common.scss";
  .textStyle {
    font-size: 13px;
    color: gray;
  }
</style>