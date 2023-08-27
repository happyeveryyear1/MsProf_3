<template>
  <el-tree :data="data" :props="defaultProps" ></el-tree>
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
      data: null,
      defaultProps: {
        children: 'children',
        label: 'label'
      }
    }
  },
  mounted() {
      let queryData = {}
      queryData['proj_id'] = this.proj_id
      queryData['ver_tag'] = this.ver_tag
      getProject(queryData).then(response => {
        const cdata = response.data
        const qas = cdata['quality_aspects']
        let tdata = []
        for (const idx in qas) {
          const t_label_1 = qas[idx]['name'] + ': ' + qas[idx]['score']
          let tdata_1 = { label: t_label_1, children: [] }
          for (const idx2 in qas[idx]['metrics']) {
            const t_label_2 = qas[idx]['metrics'][idx2]['name'] + ': ' + qas[idx]['metrics'][idx2]['value']
            console.log(t_label_2)
            tdata_1.children.push({ label: t_label_2 })
          }
          tdata.push(tdata_1)
        }
        this.data = tdata
      })
      .catch(function(error) {
        console.log(error)
      })
  },
  methods: {
  }
}
</script>
