import { remove, getList, addRole, editRole, savePermissons } from '@/api/system/role'
import { list as getDeptList } from '@/api/system/dept'
import { menuTreeListByRoleId } from '@/api/system/menu'

export default {
  data() {
    return {
      formVisible: false,
      formTitle: '添加角色',
      deptList: [],  // 部门数据
      // roleList: [],
      isAdd: true,
      checkedPermissionKeys: [], // 角色对应勾选的权限id
      permissons: [], // 权限配置页面tree的数据源
      defaultProps: { // 默认参数？
        id: 'id',
        label: 'name',
        children: 'children'
      },
      permissonVisible: false,
      deptTree: {
        show: false,
        defaultProps: {
          id: 'id',
          label: 'simplename',
          children: 'children'
        }
      },
      roleTree: {
        show: false,
        defaultProps: {
          id: 'id',
          label: 'name',
          children: 'children'
        }
      },

      form: {
        tips: '', // 角色编码
        name: '', // 角色名
        deptid: '', // 部门id
        pid: 0, // 上级角色id
        id: '',
        // version: '', // 多余参数
        deptName: '', // 部门
        pName: '', // 上级角色名
        // num: 1  // 排序用的
      },
      rules: {
        //角色编码表单规则，必填，长度3-20个字符
        tips: [
          { required: true, message: '请输入角色编码', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
          { pattern: /^[0-9A-Za-z]{3,20}$/, message: '只支持字母、数字',trigger: 'blur' }
        ],
        // 角色名表单规则，必填，长度2-20个字符
        name: [
          { required: true,
            validator:(rule,value,callback)=>{
            if(value===''){
              callback(new Error('请输入角色名称'))
            }else{
              if(value.length<2 || value.length>10){
                callback(new Error('长度在2到10个字符之间'))
              }else{
                let reg = /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/
                if(reg.test(value)){
                  callback()
                }else{
                  callback(new Error('只支持中文、英文、数字和下划线'))
                }
              }
            }
          },
          trigger:'blur'
          }
        ],
      },
      search_name: '',
      listQuery: {
        name: undefined
      },
      total: 0,
      list: null,
      listLoading: true,
      selRow: {},
      buttonLoad: false
    }
  },
  filters: {
    statusFilter(status) {
      const statusMap = {
        published: 'success',
        draft: 'gray',
        deleted: 'danger'
      }
      return statusMap[status]
    }
  },
  created() {
    this.init()
  },
  methods: {
    init() {
      getDeptList().then(response => {
        this.deptList = response.data
      })
      this.fetchData()
    },
    fetchData() {
      this.listLoading = true
      if(this.search_name){
        this.listQuery.name  = encodeURI(this.search_name)
      }else{
        this.listQuery.name = ''
      }
      getList(this.listQuery).then(response => {
        console.log(response.data)
        this.list = response.data
        this.listLoading = false
        this.total = response.data.total
      })
    },
    search() { // 搜索查询相似的，也就是包含文字就行
      this.list = []
      if(this.validNameInput(this.search_name)){
        this.fetchData()
      }else{
        this.$notify.error({
          title: '错误',
          message: '输入不合法'
        })
      }
    },
    reset() {
      this.listQuery.name = ''
      this.search_name = ''
      this.fetchData()
    },
    handleFilter() {
      this.getList()
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
    handleCurrentChange(currentRow, oldCurrentRow) {
      this.selRow = currentRow
    },
    resetForm() {
      this.form = {
        tips: '',
        name: '',
        deptid: '',
        pid: 0,
        id: '',
        version: '',
        deptName: '',
        pName: '',
        // num: 1

      }
    },
    add() { // 添加
      this.resetForm()
      this.formTitle = '添加角色'
      this.formVisible = true
      this.isAdd = true
    },
    save() { // 保存
      this.$refs['form'].validate((valid) => {
        if (valid) {
          if(this.isAdd){
            addRole({
              id: this.form.id,
              // num: this.form.num,
              deptid: this.form.deptid,
              pid: this.form.pid,
              name: this.form.name,
              tips: this.form.tips
            }).then(response => {
              this.$message({
                message: '提交成功',
                type: 'success'
              })
              this.fetchData()
              this.formVisible = false
            })
          }else{
            editRole({
              id: this.form.id,
              // num: this.form.num,
              deptid: this.form.deptid,
              pid: this.form.pid,
              name: this.form.name,
              tips: this.form.tips
            }).then(response => {
              this.$message({
                message: '提交成功',
                type: 'success'
              })
              this.fetchData()
              this.formVisible = false
            })
          }




        } else {
          console.log('error submit!!')
          return false
        }
      })
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
    edit() { // 编辑
      if (this.checkSel()) {
        if(this.selRow.id === '1'){
          this.$alert('【超级管理员】不允许进行编辑', '提示', {
            confirmButtonText: '确定',
          })
        }else{
          this.isAdd = false
          this.form = this.selRow
          this.form.status = this.selRow.statusName === '启用'
          this.form.password = ''
          this.formTitle = '修改角色'
          this.formVisible = true
        }
      }
    },
    remove() { // 删除
      if (this.checkSel()) {
        if(this.selRow.id === '1'){
          this.$alert('【超级管理员】不允许删除', '提示', {
            confirmButtonText: '确定',
          })
        }else{
          const id = this.selRow.id
          this.$confirm('确定删除该记录?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            remove(id).then(response => {
              this.$message({
                message: '提交成功',
                type: 'success'
              })
              this.fetchData()
            }).catch(err => {
              this.$notify.error({
                title: '错误',
                message: err.msg
              })
            })
          }).catch(() => {
          })
        }
      }
    },
    openPermissions() { // 权限配置
      if (this.checkSel()) {
        if(this.selRow.id === '1'){
          this.$alert('【超级管理员】拥有全部权限，不允许更改', '提示', {
            confirmButtonText: '确定',
          })
        }else{
          menuTreeListByRoleId(this.selRow.id).then(response => {
            // this.permissons = response.data.treeData
            this.permissons = []
            let treeData = response.data.treeData
            treeData.forEach(element => { // 有些菜单不必要显示出来，过滤掉
              if(element.id==='1' || element.id === "3" || element.id==='71' || element.id==='78' || element.id==='84'){ // 只显示系统管理、运维管理、项目、性能分析、质量评估菜单
                if(element.id === '1'){ // 系统管理只显示前四个菜单
                  let childrenData = []
                  for(let i=0;i<4;i++){ 
                    if(i !== 2){
                      childrenData.push(element.children[i])
                    }
                  }
                  element.children = childrenData
                }
                if(element.id === '3'){ // 运维管理只显示业务日志和登录日志
                  let childrenData1 = []
                  childrenData1.push(element.children[0])
                  childrenData1.push(element.children[1])
                  element.children = childrenData1
                }
                this.permissons.push(element)
              }
            });
            this.checkedPermissionKeys = response.data.checkedIds
            this.permissonVisible = true
          })
        }
      }
    },
    savePermissions() { // 保存权限
      this.buttonLoad = true
      const checkedNodes = this.$refs.permissonTree.getCheckedNodes(false, true)
      let menuIds = ''
      for (var index in checkedNodes) {
        menuIds += checkedNodes[index].id + ','
      }
      const data = {
        roleId: this.selRow.id,
        permissions: menuIds
      }
      console.log('permissions', data)
      savePermissons(data).then(response => {
        this.permissonVisible = false
        this.buttonLoad = false
        this.$message({
          message: '提交成功',
          type: 'success'
        })
      })
    },
    handleDeptNodeClick(data, node) { // 单击部门文本框
      this.form.deptid = data.id
      this.form.deptName = data.simplename
      this.deptTree.show = false
    },
    handleRoleNodeClick(data, node) { // 单击上级角色文本框
      this.form.pid = data.id
      this.form.pName = data.name
      this.roleTree.show = false
    },
    validNameInput(str){
      if(str){
        var reg = /^(?!_)(?!.*?_$)[a-zA-Z0-9_\u4e00-\u9fa5]+$/
			  if (reg.test(str)) {
				  return true
			  } else {
          return false
        }
      }else{
        return true
      }
    },

  }
}
