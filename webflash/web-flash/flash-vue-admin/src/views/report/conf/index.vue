<template>
  <div class="app-container">
    <br>
    <el-form ref="form" label-position="right" :model="form" label-width="100px">
      <el-row>
        <el-col :span="6">
          <el-form-item
            label="项目名："
            prop="projectName"
            :rules="[{ required: true, message: '项目名不能为空', trigger: 'change' }]"
          >
            <el-select v-model="form.projectName" placeholder="请选择项目名" @change="editPro">
              <el-option v-for="item in name_unique(projectList)" :key="item.id" :label="item.projectName" :value="item.projectName" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item
            label="版本号："
            prop="versionNum"
            :rules="[{ required: true, message: '版本号不能为空', trigger: 'change' }]"
          >
            <el-select v-model="form.versionNum" placeholder="请选择版本号" :disabled="verSel">
              <el-option v-for="item in version_unique(versionList)" :key="item.id" :label="item.versionNum" :value="item.versionNum" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="客观数据：">
        <el-tree
          :data="objectiveData"
          show-checkbox
          node-key="id"
          default-expand-all
          :expand-on-click-node="false"
          class="dataDiv"
        >
          <span slot-scope="{ node }">
            <span style="margin-left:10px">{{ node.label }}</span>
            <span>w.<el-input v-model="form.w" type="text" class="line" /></span>
          </span>
        </el-tree>
      </el-form-item>
      <br>
      <el-form-item label="主观评价：">
        <el-input
          v-model="form.textarea"
          type="textarea"
          :rows="6"
          placeholder="请输入内容"
        />
      </el-form-item>
    </el-form>
    <div style="text-align:center;margin-top:40px">
      <el-button type="primary" style="width:150px" @click="generateWeb">生成Web页面</el-button>
      <el-button type="primary" style="width:150px;margin-left:90px" @click="generatePDF">生成PDF</el-button>
    </div>
  </div>
</template>

<script src="./conf.js"></script>

<style rel="stylesheet/scss" lang="scss" scope>
    @import "src/styles/common.scss";
.dataDiv{
  border: 1px solid rgb(219, 216, 216);
  border-radius: 4px;
  margin-top: 10px;
  padding: 10px;
  overflow: auto;
  height:250px;
}
.line /deep/ .el-input__inner {
	width: 50px;
    height: 20px;
    border-radius: 0;
	border-top: 0px;
    border-left: 0px;
    border-right: 0px;
	// border-bottom-width: 1px, solid;
}
</style>
