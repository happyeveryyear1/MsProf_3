<template>
  <div class="app-container">
    <div class="block">
      <el-row>
        <el-col :span="24">
          <el-input v-model="search_testactivityName" placeholder="活动名" size="mini" clearable class="filter-item" style="width: 150px" />
          <el-button type="primary" icon="el-icon-search" size="mini" style="margin-left:20px" @click.native="search">{{ $t('button.search') }}</el-button>
          <el-button type="primary" icon="el-icon-refresh" size="mini" @click.native="reset">{{ $t('button.reset') }}</el-button>
          <el-button type="success" icon="el-icon-plus" size="mini" @click.native="add" v-permission="['/project/projectActivity/add']">{{ $t('button.add') }}</el-button>
          <el-button type="warning" icon="el-icon-edit" size="mini" @click.native="edit" v-permission="['/project/projectActivity/edit']">{{ $t('button.edit') }}</el-button>
          <el-button type="danger" icon="el-icon-delete" size="mini" @click.native="remove" v-permission="['/project/projectActivity/delete']">{{ $t('button.delete') }}</el-button>
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
      <el-table-column label="活动名" min-width="150px" header-align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{ scope.row.testactivityName }}
        </template>
      </el-table-column>
      <el-table-column label="项目名" min-width="150px" header-align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <!-- {{ scope.row.projectName }} -->
          {{ scope.row.proName }}
        </template>
      </el-table-column>
      <el-table-column label="活动简介" min-width="200px" header-align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{ scope.row.testactivityIntroduction }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="100px" header-align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{ scope.row.createTime }}
        </template>
      </el-table-column>
      <el-table-column label="任务数" min-width="50px" align="center">
        <template slot-scope="scope">
          <router-link :to="'/project/testTask/'+scope.row.id">
          <el-button type="text">
            {{ scope.row.tasks }}
          </el-button>
          </router-link>
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
      width="50%"
      @close="closeDialog"
    >
      <el-form ref="form" :model="form" label-width="150px" :rules="formRules" style="margin-right:50px">
        <el-form-item
          label="活动名"
          prop="testactivityName"
        >
          <el-input v-model="form.testactivityName" minlength="1" />
        </el-form-item>
        <el-form-item
          label="项目名"
          prop="projectName"
          :rules="[{ required: true, message: '项目名不能为空', trigger: 'blur' }]"
        >
          <el-input
            v-if="projectName"
            v-model="form.projectName"
            style="width:100%"
            :disabled="true"
          />

          <el-select v-else v-model="form.projectName" placeholder="请选择项目名" clearable class="filter-item" style="width:100%" :disabled="!isAdd">
            <el-option v-for="item in proList" :key="item.id" :label="item.projectName" :value="item.projectName" />
          </el-select>
        </el-form-item>
        <el-form-item
          label="活动简介"
          prop="testactivityIntroduction"
        >
          <el-input v-model="form.testactivityIntroduction" minlength="1" />
        </el-form-item>

        <el-form-item style="text-align:right">
          <el-button type="primary" @click="save" :disabled="isSaving">{{ $t('button.submit') }}</el-button>
          <el-button @click.native="formVisible = false">{{ $t('button.cancel') }}</el-button>
        </el-form-item>

      </el-form>
    </el-dialog>
  </div>
</template>

<script src="./testActivity.js"></script>

<style rel="stylesheet/scss" lang="scss" scoped>
    @import "src/styles/common.scss";
</style>

