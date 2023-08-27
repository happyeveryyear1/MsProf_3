<template>
  <div style="padding:20px">
    <div style="margin-bottom:10px">
      <el-button-group>
        <el-button size="medium " :disabled="shrinkStatus">
          Swagger解析结果
        </el-button>
        <!-- <el-button size="mini" type="primary" :disabled="expandStatus" @click="enlargeColSize">展开
          <i v-if="expandLevel==='0'" class="el-icon-arrow-right el-icon--right" />
          <i v-if="expandLevel==='-1'" class="el-icon-d-arrow-right el-icon--right" /></el-button>
      </el-button-group> -->
        <!-- <el-button size="mini" type="primary" style="float:right" @click="openUrl">全屏显示</el-button> -->
      </el-button-group></div>
    <div style="display: flex;">
      <el-row type="flex" class="row-bg" justify="space-between" style="flex: 1;">
        <el-col :span="InfoColSize">
          <el-tabs v-model="activeName" type="border-card" style="auto">
            <el-tab-pane label="资源列表" name="har" :disabled="isPage">
              <json-viewer :expand-depth="4" expanded :value="resProdConsDep" copyable boxed sort />
            </el-tab-pane>
          </el-tabs>
        </el-col>
        <el-col :span="InfoColSize">
          <el-tabs v-model="activeName" type="border-card" style="auto">
            <el-tab-pane label="资源生产者消费者关系" name="har" :disabled="isPage">
              <json-viewer :expand-depth="4" expanded :value="resList" copyable boxed sort />
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script src="./viewSwaggerInfo.js"></script>

<style>
#container {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
  color: rgb(228, 226, 226);
}

.validUrl {
  margin-left:10px;
  cursor:pointer;
  text-decoration:none
}
</style>
