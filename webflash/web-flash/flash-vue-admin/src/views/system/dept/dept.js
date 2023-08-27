import treeTable from '@/components/TreeTable'
import { list, addDept, editDept, del } from '@/api/system/dept'

export default {
  name: 'customTreeTableDemo',
  components: { treeTable },
  data() {
    return {
      expandAll: true, // 表格展开全部
      data: [], // 表格数据源
      formVisible: false, 
      formTitle: '',
      isAdd: false,

      showTree: false, // 控制父部门的tree的可视
      defaultProps: {
        id: 'id',
        label: 'simplename',
        children: 'children'
      },
      form: {
        id: '',
        simplename: '', // 名称
        fullname: '', // 全称
        pid: '',  // 父部门id
        num: '',  // 排序
        tips: ''  // 这个字段好像没用到
      },
      rules: {
        // 名称的表单规则，必填，长度在3到20个字符
        simplename: [ 
          { required: true, message: '请输入菜单名称', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        // 全称的表单规则，必填，长度在2到20个字符
        fullname: [
          { required: true, message: '请输入编码', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        // 排序的表单规则，必填
        num: [
          { required: true, message: '请输入排序', trigger: 'blur' }
        ]
      }

    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.listLoading = true
      list().then(response => {
        let resData = response.data
        resData.sort(function(a , b){ // 排序
          return a.num - b.num
        })
        resData.forEach(element => { // 给children排序
          if(element.children.length){
            this.sortNum(element)
          }
        })
        this.data = resData
        this.data.forEach(element => { // 层级
          element.level = element.fullname
          if(element.children.length){
            for(let i = 0;i<element.children.length;i++){
              element.children[i].level = element.fullname+' / '+element.children[i].fullname
              if(element.children[i].children.length){
                for(let j = 0;j<element.children[i].children.length;j++){
                  element.children[i].children[j].level = element.children[i].level+' / ' + element.children[i].children[j].fullname
                  if(element.children[i].children[j].children.length){
                    for(let k = 0;k<element.children[i].children[j].children.length;k++){
                      element.children[i].children[j].children[k].level =element.children[i].children[j].level + ' / ' +element.children[i].children[j].children[k].fullname
                    }
                  }
                }
              }
            }
          }
        });
        this.listLoading = false
      })
    },
    sortNum(node){ // children排序
      node.children.sort(function(a , b){
        return a.num - b.num
      })
      for(let i=0;i<node.children.length;i++){ // 如果node的children还有children，继续排序
        this.sortNum(node.children[i])
      }   
    },

    handleNodeClick(data, node) {
      console.log(data)
      this.form.pid = data.id
      this.form.pname = data.simplename
      this.showTree = false
    },
    checkSel() {
      if (this.selRow && this.selRow.id) {
        return true
      }
      this.$message({
        message: '请选中操作项',
        type: 'warning'
      })
      return false
    },
    add() { // 添加
      this.form = {}
      this.formTitle = '添加部门'
      this.formVisible = true
      this.isAdd = true
    },
    save() { // 保存
      var self = this
      this.$refs['form'].validate((valid) => {
        if (valid) {
          console.log('form', self.form)
          const menuData = {id:self.form.id,simplename:self.form.simplename,fullname:self.form.fullname,num:self.form.num,pid:self.form.pid,tips:self.form.tips}//self.form
          menuData.parent = null
          if(this.isAdd){
            addDept(menuData).then(response => {
              console.log(response)
              this.$message({
                message: '提交成功',
                type: 'success'
              })
              self.fetchData()
              self.formVisible = false
            })
          }else{
            editDept(menuData).then(response => {
              console.log(response)
              this.$message({
                message: '提交成功',
                type: 'success'
              })
              self.fetchData()
              self.formVisible = false
            })
          }
        } else {
          return false
        }
      })
    },
    edit(row) { // 编辑
      this.form = row

      if (row.parent) {
        this.form.pid = row.parent.id
        this.form.pname = row.parent.simplename
      }
      this.formTitle = '编辑部门'
      this.formVisible = true
      this.isAdd = false
    },
    remove(row) { // 删除
      this.$confirm('确定删除该记录?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        del(row.id).then(response => {
          this.$message({
            message: '删除成功',
            type: 'success'
          })
          this.fetchData()
        })
      })
    },

    closeDialog(){ // 关闭dialog清除表单残留信息
      this.showTree = false
      this.$refs['form'].resetFields()
    }
  }
}
