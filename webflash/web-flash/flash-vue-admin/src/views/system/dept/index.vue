<template>
  <div class="app-container">
    <div class="block">
      <el-button type="success" size="mini" icon="el-icon-plus"  @click.native="add">{{ $t('button.add') }}</el-button>
    </div>

    <tree-table
    :data="data"
    :expandAll="expandAll"
    highlight-current-row
    border
    v-loading="listLoading"
    >
      <el-table-column label="名称" header-align="center" min-width="80px">
        <template slot-scope="scope">
          <el-button type="text" @click="edit(scope.row)">{{scope.row.simplename}}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="全称" min-width="100px">
        <template slot-scope="scope">
          <span >{{scope.row.fullname}}</span>
        </template>
      </el-table-column>
      <el-table-column label="层级" min-width="150px">
        <template slot-scope="scope">
          <span >{{scope.row.level}}</span>
        </template>
      </el-table-column>
      <el-table-column label="顺序" min-width="50px" align="center">
        <template slot-scope="scope">
          <span >{{scope.row.num}}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="150px">
        <template slot-scope="scope">
          <el-button type="text" @click="edit(scope.row)">编辑</el-button>
          <el-button type="text" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </tree-table>

    <el-dialog
      :title="formTitle"
      :visible.sync="formVisible"
      width="70%"
      @close="closeDialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px" style="margin-right:30px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="名称" prop="simplename">
              <el-input v-model="form.simplename" minlength=1></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="全称" prop="fullname">
              <el-input v-model="form.fullname"  minlength=1></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="排序" prop="num">
              <el-input type="number" v-model="form.num"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="父部门" >
              <el-input
                placeholder="请选择父部门"
                v-model="form.pname"
                readonly="readonly"
                @click.native="showTree = !showTree">
              </el-input>
              <el-tree v-if="showTree"
                       empty-text="暂无数据"
                       :expand-on-click-node="false"
                       :data="data"
                       :props="defaultProps"
                       @node-click="handleNodeClick"
                       class="input-tree">
              </el-tree>

            </el-form-item>
          </el-col>


        </el-row>
        <el-form-item>
          <div style="float:right">
            <el-button type="primary" @click="save">{{ $t('button.submit') }}</el-button>
            <el-button @click.native="formVisible = false">{{ $t('button.cancel') }}</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script src="./dept.js"></script>
<style rel="stylesheet/scss" lang="scss" scoped>
  @import "src/styles/common.scss";
</style>
