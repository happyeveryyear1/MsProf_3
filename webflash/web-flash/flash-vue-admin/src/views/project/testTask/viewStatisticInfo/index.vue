<template>
  <div style="padding: 20px">
    <div style="margin-bottom: 10px" />

    <!-- 火焰图 -->
    <div style="width: 100%; overflow: auto">
      <div
        id="WWWWWWWWWWWWWWWWWWWWWWWW"
        ref="chart"
        style="height: 30vh; width: 30vw"
      />
    </div>

    <el-row :gutter="20">
      <el-col :span="6">
        <!-- 曲线图，请求耗时-占比分布 -->
        <div id="myChart" style="height: 30vh; width: 20vw" />
      </el-col>
      <el-col :span="6">
        <!-- 折线图，请求耗时-时间分布 -->
        <div id="chart-container" style="width: 40vh;height: 30vh;" />
      </el-col>
    </el-row>

    <div v-for="(bar_data_src_tmp, idx) in bar_data_src" :key="idx" style="height: 25vh; overflow:auto">
      <div :id="'chartContainer'+idx" :ref="'chartContainer'+idx" :style="{ height: bar_heights[idx]*25+25 + 'px' }" />
    </div>
    <!-- <div v-for="(bar_data_src, idx) in bar_data_src" :key="idx" style="height: 25vh; overflow:auto">
      <div id="tmp" :style="{height: 50 + 'px' , width: '60vh'}">
        <div :id="'chartContainer'+idx" :ref="'chartContainer'+idx" />
      </div>
    </div> -->
    <!-- <div id="2233" style="height: 25vh; overflow:auto">
      <div id="chartContainer0" ref="chartContainer0" :style="{ height: 5*18 + 'px' }" />
    </div> -->
  </div>
</template>

<script src="./viewStatisticInfo.js"></script>

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
  margin-left: 10px;
  cursor: pointer;
  text-decoration: none;
}

.d3-flame-graph rect {
  stroke: #eeeeee;
  fill-opacity: 0.8;
}

.d3-flame-graph rect:hover {
  stroke: #474747;
  stroke-width: 0.5;
  cursor: pointer;
}

.d3-flame-graph-label {
  pointer-events: none;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  font-size: 12px;
  font-family: Verdana;
  margin-left: 4px;
  margin-right: 4px;
  line-height: 1.5;
  padding: 0 0 0;
  font-weight: 400;
  color: black;
  text-align: left;
}

.d3-flame-graph .fade {
  opacity: 0.6 !important;
}

.d3-flame-graph .title {
  font-size: 20px;
  font-family: Verdana;
}

.d3-flame-graph-tip {
  line-height: 1;
  font-family: Verdana;
  font-size: 12px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  border-radius: 2px;
  pointer-events: none;
}

/* Creates a small triangle extender for the tooltip */
.d3-flame-graph-tip:after {
  box-sizing: border-box;
  display: inline;
  font-size: 10px;
  width: 100%;
  line-height: 1;
  color: rgba(0, 0, 0, 0.8);
  position: absolute;
  pointer-events: none;
}

/* Northward tooltips */
.d3-flame-graph-tip.n:after {
  content: "\25BC";
  margin: -1px 0 0 0;
  top: 100%;
  left: 0;
  text-align: center;
}

/* Eastward tooltips */
.d3-flame-graph-tip.e:after {
  content: "\25C0";
  margin: -4px 0 0 0;
  top: 50%;
  left: -8px;
}

/* Southward tooltips */
.d3-flame-graph-tip.s:after {
  content: "\25B2";
  margin: 0 0 1px 0;
  top: -8px;
  left: 0;
  text-align: center;
}

/* Westward tooltips */
.d3-flame-graph-tip.w:after {
  content: "\25B6";
  margin: -4px 0 0 -1px;
  top: 50%;
  left: 100%;
}
</style>
