<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.title" placeholder="质量报告名称" style="width: 200px;" class="filter-item" @keyup.enter.native="handleFilter" />

      <el-select v-model="listQuery.importance" placeholder="项目名" clearable style="width: 200px" class="filter-item">
        <el-option v-for="item in importanceOptions" :key="item" :label="item" :value="item" />
      </el-select>

      <el-select v-model="listQuery.type" placeholder="版本" clearable class="filter-item" style="width: 130px">
        <el-option v-for="item in calendarTypeOptions" :key="item.key" :label="item.display_name+'('+item.key+')'" :value="item.key" />
      </el-select>

      <el-select v-model="listQuery.sort" style="width: 130px" class="filter-item" placeholder="任务" @change="handleFilter">
        <el-option v-for="item in sortOptions" :key="item.key" :label="item.label" :value="item.key" />
      </el-select>

      <el-button v-waves class="filter-item" type="primary" icon="el-icon-search" style="margin-left:30px" @click="handleFilter">
        查询
      </el-button>
    </div>
    <el-table :data="list" border fit highlight-current-row style="width: 100%">
      <el-table-column
        v-loading="loading"
        align="center"
        label="序号"
        width="80"
        element-loading-text="请给我点时间！"
      >
        <template slot-scope="scope">
          <span>{{ scope.$index+1 }}</span>
        </template>
      </el-table-column>

      <el-table-column width="500" align="center" label="质量报告名称">
        <template slot-scope="scope">
          {{ scope.row.title }}
        </template>
      </el-table-column>

      <el-table-column align="center" label="项目">
        <template>
          <!-- {{ scope.row.id }} -->
          营销系统
        </template>
      </el-table-column>

      <el-table-column align="center" label="版本">
        <template slot-scope="scope">
          <router-link :to="'/example/edit/'+scope.row.id" class="link-type">
            {{ scope.$index+24 }}
          </router-link>
        </template>
      </el-table-column>

      <el-table-column align="center" label="查看报告">
        <template>
          <el-button type="text" @click="subItem">分项</el-button>
          <el-button type="text" @click="comprehensive">综合</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="fetchData" />
  </div>
</template>

<script src="./list.js"></script>

<style lang="scss" scoped>
.filter-container {
  margin-bottom: 20px;
}
</style>

