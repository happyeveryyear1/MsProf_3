<template>
  <div id="QualityDataConf">
    <h2 style="text-align:center">质量数据配置项</h2>
    <el-form ref="postForm" label-position="left" :model="postForm">
      <el-form-item label="Functional completeness">
        <el-switch
          v-model="postForm.switchValue"
          active-color="#13ce66"
          inactive-color="#ff4949"
        />
      </el-form-item>
      <div class="contentDiv">
        <el-form-item label-width="100px" label="度量项含义" style="margin-top:10px">
          <el-input v-model="postForm.dlxhy" placeholder="默认" />
        </el-form-item>

        <el-form-item label-width="100px" label="数据源" style="margin-top:20px">
          <el-input v-model="postForm.dataSource" />
        </el-form-item>

        <el-form-item label-width="100px" label="数据格式" style="margin-top:20px">
          <el-select v-model="postForm.value" placeholder="请选择" style="width:100%">
            <el-option label="JSON" value="json" />
            <el-option label="xml" value="xml" />
          </el-select>
        </el-form-item>

        <el-form-item label-width="100px" label="计算脚本" style="margin-top:20px">
          <el-input v-model="postForm.calcuScript" placeholder="默认（处理数据源数据）" />
        </el-form-item>

        <el-form-item label-width="100px" label="手工录入" style="margin-top:20px">
          <el-input v-model="postForm.manualEnter" />
        </el-form-item>
      </div>
    </el-form>
    <div style="text-align:center">
      <el-button type="primary" @click="verify">验证</el-button>
      <el-button type="primary" style="margin-left:60px" @click="saveConf">保存</el-button>
    </div>
  </div>
</template>

<script src="./qdconf.js"></script>

<style lang="scss" scoped>
#QualityDataConf {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding-top:15px; padding-bottom:25px; padding-left:35px; padding-right:35px;
  font-family: Helvetica, sans-serif;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%,-50%);
  width: 60%;
  height: 90%;
}

.contentDiv{
  border: 1px solid rgb(207, 207, 207);
  border-radius: 4px;
  padding:20px;
  padding-bottom: 10px;
  margin-bottom: 20px;
}
</style>
