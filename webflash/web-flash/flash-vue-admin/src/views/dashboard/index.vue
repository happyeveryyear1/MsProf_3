<template>
  <div v-if="!loading" class="dashboard-container">
    <div class="dashboard-text">欢迎光临：{{ name }} </div>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="box-card">
          <el-col :span="12">
            <svg-icon icon-class="drag" />
          </el-col>
          <el-col :span="12">
            <div class="card-panel-text">系统个数</div>
            <div class="card-panel-num">5</div>
          </el-col>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <el-col :span="12">
            <svg-icon icon-class="component" />
          </el-col>
          <el-col :span="12">
            <div class="card-panel-text">项目总数</div>
            <el-popover
              placement="bottom"
              trigger="click">
              <v-chart :options="pieData" style="width:450px;height:200px" />
              <div class="card-panel-num" slot="reference" style="cursor:pointer" @click="viewProjectNum">{{ projectNum }}</div>
            </el-popover>   
          </el-col>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <el-col :span="12">
            <svg-icon icon-class="list" />
          </el-col>
          <el-col :span="12">
            <div class="card-panel-text">测试活动总数</div>
            <el-popover
              placement="bottom"
              trigger="click">
              <v-chart :options="pieData" style="width:450px;height:200px" />
              <div class="card-panel-num" slot="reference" style="cursor:pointer" @click="viewActivityNum">{{ activityNum }}</div>
            </el-popover>   
          </el-col>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <el-col :span="12">
            <svg-icon icon-class="skill" />
          </el-col>
          <el-col :span="12">
            <div class="card-panel-text">任务总数</div>
            <el-popover
              placement="bottom"
              trigger="click">
              <v-chart :options="pieData" style="width:450px;height:200px" />
              <div class="card-panel-num" slot="reference" style="cursor:pointer" @click="viewTaskNum">{{ taskNum }}</div>
            </el-popover>   
          </el-col>
        </el-card>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="24">
        <div class="chart section">
          <v-chart :options="lineData" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <div class="chart section">
          <el-select v-model="barData_project_system" placeholder="项目" size="mini" style="margin-left:1000px" @change="pro_changesys">
            <el-option key="全部" label="全部" value="全部" />
            <el-option key="营销系统" label="营销系统" value="营销系统" />
            <el-option key="生产系统" label="生产系统" value="生产系统" />
            <el-option key="资产系统" label="资产系统" value="资产系统" />
            <el-option key="财务系统" label="财务系统" value="财务系统" />
            <el-option key="人资系统" label="人资系统" value="人资系统" />
          </el-select>
          <v-chart :options="barData_project"></v-chart>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <div class="chart section">
          <el-select v-model="mixData_system" placeholder="项目" size="mini" style="margin-left:1000px" @change="task_changesys">
            <el-option key="营销系统" label="营销系统" value="营销系统" />
            <el-option key="生产系统" label="生产系统" value="生产系统" />
            <el-option key="资产系统" label="资产系统" value="资产系统" />
            <el-option key="财务系统" label="财务系统" value="财务系统" />
            <el-option key="人资系统" label="人资系统" value="人资系统" />
          </el-select>
          <v-chart :options="mixData"></v-chart>
        </div>
      </el-col>

      <!-- <el-col :span="12">
        <v-chart :options="barData" class="chart section" />
      </el-col> -->
    </el-row>

    <!-- <el-row :gutter="20">
      <el-col :span="12">
        <div class="barchart section">
        <el-select v-model="barData_analyse_system" placeholder="项目" size="mini" style="margin-left:450px" @change="analyse_changesys">
            <el-option key="营销系统" label="营销系统" value="营销系统" />
            <el-option key="生产系统" label="生产系统" value="生产系统" />
            <el-option key="资产系统" label="资产系统" value="资产系统" />
            <el-option key="财务系统" label="财务系统" value="财务系统" />
            <el-option key="人资系统" label="人资系统" value="人资系统" />
          </el-select>
          <v-chart :options="barData_analyse" />
        </div>
      </el-col>
      <el-col :span="12">
          <div class="barchart section">
        <el-select v-model="barData_evaluate_system" placeholder="项目" size="mini" style="margin-left:450px" @change="evaluate_changesys"> 
            <el-option key="营销系统" label="营销系统" value="营销系统" />
            <el-option key="生产系统" label="生产系统" value="生产系统" />
            <el-option key="资产系统" label="资产系统" value="资产系统" />
            <el-option key="财务系统" label="财务系统" value="财务系统" />
            <el-option key="人资系统" label="人资系统" value="人资系统" />
          </el-select>
          <v-chart :options="barData_evaluate" />
        </div>
      </el-col>

    </el-row> -->
  </div>
</template>

<script src="./dashboard.js"></script>
<style rel="stylesheet/scss" lang="scss" scoped>
  .el-row{
    margin-bottom: 20px;
    &:last-child {
      margin-bottom: 0;
    }
  }

.dashboard {

  &-container {
    padding: 15px;
    background-color: #f0f2f5;
    overflow: auto;
    width: 1310px;
  }
  &-text {
    font-size: 14px;
    font-weight: bold;
    line-height: 22px;
    padding-bottom:15px;
  }
}
.echarts{
  width: 100%;
  height: 100%;
}
.box-card{
  height:108px;
}
  .chart{
    height: 350px;
    width: 1280px;
  }
  .section{
    padding-top:20px;
    padding-bottom:30px;
    padding-left:20px;
    padding-right:10px;
    background-color: white;
    border: 1px solid #ebeef5;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
  }

  .barchart{
    height: 350px;
  }

  .box-card > div >div > .svg-icon {
    width: 4em;
    height: 4em;
    color:#34bfa3;
  }
  .card-panel-text{
    padding-top:10px;
    font-size:16px;
    color:gray;
  }
  .card-panel-num{
    padding-top:10px;
    font-size:20px;
    font-weight: bold;
  }
</style>
