<template>
  <div style="padding:20px">
    <div style="margin-bottom:10px">
      <el-button-group>
        <el-button size="mini" type="primary" @click="reduceColSize" :disabled="shrinkStatus">
          <i v-if="expandLevel==='1'" class="el-icon-d-arrow-left el-icon--left"></i>
          <i v-if="expandLevel==='0'" class="el-icon-arrow-left el-icon--left"></i>
          收起
        </el-button>
        <el-button size="mini" type="primary" @click="enlargeColSize" :disabled="expandStatus">展开
          <i v-if="expandLevel==='0'" class="el-icon-arrow-right el-icon--right"></i>
          <i v-if="expandLevel==='-1'" class="el-icon-d-arrow-right el-icon--right"></i></el-button>
      </el-button-group>
      <el-button size="mini" type="primary" style="float:right" @click="openUrl">全屏显示</el-button>
    </div>

    <div class="json-view">
      <div class="json-box">
        <div class="title" @click="toggleJson1">{{title1}}</div>
        <div v-if="showJson1" class="json-content">
          <url-box v-for="(request, url) in json1.url" :url="url" :request="request" :key="url"></url-box>
        </div>
      </div>
      <div class="json-box">
        <div class="title" @click="toggleJson2">{{title2}}</div>
        <div v-if="showJson2" class="json-content">
          <url-box v-for="(request, url) in json2.url" :url="url" :request="request" :key="url"></url-box>
        </div>
      </div>
    </div>
  </div>
</template>

<script src="./viewHarInfo.js"></script>

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
