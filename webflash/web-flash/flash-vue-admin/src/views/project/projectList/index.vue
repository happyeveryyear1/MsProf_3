<template>
  <div class="app-container">
    <div class="block">
      <el-row>
        <el-col :span="24">
          <el-input v-model="search_projectName" placeholder="项目名" size="mini" clearable class="filter-item" style="width: 185px" />
          <el-select v-model="listQuery.systemName" size="mini" placeholder="系统" clearable style="margin-left:20px">
            <el-option key="营销系统" label="营销系统" value="营销系统" />
            <el-option key="生产系统" label="生产系统" value="生产系统" />
            <el-option key="资产系统" label="资产系统" value="资产系统" />
            <el-option key="财务系统" label="财务系统" value="财务系统" />
            <el-option key="人资系统" label="人资系统" value="人资系统" />
          </el-select>
          <el-button type="primary" icon="el-icon-search" style="margin-left:20px" size="mini" @click.native="search">{{ $t('button.search') }}</el-button>
          <el-button type="primary" icon="el-icon-refresh" size="mini" @click.native="reset">{{ $t('button.reset') }}</el-button>
          <el-button type="success" size="mini" icon="el-icon-plus" @click.native="add" v-permission="['/project/projectList/add']">{{ $t('button.add') }}</el-button>
          <el-button type="primary" size="mini" icon="el-icon-edit" @click.native="edit" v-permission="['/project/projectList/edit']">{{ $t('button.edit') }}</el-button>
          <el-button type="danger" size="mini" icon="el-icon-delete" @click.native="remove" v-permission="['/project/projectList/delete']">{{ $t('button.delete') }}</el-button>
        </el-col>
      </el-row>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      element-loading-text="Loading"
      border
      fit
      highlight-current-row
      @current-change="handleCurrentChange"
    >
      <el-table-column label="序号" min-width="50px" align="center">
        <template slot-scope="scope">
          {{ scope.$index+(listQuery.page-1)*listQuery.limit+1 }}
        </template>
      </el-table-column>
      <el-table-column label="项目名" min-width="150px" align="center">
        <template slot-scope="scope">
          {{ scope.row.projectName }}
        </template>
      </el-table-column>
      <!-- <el-table-column label="所属系统" min-width="100px" align="center">
        <template slot-scope="scope">
          {{ scope.row.systemName }}
        </template>
      </el-table-column>
      <el-table-column label="应用名" min-width="100px" align="center">
        <template slot-scope="scope">
          {{ scope.row.applicationName }}
        </template>
      </el-table-column> -->
      <el-table-column label="项目简介" min-width="200px" :show-overflow-tooltip="true" header-align="center">
        <template slot-scope="scope">
          {{ scope.row.projectIntroduction }}
        </template>
      </el-table-column>
      <!-- <el-table-column label="sonar ID" min-width="70px" :show-overflow-tooltip="true" header-align="center">
        <template slot-scope="scope">
          {{ scope.row.sonarId }}
        </template>
      </el-table-column> -->
      <el-table-column label="负责人" min-width="70px" align="center">
        <template slot-scope="scope">
          <!-- {{ scope.row.projectLeader }} -->
          {{ scope.row.leaderName }}
        </template>
      </el-table-column>
      <!-- <el-table-column label="测试用例地址" min-width="150px" header-align="center">
        <template slot-scope="scope">
          {{ scope.row.testcaseAddress }}
        </template> -->
      </el-table-column>
      <el-table-column label="Har地址" min-width="150px" header-align="center">
        <template slot-scope="scope">
          {{ scope.row.harAddress }}
        </template>
      </el-table-column>
      <el-table-column label="Swagger地址" min-width="150px" header-align="center">
        <template slot-scope="scope">
          {{ scope.row.swaggerAddress }}
        </template>
      </el-table-column>
      <el-table-column label="项目创建时间" min-width="120px" align="center"> 
        <template slot-scope="scope">
          {{ scope.row.createTime }}
        </template>
      </el-table-column>
      <el-table-column label="活动数" min-width="80px" align="center">
        <template slot-scope="scope">
          <el-button type="text" @click="viewActivity(scope.row.projectName,scope.row.id)">{{ scope.row.activities }}</el-button>
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

    <el-dialog
      :title="formTitle"
      :visible.sync="formVisible"
      width="70%"
      @close="closeDialog"
    >
      <el-form ref="form" :model="form" :rules="formRules" label-width="150px" style="margin-right:50px">

        <el-form-item
          label="项目名"
          prop="projectName"
        >
          <el-input v-model="form.projectName" minlength="1" placeholder="(项目名新建后不可更改)" :disabled="disabledInput" />
        </el-form-item>

        <!-- <el-form-item
          label="所属系统"
          prop="systemName"
        >
          <el-select v-model="form.systemName" placeholder="请选择项目所属系统" clearable class="filter-item" style="width:100%" :disabled="disabledInput">
            <el-option key="营销系统" label="营销系统" value="营销系统" />
            <el-option key="生产系统" label="生产系统" value="生产系统" />
            <el-option key="资产系统" label="资产系统" value="资产系统" />
            <el-option key="财务系统" label="财务系统" value="财务系统" />
            <el-option key="人资系统" label="人资系统" value="人资系统" />
          </el-select>
        </el-form-item> -->

        <!-- <el-form-item
          label="应用名"
          prop="applicationName"
        >
          <el-select v-model="form.applicationName" placeholder="请选择应用名" clearable class="filter-item" style="width:100%" :disabled="disabledInput">
            <el-option v-for="item in applicationNameList" :key="item.applicationName" :label="item.applicationName" :value="item.applicationName" />
          </el-select>
        </el-form-item> -->

        <el-form-item label="项目简介" prop="projectIntroduction">
          <el-input v-model="form.projectIntroduction" type="textarea" rows="6" />
        </el-form-item>

        <el-form-item
          label="项目负责人"
          prop="projectLeader"
        >
          <!-- <el-select v-model="form.projectLeader" placeholder="请选择项目负责人" clearable class="filter-item" style="width:100%">
            <el-option v-for="item in projectLeaders" :key="item.id" :label="item.name" :value="item.name" />
          </el-select> -->
          <el-input v-model="form.projectLeader" minlength="1" :disabled="true" />
        </el-form-item>

        <!-- <el-form-item
          label="Sonar ID"
          prop="sonarId"
        >
          <el-input v-model="form.sonarId" :disabled="disabledInput"  />
        </el-form-item> -->

        <!-- <el-form-item
          label="用例地址"
          prop="testcaseAddress"
        >
          <el-input v-model="form.testcaseAddress" minlength="1" />
        </el-form-item> -->

        <el-form-item
          label="Har地址"
          prop="harAddress"
        >
          <el-input v-model="form.harAddress" minlength="1" />
        </el-form-item>

        <el-form-item
          label="Swagger地址"
          prop="swaggerAddress"
        >
          <el-input v-model="form.swaggerAddress" minlength="1" />
        </el-form-item>

        <el-form-item style="text-align:right">
          <el-button type="primary" @click="save" :disabled='isSaving'>{{ $t('button.submit') }}</el-button>
          <el-button type="danger" @click.native="formVisible = false">{{ $t('button.cancel') }}</el-button>
        </el-form-item>

      </el-form>
    </el-dialog>
  </div>
</template>

<script src="./projectList.js"></script>

<style rel="stylesheet/scss" lang="scss" scoped>
    @import "src/styles/common.scss";
</style>

