<template>
  <div class="app-container">
    <div class="block">
      <el-row  :gutter="20">
        <el-col :span="6">
          <el-input v-model="search_account" size="mini" placeholder="请输入帐号"></el-input>
        </el-col>
        <el-col :span="6">
          <el-input v-model="search_name" size="mini" placeholder="请输入姓名"></el-input>
        </el-col>
        <el-col :span="6">
          <el-button type="success" size="mini" icon="el-icon-search" @click.native="search">{{ $t('button.search') }}</el-button>
          <el-button type="primary" size="mini" icon="el-icon-refresh" @click.native="reset">{{ $t('button.reset') }}</el-button>
        </el-col>
      </el-row>
      <br>
      <el-row>
        <el-col :span="24">
          <el-button type="success" size="mini" icon="el-icon-plus" @click.native="add" v-permission="['/mgr/add']">
            {{$t('button.add') }}
          </el-button>
          <el-button type="primary" size="mini" icon="el-icon-edit" @click.native="edit" v-permission="['/mgr/edit']">
            {{$t('button.edit') }}
          </el-button>
          <el-button type="danger" size="mini" icon="el-icon-delete" @click.native="remove" v-permission="['/mgr/delete']">
            {{$t('button.delete') }}
          </el-button>
          <el-button type="info" size="mini" icon="el-icon-role" @click.native="openRole" v-permission="['/mgr/setRole']">角色分配</el-button>
        </el-col>
      </el-row>
    </div>


    <el-table :data="list" v-loading="listLoading" element-loading-text="Loading" border fit highlight-current-row
    @current-change="handleCurrentChange">

      <el-table-column label="序号" min-width="30px" align="center">
        <template slot-scope="scope">
          {{ scope.$index+(listQuery.page-1)*listQuery.limit+1 }}
        </template>
      </el-table-column>

      <el-table-column label="账号" header-align="center">
        <template slot-scope="scope">
          {{scope.row.account}}
        </template>
      </el-table-column>
      <el-table-column label="姓名" header-align="center">
        <template slot-scope="scope">
          {{scope.row.name}}
        </template>
      </el-table-column>
      <el-table-column label="性别" header-align="center">
        <template slot-scope="scope">
          {{scope.row.sexName}}
        </template>
      </el-table-column>
      <el-table-column label="角色" header-align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{scope.row.roleName}}
        </template>
      </el-table-column>
      <el-table-column label="部门" header-align="center">
        <template slot-scope="scope">
          {{scope.row.deptName}}
        </template>
      </el-table-column>
      <el-table-column label="邮箱" header-align="center">
        <template slot-scope="scope">
          {{scope.row.email}}
        </template>
      </el-table-column>
      <el-table-column label="电话" header-align="center">
        <template slot-scope="scope">
          {{scope.row.phone}}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" :show-overflow-tooltip="true" header-align="center">
        <template slot-scope="scope">
          {{scope.row.createTime}}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center">
        <template slot-scope="scope">
          <el-switch v-if="scope.row.id!=='1'" v-model="switchStatus[scope.row.id]" @change="changeUserStatus(scope.row)"></el-switch>
          <el-switch v-else v-model="switchStatus[scope.row.id]" :disabled="true"></el-switch>
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
      @next-click="fetchNext">
    </el-pagination>

    <el-dialog
      :title="formTitle"
      :visible.sync="formVisible"
      width="70%"
      @close="closeDialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px" label-position="right" style="margin-right:30px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="账户" prop="account">
              <el-input v-model="form.account" minlength=1 style="width:95%" :disabled="!isAdd"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-if="form.id === '1'" v-model="form.name"  minlength=1 style="width:95%" :disabled="true"></el-input>
              <el-input v-else v-model="form.name"  minlength=1 style="width:95%"></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item v-if="canChangePwd" label="密码" prop="password">
              <el-input v-model="form.password" :type="pwdType" style="width:95%"></el-input>
              <span class="show-pwd" @click="showPwd">
                <svg-icon :icon-class="pwdType === 'password' ? 'eye' : 'eye-open'" />
              </span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="canChangePwd" label="确认密码" prop="rePassword">
              <el-input v-model="form.rePassword" :type="rePwdType" style="width:95%"></el-input>
              <span class="show-pwd" @click="showRePwd">
                <svg-icon :icon-class="rePwdType === 'password' ? 'eye' : 'eye-open'" />
              </span>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.sex">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" style="width:95%"></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" style="width:95%"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" >
              <el-input
                placeholder="请选择所属部门"
                v-model="form.deptName"
                readonly="readonly"
                @click.native="deptTree.show  = !deptTree.show"
                style="width:95%">
              </el-input>
              <el-tree v-if="deptTree.show"
                       empty-text="暂无数据"
                       :expand-on-click-node="false"
                       :data="deptTree.data"
                       :props="deptTree.defaultProps"
                       @node-click="handleNodeClick"
                       class="input-tree">
              </el-tree>

            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否启用" prop="status">
              <el-switch v-if="form.id === '1'" v-model="form.status" :disabled="true"></el-switch>
              <el-switch v-else v-model="form.status"></el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期">
                <el-date-picker type="date" placeholder="选择日期" v-model="form.birthday" style="width: 95%;">

                </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <div style="float:right;margin-right:20px">
            <el-button v-if="isChangePwd" type="text" style="margin-right:20px" @click="changePWD">{{ pwdtext }}</el-button>
            <el-button type="primary" @click="saveUser">{{ $t('button.submit') }}</el-button>
            <el-button @click.native="formVisible = false">{{ $t('button.cancel') }}</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog
      title="角色分配"
      :visible.sync="roleDialog.visible"
      width="25%">
      <el-form>
        <el-row>
          <el-col :span="12">
            <el-tree
              :data="roleDialog.roles"
              ref="roleTree"
              show-checkbox
              node-key="id"
              :default-checked-keys="roleDialog.checkedRoleKeys"
              :props="roleDialog.defaultProps">
            </el-tree>

          </el-col>
        </el-row>
        <el-form-item style="margin-top:20px;text-align:center">
          <el-button type="primary" @click="setRole">{{ $t('button.submit') }}</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script src="./user.js"></script>
<style rel="stylesheet/scss" lang="scss">
  @import "src/styles/common.scss";
  .el-tree-node__label {
    font-size: 14px;
    margin-left: 8px;
}
</style>

