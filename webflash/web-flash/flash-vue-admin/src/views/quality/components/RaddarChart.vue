<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
import echarts from 'echarts'
require('echarts/theme/macarons') // echarts theme
import resize from './mixins/resize'
import { getProject } from '@/api/quality/qproject'

const animationDuration = 3000

export default {
  mixins: [resize],
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '350px'
    },
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
      chart: null
    }
  },
  mounted() {
      let queryData = {}
      queryData['proj_id'] = this.proj_id
      queryData['ver_tag'] = this.ver_tag
      getProject(queryData).then(response => {
        console.log(response)
        const cdata = response.data
        const proj_name = cdata['project_name']
        const qas = cdata['quality_aspects']
        const indicators = []
        const values = []
        for (const idx in qas) {
          indicators.push({ name: qas[idx]['name'], max: 100 })
          values.push(qas[idx]['score'])
        }
        this.$nextTick(() => {
          this.initChart(proj_name, indicators, values, this.handleSetLineChartData)
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
    handleSetLineChartData(qan) {
      this.$emit('handleSetLineChartData', qan)
    },
    initChart(proj_name, indicators, values, func) {
      this.chart = echarts.init(this.$el, 'macarons')

      this.chart.setOption({
        title: {
          text: '各维度评分'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { // 坐标轴指示器，坐标轴触发有效
            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
          }
        },
        radar: {
          radius: '66%',
          center: ['50%', '42%'],
          splitNumber: 8,
          splitArea: {
            areaStyle: {
              color: 'rgba(127,95,132,.3)',
              opacity: 1,
              shadowBlur: 45,
              shadowColor: 'rgba(0,0,0,.5)',
              shadowOffsetX: 0,
              shadowOffsetY: 15
            }
          },
          indicator: indicators,
          triggerEvent: true
        },
        legend: {
          left: 'center',
          bottom: '10',
          data: [proj_name]
        },
        series: [{
          type: 'radar',
          symbolSize: 0,
          areaStyle: {
            normal: {
              shadowBlur: 13,
              shadowColor: 'rgba(0,0,0,.2)',
              shadowOffsetX: 0,
              shadowOffsetY: 10,
              opacity: 1
            }
          },
          data: [
            {
              value: values,
              name: proj_name
            }
          ],
          animationDuration: animationDuration
        }]
      })

      this.chart.on('click', function(params) {
        func(params.name)
      })
    }
  }
}
</script>
