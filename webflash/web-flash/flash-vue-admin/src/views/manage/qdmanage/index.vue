<template>
  <div id="QualityManage">
    <h2>质量数据管理</h2>
    <el-input v-model="filterText" placeholder="输入搜索关键词" style="margin-bottom:20px;" />
    <div class="treeCss">
      <el-tree
        ref="tree"
        :data="data"
        :filter-node-method="filterNode"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
      >
        <span slot-scope="{ node,data }" class="custom-tree-node">
          <span>{{ node.label }}</span>
          <span>
            <el-button
              type="text"
              size="mini"
              icon="el-icon-circle-plus-outline"
              @click="() => append(data)"
            >
              NEW
            </el-button>
            <el-button
              type="text"
              size="mini"
              icon="el-icon-remove"
              @click="() => remove(node, data)"
            >
              Del
            </el-button>
          </span>
        </span>
      </el-tree>
    </div>

    <el-row type="flex" justify="space-around">
      <el-col :span="5">
        <el-button type="primary" @click="qualityDataConf">配置质量数据</el-button>
      </el-col>

      <el-col :span="5">
        <el-button type="primary" @click="qualityRepConf">质量报告配置</el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script src="./qdmanage.js"></script>

<style>
#QualityManage {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding-top:15px; padding-bottom:25px; padding-left:35px; padding-right:35px;
  font-family: Helvetica, sans-serif;
  text-align: center;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%,-50%);
  width: 60%;
  height: 90%;
}

.custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
}

.treeCss{
    border: 1px solid rgb(207, 207, 207);
    border-radius: 4px;
    padding: 20px;
    margin-bottom: 20px;
    height: 65%;
    overflow:auto
}
</style>
