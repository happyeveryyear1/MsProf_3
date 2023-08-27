<template>
  <div class="app-container">
    <span class="textStyle">项目名：</span>
    <el-input v-model="search_projectName" style="margin-right:20px;width:150px" size="mini" placeholder="项目" clearable />
    <el-button type="primary" size="mini" icon="el-icon-search"  @click.native="search">{{ $t('button.search') }}</el-button>
    <el-button type="primary" size="mini" icon="el-icon-refresh"  @click.native="reset">{{ $t('button.reset') }}</el-button>
    <el-button type="primary" size="mini" @click="compare" style="float:right">对比</el-button>
    <el-table v-loading="listLoading" :data="list" border fit highlight-current-row style="width: 100%;margin-top:20px" ref="multipleTable">
      <el-table-column
          type="selection"
          align="center">
        </el-table-column>      
      <el-table-column label="序号" min-width="30px" align="center">
        <template slot-scope="scope">
          {{ scope.$index+(listQuery.page-1)*listQuery.limit+1 }}
        </template>
      </el-table-column>
      <el-table-column min-width="300px" label="项目">
        <template slot-scope="scope">
          <router-link :to="'/quality/vlist/'+scope.row.id" class="link-type">
            <span>{{ scope.row.projectName }}</span>
          </router-link>
        </template>
      </el-table-column>
      <el-table-column align="center" label="质量体系" width="120">
        <template slot-scope="scope">
          <router-link :to="'/quality/qsys/'+scope.row.id">
            <el-button type="primary" size="mini" >
              查看
            </el-button>
          </router-link>
        </template>
      </el-table-column>
      <el-table-column align="center" label="测试任务列表" width="120">
        <template slot-scope="scope">
          <router-link :to="'/quality/vlist/'+scope.row.id">
            <el-button type="primary" size="mini" >
              查看
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

  </div>
</template>

<script src="./proList.js"></script>

<style scoped>
.compareBtn {
    position: absolute;
    right: 30px;
    padding: 15px;
}

.textStyle {
    font-size: 13px;
    color: gray;
  }
</style>

