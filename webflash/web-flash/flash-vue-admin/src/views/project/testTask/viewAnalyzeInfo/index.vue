<template>
  <div style="padding: 20px">
    <div style="margin-bottom: 10px" />

    <!-- <div v-for="(bar_data_src_tmp, idx) in bar_data_src" :key="idx" style="height: 25vh; overflow:auto">
      <div :id="'chartContainer'+idx" :ref="'chartContainer'+idx" :style="{ height: bar_heights[idx]*20 + 'px' }" />
    </div> -->
    <el-collapse v-model="activeNames" @change="handleChange">
      <el-collapse-item v-for="(analyzeResultItem, analyzeResultIdx) in bar_data_src" :key="analyzeResultIdx" :title="'接口异常'+convertToChinese(analyzeResultIdx+1)" :name="analyzeResultIdx">
        <el-collapse v-model="activeNames" @change="handleChange">
          <el-collapse-item v-for="(rootCauseItem, rootCauseItemIdx) in analyzeResultItem" :key="rootCauseItemIdx" :title="'>>> 结构异常'+convertToChinese(rootCauseItemIdx+1)" :name="analyzeResultIdx+'-'+rootCauseItemIdx">

            <!-- 该异常异常程度排序第，它共出现xx次，异常结构请求占比 20%，异常结构平均耗时200ms： 对应正常结构平均耗时：100ms。异常结构"", 异常Span, 正常结构"" -->
            <div v-if="rootCauseItem['类型']=='type1'" style="margin-left: 30px;">

              <span>该异常在该接口中排序第 {{ rootCauseItemIdx+1 }}，其异常类型为结构异常。它一共出现了{{ rootCauseItem['异常出现次数'] }}次，异常结构请求占比：{{ convertToPercentage(rootCauseItem['异常占比']) }}，异常结构平均耗时：{{ formatCostTime(rootCauseItem['异常平均耗时']) }}，对应正常结构平均耗时：{{ formatCostTime(rootCauseItem['正常平均耗时']) }}。</span>
              <br>
              <span>分析得知异常根因为：{{ rootCauseItem['异常Span'] }} </span>
              <el-divider />
              <span>异常结构</span>
              <div :id="'div '+analyzeResultIdx+'-' + rootCauseItemIdx + ' abnormal'" style="height: 25vh; overflow:auto">
                <div :id="'barDiv '+analyzeResultIdx+'-'+rootCauseItemIdx + ' abnormal'" :ref="'barDiv '+analyzeResultIdx+'-'+rootCauseItemIdx" :style="{ height: bar_data[analyzeResultIdx][rootCauseItemIdx]['dataAbnormal'].length*20+20 + 'px' }" />
              </div>
              <el-divider />
              <span>正常结构</span>
              <div :id="'div '+analyzeResultIdx+'-' + rootCauseItemIdx + ' normal'" style="height: 25vh; overflow:auto">
                <div :id="'barDiv '+analyzeResultIdx+'-'+rootCauseItemIdx + ' normal'" :ref="'barDiv '+analyzeResultIdx+'-'+rootCauseItemIdx" :style="{ height: bar_data[analyzeResultIdx][rootCauseItemIdx]['dataNormal'].length*20+20 + 'px' }" />
              </div>
              <div />
            </div>

            <!-- 该异常异常程度排序第，其异常为selfTime异常，它共出现xx次，异常结构请求占比 20%，异常结构平均耗时200ms： 对应正常结构平均耗时：100ms，异常selfTime根因平均时间50ms，正常selfTime根因对应平均时间10ms. 结构"", 异常span"", 根因span"". -->
            <div v-if="rootCauseItem['类型']=='type2'" style="margin-left: 30px;">
              <span>该异常在该接口中排序第 {{ rootCauseItemIdx+1 }}，其异常类型为本地计算时间异常。它一共出现了{{ rootCauseItem['异常出现次数'] }}次，异常结构请求占比：{{ convertToPercentage(rootCauseItem['异常占比']) }}，异常结构平均耗时：{{ formatCostTime(rootCauseItem['异常平均耗时']) }}，对应正常结构平均耗时：{{ formatCostTime(rootCauseItem['正常平均耗时']) }}。</span>
              <br>
              <span> 分析得知异常服务为：{{ rootCauseItem['异常Span'] }} ，异常本地计算平均时间为：{{ formatCostTime(rootCauseItem['根因异常SelfTime耗时']) }}，正常本地计算平均时间为：{{ formatCostTime(rootCauseItem['根因正常SelfTime耗时']) }} </span>
              <div />
              <el-divider />
              <span>异常结构</span>
              <div :id="'div '+analyzeResultIdx+'-' + rootCauseItemIdx" style="height: 25vh; overflow:auto">
                <div :id="'barDiv '+analyzeResultIdx+'-'+rootCauseItemIdx" :ref="'barDiv '+analyzeResultIdx+'-'+rootCauseItemIdx" :style="{ height: bar_data[analyzeResultIdx][rootCauseItemIdx]['data'].length*20+20 + 'px' }" />
              </div>
            </div>

            <div v-if="rootCauseItem['类型']=='type3'" style="margin-left: 30px;">
              <span>该异常在该接口中排序第 {{ rootCauseItemIdx+1 }}，其异常类型为请求阻塞异常。它一共出现了{{ rootCauseItem['异常出现次数'] }}次，异常结构请求占比：{{ convertToPercentage(rootCauseItem['异常占比']) }}，异常结构平均耗时：{{ formatCostTime(rootCauseItem['异常平均耗时 ']) }}，对应正常结构平均耗时：{{ formatCostTime(rootCauseItem['正常平均耗时']) }}。</span>
              <br>
              <span> 分析得知异常调用为：{{ rootCauseItem['异常Span'] }} ，它一共被阻塞：{{ rootCauseItem['阻塞总耗时'] }} </span>
              <div />
              <el-divider />
              <span>异常结构</span>
              <div :id="'div '+analyzeResultIdx+'-' + rootCauseItemIdx" style="height: 25vh; overflow:auto">
                <div :id="'barDiv '+analyzeResultIdx+'-'+rootCauseItemIdx" :ref="'barDiv '+analyzeResultIdx+'-'+rootCauseItemIdx" :style="{ height: bar_data[analyzeResultIdx][rootCauseItemIdx]['data'].length*20+20 + 'px' }" />
              </div>
              <el-divider />
              <span>阻塞耗时占比</span>
              <div :id="'div '+analyzeResultIdx+'-' + rootCauseItemIdx" style="height: 25vh; overflow:auto">
                <div :id="'pieDiv '+analyzeResultIdx+'-'+rootCauseItemIdx" :ref="'pieDiv '+analyzeResultIdx+'-'+rootCauseItemIdx" style="height: 20vh; weight: 60vh; overflow:auto" />
              </div>
              <el-divider />
              <span style="margin-bottom: 5px">阻塞原因</span>
              <div :id="'blockList '+analyzeResultIdx+'-'+rootCauseItemIdx" style="margin-left: 30px;">
                <div v-for="(blockItem, blockIdx) in rootCauseItem['上游异常']" :key="blockIdx">
                  <span>阻塞原因: {{ rootCauseItem['上游异常'][blockIdx]["根因Span"] }}, 异常平均QPS: {{ rootCauseItem['上游异常'][blockIdx]["异常平均QPS"] }}, 正常平均QPS: {{ rootCauseItem['上游异常'][blockIdx]["正常平均QPS"] }}</span>
                  <div :id="'div '+analyzeResultIdx+'-'+rootCauseItemIdx+'-'+blockIdx" style="height: 25vh; overflow:auto">
                    <div :id="'barDiv '+analyzeResultIdx+'-'+rootCauseItemIdx+'-'+blockIdx" :ref="'barDiv '+analyzeResultIdx+'-'+rootCauseItemIdx+'-'+blockIdx" :style="{ height: bar_data[analyzeResultIdx][rootCauseItemIdx]['blockTraceList'][blockIdx]['data'].length*20+20 + 'px' }" />
                  </div>
                </div>
                <el-divider />
              </div>
            </div>

          </el-collapse-item>
        </el-collapse>

      </el-collapse-item>
    </el-collapse>
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

<script src="./viewAnalyzeInfo.js"></script>

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
