import { getProList } from '@/api/project/projectList'
import { validateName } from "@/utils/validate"

export default {
  data() {
    return {
      list: null,
      total: 0,
      listLoading: true,
      search_projectName: '',
      listQuery: {
        page: 1,
        limit: 10,
        projectName: undefined
      },
      projs_c: [], // 存储勾选数据，传参给对比页面
    }
  },
  created() {
    this.init()
  },
  methods: {
    init(){
      this.fetchData()
    },

    fetchData() { // 获取项目列表
      this.listLoading = true
      if(this.search_projectName){
        this.listQuery.projectName = encodeURI(this.search_projectName)
      }else{
        this.listQuery.projectName = ''
      }
      getProList(this.listQuery).then(response => {
        this.list = response.data.records
        this.total = response.data.total
        this.listLoading = false
      })
    },

    search() {
      this.total = 0
      this.listQuery.page = 1
      this.list = []
      if(this.validInput(this.search_projectName)){
        this.fetchData()
      }else{
        this.$notify.error({
          title: '错误',
          message: '输入不合法'
        })
      }
    },

    reset(){
      this.listQuery.projectName = ''
      this.search_projectName = ''
      this.fetchData()
    },

    compare() { // 项目质量评估数据对比
      if(this.$refs.multipleTable.selection[1]){
        this.projs_c = []
        for(let i=0;i<this.$refs.multipleTable.selection.length;i++){
          this.projs_c.push(this.$refs.multipleTable.selection[i].id)
        }
        this.$router.push({ path:'/quality/comparison',query:{ projs: this.projs_c }})
      }else{
        this.$message({ // 没勾选两个项目
          message: '对比功能至少要勾选两个项目',
          type: 'warning'
        })
      }
    },

    fetchNext() {
      this.listQuery.page = this.listQuery.page + 1
      this.fetchData()
    },
    fetchPrev() {
      this.listQuery.page = this.listQuery.page - 1
      this.fetchData()
    },
    fetchPage(page) {
      this.listQuery.page = page
      this.fetchData()
    },
    changeSize(limit) {
      this.listQuery.limit = limit
      this.fetchData()
    },
    validInput(str){
      if(str){
			  if (validateName(str)) {
				  return true
			  } else {
          return false
        }
      }else{
        return true
      }
    }
  }
}