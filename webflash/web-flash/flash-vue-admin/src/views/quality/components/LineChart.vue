<template>
  <div :class="className" :style="{height:height,width:width,padding:padding}" />
</template>

<script>
import { getQA } from '@/api/quality/qsys'
import echarts from 'echarts'
require('echarts/theme/macarons') // echarts theme
import resize from './mixins/resize'

export default {
  mixins: [resize],
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    padding: {
      type: String,
      default: '20px'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '350px'
    },
    autoResize: {
      type: Boolean,
      default: true
    },
    qualityaspect: {
      // type: String,
      required: true
    },
    proj_id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      chart: null,
      selecteds: {}
    }
  },
  watch: {
    qualityaspect: {
      deep: true,
      handler(val) {
        this.setSelected(val)
      }
    }
  },
  mounted() {
    let queryData = {}
    queryData['proj_id'] = this.proj_id
    getQA(queryData).then(response => {
        console.log(response)
        const cdata = response.data
        const proj_name = cdata['project_id_name']
        const qas = cdata['quality_aspects']
        const t_colors = ['#FF005A', '#3888fa', '#40E0D0', '#E024F5', '#FFA07A']
        let lgd = [] // 那些指标的名字，functional_suitability...
        let srs = []  // 指标的值，数组，[{name:functional_suitability, xx:xxx,data:[80,80,80]},{}...]
        let xas = [] // x坐标，task-x-x
        let mn = 100 // 纵坐标最小的那个值
        for (const idx in qas[0]['versions']) {
          xas.push(qas[0]['versions'][idx]['tag'])
        }
        for (const idx in qas) {
          lgd.push(qas[idx]['name'])
          this.selecteds[qas[idx]['name']] = false
          let t_data = []
          for (const idx2 in qas[idx]['versions']) {
            t_data.push(qas[idx]['versions'][idx2]['score'])
            if (mn > qas[idx]['versions'][idx2]['score']) {
              mn = qas[idx]['versions'][idx2]['score']
            }
          }
          let sr = {
            name: qas[idx]['name'],
            itemStyle: {
              normal: {
                color: t_colors[idx],
                lineStyle: {
                  color: t_colors[idx],
                  width: 2
                }
              }
            },
            smooth: true,
            type: 'line',
            data: t_data,
            animationDuration: 2800,
            animationEasing: 'cubicInOut'
          }
          srs.push(sr)
        }
        if (mn > 20) {
          mn = Math.round(mn - 10)
        }
        this.$nextTick(() => {
          this.initChart(lgd, srs, xas, mn)
        })
      })
      .catch(function(error) {
        console.log(error)
      })
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    initChart(lgd, srs, xas, mn) {
      this.chart = echarts.init(this.$el, 'macarons')
      this.setOptions(lgd, srs, xas, mn)
    },
    setOptions(lgd, srs, xas, mn) {
      this.chart.setOption({
        title: {
          text: '历史分数',
        },
        dataZoom: [
          {
            show: true,
            realtime: true,
            start: 30,
            end: 70,
          },
          {
            type: 'inside',
            realtime: true,
            start: 30,
            end: 70,
          }
        ],
        xAxis: {
          data: xas,
          boundaryGap: false,
          axisTick: {
            show: false
          }
        },
        grid: {
          left: 10,
          right: 10,
          bottom: 20,
          top: 30,
          containLabel: true
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
          padding: [5, 10]
        },
        yAxis: {
          axisTick: {
            show: false
          },
          min: mn,
          max: 100
        },
        legend: {
          data: lgd
        },
        series: srs
      })
    },
    setSelected(sname) {
      console.log(sname)
      console.log(this.selecteds)
      let t_flag = false
      for (let key in this.selecteds) {
        console.log(key)
        if (key === sname) {
          this.selecteds[key] = true
          t_flag = true
        }
        else {
          this.selecteds[key] = false
        }
      }
      if (t_flag === false) {
        for (let key in this.selecteds) {
          this.selecteds[key] = true
        }
      }
      this.chart.setOption({
        legend: {
          selected: this.selecteds
        }
      })
    }
  }
}
</script>
