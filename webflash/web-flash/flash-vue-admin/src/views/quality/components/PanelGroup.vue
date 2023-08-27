<template>
  <el-row type="flex" justify="space-between" class="panel-group">
    <el-col :span="6" class="card-panel-col">
      <div class="card-panel">
        <div class="card-panel-icon-wrapper icon-component">
          <svg-icon icon-class="component" class-name="card-panel-icon" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            项目
          </div>
          <div class="card-panel-num">
            {{ proj_name }}
          </div>
        </div>
      </div>
    </el-col>
    <el-col :span="6" class="card-panel-col">
      <div class="card-panel">
        <div class="card-panel-icon-wrapper icon-tree">
          <svg-icon icon-class="tree" class-name="card-panel-icon" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            任务
          </div>
          <div class="card-panel-num">
            {{ ver_tag }}
          </div>
        </div>
      </div>
    </el-col>
    <el-col :span="6" class="card-panel-col">
      <div class="card-panel">
        <div class="card-panel-icon-wrapper icon-dashboard">
          <svg-icon icon-class="dashboard" class-name="card-panel-icon" />
        </div>
        <div class="card-panel-description">
          <div class="card-panel-text">
            综合评价
          </div>
          <div><el-rate v-model="value5" disabled show-score score-template="{value}"></el-rate></div>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import { getProject } from '@/api/quality/qproject'

export default {
  props: {
    proj_id: {
      type: String,
      required: true
    },
    ver_tag: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      score: null,
      proj_name: null,
      value5: null
    }
  },
  mounted() {
    let queryData = {}
    queryData['proj_id'] = this.proj_id
    queryData['ver_tag'] = this.ver_tag
    getProject(queryData).then(response => {
        const cdata = response.data
        const qas = cdata['quality_aspects']
        let s = 0
        let l = 0
        for (const idx in qas) {
          s = s + qas[idx]['score']
          l = l + 1
        }
        this.score = '(' + Math.round(s / l) + ')'
        // this.value = this.score
        this.proj_name = cdata['project_name']
        this.ver_tag = cdata['version_tag']
        this.value5 = Math.round(s / l / 20.0 * 10)/10
        console.log(this.score)
      })
      .catch(function(error) {
        console.log(error)
      })
  }
}
</script>

<style lang="scss" scoped>
.panel-group {
  margin-top: 18px;

  .card-panel-col {
    margin-bottom: 32px;
  }

  .card-panel {
    height: 108px;
    cursor: pointer;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #666;
    background: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
    border-color: rgba(0, 0, 0, .05);

    &:hover {
      .card-panel-icon-wrapper {
        color: #fff;
      }

      .icon-component {
        background: #40c9c6;
      }

      .icon-tree {
        background: #36a3f7;
      }

      .icon-dashboard {
        background: #f4516c;
      }

      .icon-shopping {
        background: #34bfa3
      }
    }

    .icon-component {
      color: #40c9c6;
    }

    .icon-tree {
      color: #36a3f7;
    }

    .icon-dashboard {
      color: #f4516c;
    }

    .icon-shopping {
      color: #34bfa3
    }

    .card-panel-icon-wrapper {
      float: left;
      margin: 14px 0 0 14px;
      padding: 16px;
      transition: all 0.38s ease-out;
      border-radius: 6px;
    }

    .card-panel-icon {
      float: left;
      font-size: 48px;
    }

    .card-panel-description {
      float: right;
      font-weight: bold;
      margin: 26px;
      margin-left: 0px;

      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }

      .card-panel-num {
        font-size: 20px;
      }
    }
  }
}

@media (max-width:550px) {
  .card-panel-description {
    display: none;
  }

  .card-panel-icon-wrapper {
    float: none !important;
    width: 100%;
    height: 100%;
    margin: 0 !important;

    .svg-icon {
      display: block;
      margin: 14px auto !important;
      float: none !important;
    }
  }
}
</style>
