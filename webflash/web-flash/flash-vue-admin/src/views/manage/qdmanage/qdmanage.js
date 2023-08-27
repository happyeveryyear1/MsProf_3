let id = 1000
export default {
  data() {
    const data = [{
      id: 1,
      label: '质量数据管理',
      children: [{
        id: 2,
        label: 'Functional Suitability',
        children: [{
          id: 5,
          label: 'Functional completeness'
        }, {
          id: 6,
          label: 'Functional correctness'
        }]
      }, {
        id: 3,
        label: 'Performance efficiency',
        children: [{
          id: 7,
          label: 'Time- behavior'
        }, {
          id: 8,
          label: 'Resource utilization'
        }]
      }, {
        id: 4,
        label: 'Maintainability'
      }]
    }]
    return {
      filterText: '',
      data: JSON.parse(JSON.stringify(data))
    }
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val)
    }
  },

  methods: {
    filterNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    append(data) {
      const newChild = { id: id++, label: 'testtest', children: [] }
      if (!data.children) {
        this.$set(data, 'children', [])
      }
      data.children.push(newChild)
    },

    remove(node, data) {
      const parent = node.parent
      const children = parent.data.children || parent.data
      const index = children.findIndex(d => d.id === data.id)
      children.splice(index, 1)
    },

    qualityDataConf() {
      this.$router.push({ path: 'qdconf' })
    },

    qualityRepConf() {
      this.$router.push({ path: '/16_QualityRepConf' })
    }
  }
}
