<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
import echarts from 'echarts'
require('echarts/theme/macarons') // echarts theme
import resize from './mixins/resize'
import { cmp } from '@/api/quality/qproject'

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
    projs: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      chart: null
    }
  },
  mounted() {
    cmp({"projs": this.projs}).then(response => {
        console.log(response.data)
        const cdata = response.data
        const projs = cdata['projects']
        const indicators = []
        const values = []
        const projs_name = []
        for (const idx in projs[0]["quality_aspects"]) {
          indicators.push({ name: projs[0]["quality_aspects"][idx]["name"], max: 100 })
        }
        for (const idx in projs){
          projs_name.push(projs[idx]["name"])
          const qas = projs[idx]["quality_aspects"]
          let v = []
          for (const idx in qas) {    
            v.push(qas[idx]['score'])
          }
          values.push({value:v,name:projs[idx]["name"]})
        }
        console.log(projs_name)
        console.log(indicators)
        console.log(values)     
        this.$nextTick(() => {
          this.initChart(projs_name, indicators, values)
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
    initChart(projs_name, indicators, values) {
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
          data: projs_name
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
          data: values,
          animationDuration: animationDuration
        }]
      })
    }
  }
}
</script>
