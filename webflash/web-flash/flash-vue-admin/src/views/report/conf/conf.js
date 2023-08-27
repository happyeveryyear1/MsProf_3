import { getList } from '@/api/project/testActivity'
export default {
  data() {
    const objectiveData = [{
      id: 1,
      label: 'Functional Suitability',
      children: [{
        id: 4,
        label: 'Functional completeness'
      }, {
        id: 5,
        label: 'Functional correctness'
      }]
    }, {
      id: 2,
      label: 'Performance efficiency',
      children: [{
        id: 6,
        label: 'Time- behavior'
      }, {
        id: 7,
        label: 'Resource utilization'
      }]
    }, {
      id: 3,
      label: 'Maintainability'
    }]
    return {
      objectiveData: JSON.parse(JSON.stringify(objectiveData)),
      projectList: [],
      versionList: [],
      verSel: true,
      form: {
        projectName: '',
        versionNum: '',
        textarea: '',
        w: []
      }
    }
  },
  created() {
    this.init()
  },
  methods: {
    init() {
      this.fetchData()
    },
    fetchData() {
      this.listLoading = true
      getList({ page: 1, limit: 1000 }).then(response => { // 查询t_pro_test_info的项目版本信息
        this.projectList = response.data.records
        this.listLoading = false
      })
    },
    name_unique(arr) { // 项目名去重
      const res = new Map()
      return arr.filter((arr) => !res.has(arr.projectName) && res.set(arr.projectName, 1))
    },
    version_unique(arr) { // 版本号去重
      const res = new Map()
      return arr.filter((arr) => !res.has(arr.versionNum) && res.set(arr.versionNum, 1))
    },
    editPro() {
      this.verSel = false
      this.versionList = []
      this.form.versionNum = ''
      this.projectList.forEach(element => {
        if (element.projectName === this.form.projectName) {
          this.versionList.push(element)
        }
      })
    },
    generateWeb() {
    },
    generatePDF() {
    }
  }
}
