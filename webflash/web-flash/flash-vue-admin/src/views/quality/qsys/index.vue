<template>
  <div class="treeCss">
    <el-row :gutter="20" type="flex" justify="center" style="margin-bottom:20px">
      <el-col :xs="24" :sm="24" :lg="12" :offset="6">
          <el-button type="primary" @click="edit"> {{btn_title}} </el-button>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="12">
          <el-button type="primary" @click="save" v-bind:disabled="is_disabled">提交</el-button>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :lg="12" :offset="5">
      <el-tree
        :data="data"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
         style="margin-bottom:30px">
        <span class="custom-tree-node" slot-scope="{ node, data }">
          <span>{{ node.label }}</span>
          <span v-if="node.label == '质量数据管理'" >
              开启/关闭 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
          </span>
          <span v-else-if="node.label == '...'" >
            <el-button
              type="text"
              size="mini"
              icon="el-icon-circle-plus-outline"
              v-bind:disabled="is_disabled"
              @click="() => append(data)">
              新建
            </el-button>  
          </span>
          <span v-else >
            <el-switch
              v-model="data.status"
              v-bind:disabled="is_disabled" 
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="handleStatChange(node, data)">
            </el-switch>
            <el-button
              type="text"
              size="mini"
              icon="el-icon-remove"
              @click="select(data)">
              查看
            </el-button>
            <el-button
              type="text"
              size="mini"
              icon="el-icon-remove"
              v-bind:disabled="is_disabled"
              @click="() => remove(node, data)">
              删除
            </el-button>
          </span>
        </span>
      </el-tree>
      </el-col>
    </el-row>
    
    <el-dialog
      title="编辑"
      :visible.sync="dialogVisible"
      width="60%">
        <p>名称</p>
        <el-input
          type="textarea"
          autosize
          placeholder="请输入内容"
          v-bind:disabled="is_disabled"
          v-model="name">
        </el-input>
        <div style="margin: 20px 0;"></div>
        <p>描述</p>
        <el-input
          type="textarea"
          autosize
          placeholder="请输入内容"
          v-bind:disabled="is_disabled"
          v-model="textarea_description">
        </el-input>
        <div style="margin: 20px 0;"></div>
        <p v-if="is_metric">数据源URI</p>
        <el-input v-if="is_metric"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 4}"
          placeholder="请输入内容"
          v-bind:disabled="is_disabled"
          v-model="textarea_datasource_path">
        </el-input>
        <div style="margin: 20px 0;"></div>
        <p v-if="is_metric">数据源URI类型</p>
        <el-radio-group v-if="is_metric" v-bind:disabled="is_disabled" v-model="radio_datasource_path_type">
          <el-radio label="RH">Http链接(GET)</el-radio>
          <el-radio label="RHP">Http链接(POST)</el-radio>
          <el-radio label="L">本地路径</el-radio>
          <el-radio label="I">直接</el-radio>
        </el-radio-group>
        <div style="margin: 20px 0;"></div>
        <p v-if="is_metric">数据格式</p>
        <el-radio-group v-if="is_metric" v-bind:disabled="is_disabled" v-model="radio_datasource_format">
          <el-radio label="JSON">JSON</el-radio>
          <el-radio label="XML">XML</el-radio>
          <el-radio label="CSV">CSV</el-radio>
          <el-radio label="NUM">Number</el-radio>
        </el-radio-group>
        <p v-if="is_metric">数据处理脚本路径</p>
        <el-input v-if="is_metric"
          type="textarea"
          autosize
          placeholder="请输入内容"
          v-bind:disabled="is_disabled"
          v-model="textarea_datahandler">
        </el-input>
      <span slot="footer" class="dialog-footer">
          <el-button v-if="is_metric" type="primary" @click="test_metric()">测试</el-button>
          <el-button type="primary" @click="local_save()">暂存</el-button>
          <el-button type="primary" @click="cancel()">取消</el-button>
      </span>
    </el-dialog>

    </div>

</template>

<script src="./qsys.js"></script>

<style>
.custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
}

.treeCss{
    height: 400px; 
    padding: 20px;
    margin-bottom: 20px;
}

</style>
