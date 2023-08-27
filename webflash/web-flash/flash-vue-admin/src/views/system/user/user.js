import { deleteUser, getList, addUser, editUser, remove, setRole, changeStatus } from '@/api/system/user'
import { list as deptList } from '@/api/system/dept'
import { parseTime } from '@/utils/index'
import { roleTreeListByIdUser } from '@/api/system/role'
// 权限判断指令
import permission from '@/directive/permission/index.js'
import { encrypt, decrypt } from '@/utils/aes'

export default {
  directives: { permission },
  data() {
    return {
      roleDialog: { // 角色
        id: '',
        visible: false,
        roles: [],
        roleTree: [],
        checkedRoleKeys: [],
        defaultProps: {
          id: 'id',
          label: 'name',
          children: 'children'
        }
      },
      formVisible: false,
      formTitle: '添加用户',
      deptTree: { // 部门
        show: false,
        data: [],
        defaultProps: {
          id: 'id',
          label: 'simplename',
          children: 'children'
        }
      },
      isAdd: true,
      pwdtext: '修改密码',
      isChangePwd: false,
      canChangePwd: false,
      form: {
        id: '',
        account: '', // 账户
        name: '', // 姓名
        birthday: '', // 生日
        sex: 1, // 性别
        email: '', // 邮箱
        password: '', // 密码
        rePassword: '', // 确定密码
        dept: '', // 部门
        status: true, // 账号状态
        deptid: 1, // 部门id
        deptName: '', // 部门名称
      },
      rules: {
        // 账号表单规则，必填，长度为3-15个字符
        account: [
          { required: true, message: '请输入登录账号', trigger: 'blur' },
          { min: 3, max: 15, message: '长度在 3 到 15 个字符', trigger: 'blur' },
          { pattern: /^[0-9A-Za-z]{3,15}$/, message: '只支持字母、数字',trigger: 'blur' },
        ],
        // 姓名表单规则，必填，长度为2-10个字符
        name: [
          { required: true,
            validator:(rule,value,callback)=>{
            if(value===''){
              callback(new Error('请输入姓名'))
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
        // 密码表单规则，必填，长度不小于5个字符
        password: [
          { required: true,
            validator:(rule,value,callback)=>{
            if(value===''){
              callback(new Error('请输入密码'))
            }else{
              if(this.form.account === 'intellitest'){
                if(value.length<8 || value.length>20){
                  callback(new Error('管理员账号密码长度在8到20个字符之间'))
                }else{
                  let reg = /^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[~!@&%$^\\(\\)#_<>])[0-9a-zA-Z~!@&%$^\\(\\)#_<>]{8,20}$/
                  if(reg.test(value)){
                    callback()
                  }else{
                    callback(new Error('必须包含字母、数字、特殊字符(~!@&%$^()#_<>)'))
                  }
                }
              }else{
                if(value.length<6 || value.length>20){
                  callback(new Error('长度在6到20个字符之间'))
                }else{
                  let reg = /^(?=.*[0-9])(?=.*[a-zA-Z])[0-9a-zA-Z~!@&%$^\\(\\)#_<>]{6,20}$/
                  if(reg.test(value)){
                    callback()
                  }else{
                    callback(new Error('必须包含数字和字母'))
                  }
                }
              }
            }
          },
          trigger:'blur'
          }
        ],
        // 确定密码表单规则，必填，要与密码框内容一致
        rePassword:[
          { required:true,message:'确认密码',trigger:'blur'},
          { validator:(rule,value,callback)=>{
            if(value===''){
              callback(new Error('请再次输入密码'))
            }else if(value!==this.form.password){
              callback(new Error('两次输入密码不一致'))
            }else{
              callback( )
            }
          },
          trigger:'blur'
          }
        ],
        // 邮箱表单规则，必填。
        email: [
          { required: true, message: '请输入email', trigger: 'blur' },
          { pattern:/^\w{3,15}\@\w+\.[a-z]{2,3}$/, message: '邮箱格式不对', trigger: 'blur' }
          
        ],
        phone: [
          { pattern:/^[1]([3-9])[0-9]{9}$/, message: '手机号码格式不对', trigger: 'blur' }
          
        ],
      },
      search_account: '',
      search_name: '',
      listQuery: {
        page: 1,
        limit: 20,
        account: undefined,
        name: undefined
      },
      total: 0,
      list: null,
      listLoading: true,
      selRow: {},
      switchStatus: {}, // 存储一组映射数据，id：status。用于显示列表switch状态。
      pwdType: 'password', // 密码框类型
      rePwdType: 'password',  // 确定密码框类型
    }
  },
  
  created() {
    this.init()
  },
  methods: {
    init() {
      deptList().then(response => {
        this.deptTree.data = response.data
      })
      this.fetchData()
    },
    fetchData() {
      this.listLoading = true
      if(this.search_account){
        this.listQuery.account  = encodeURI(this.search_account)
      }else{
        this.listQuery.account = ''
      }

      if(this.search_name){
        this.listQuery.name  = encodeURI(this.search_name)
      }else{
        this.listQuery.name = ''
      }
      getList(this.listQuery).then(response => { 
        this.list = response.data.records
        this.list.forEach(element => {
          if(element.status === 1){
            this.switchStatus[element.id] = true
          }else{
            this.switchStatus[element.id] = false
          }
        })
        this.listLoading = false
        this.total = response.data.total
        this.id = this.list[0].id
      })
    },
    search() { // 搜索查询相似的，也就是包含文字就行
      this.total = 0
      this.listQuery.page = 1
      this.list = []
      if(this.validAccountInput(this.search_account) && this.validNameInput(this.search_name)){
        this.fetchData()
      }else{
        if(!this.validAccountInput(this.search_account) && !this.validNameInput(this.search_name)){
          this.$notify.error({
            title: '错误',
            message: '查询输入不合法'
          })
        }else if(!this.validAccountInput(this.search_account)){
          this.$notify.error({
            title: '错误',
            message: '账号查询输入不合法'
          })
        }else{
          this.$notify.error({
            title: '错误',
            message: '姓名查询输入不合法'
          })
        }
      }
    },
    reset() {
      this.listQuery.account = ''
      this.listQuery.name = ''
      this.search_account = ''
      this.search_name = ''
      this.fetchData()
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleClose() {

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
        id: '',
        account: '',
        name: '',
        birthday: '',
        sex: 1,
        email: '',
        password: '',
        rePassword: '',
        dept: '',
        status: true,
        deptid: 1
      }
    },
    add() { // 添加
      this.resetForm()
      this.formTitle = '添加用户'
      this.formVisible = true
      this.isAdd = true
      this.isChangePwd = false
      this.canChangePwd = true
    },
    changeUserStatus(record) { // 切换启动状态switch
      changeStatus(record.id).then(response => {
        this.$message({
          message: '提交成功',
          type: 'success'
        })
        this.fetchData()
      })
    },
    validPasswd() { // 验证两个密码框是否一致（加了规则之后其实应该没必要有这个判断了）
      if (!this.isAdd) {
        return true
      }
      if (this.form.password !== this.form.rePassword) {
        return false
      }
      if (this.form.password === '' || this.form.rePassword === '') {
        return false
      }
      return true
    },
    saveUser() { // 保存
      var self = this
      this.$refs['form'].validate((valid) => {
        if (valid) {
          if (this.validPasswd()) {
            var form = self.form
            if (form.status === true) {
              // 启用
              form.status = 1
            } else {
              // 冻结
              form.status = 2
            }
            form.birthday = parseTime(form.birthday, '{y}-{m}-{d}')
            // form.createtime = parseTime(form.createtime)
            // form.id = Number(this.id) + Number(1)
            // console.log('user', form)
            this.pwdType = 'password'
            this.rePwdType = 'password'
            form.password = encrypt(form.password)
            form.rePassword = encrypt(form.rePassword)
            if(this.isAdd){
              addUser(form).then(response => {
                this.$message({
                  message: '提交成功',
                  type: 'success'
                })
                this.fetchData()
                this.formVisible = false
              }).catch(err => {
                this.$notify.error({
                  title: '错误',
                  message: err.response.data.message
                })
              })
            }else{
              editUser(form).then(response => {
                this.$message({
                  message: '提交成功',
                  type: 'success'
                })
                this.fetchData()
                this.formVisible = false
              }).catch(err => {
                this.$notify.error({
                  title: '错误',
                  message: err.response.data.message
                })
              })
            }        
          } else {
            this.$message({
              message: '提交失败',
              type: 'error'
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
        this.isAdd = false
        this.isChangePwd = true
        this.form = JSON.parse(JSON.stringify(this.selRow))
        this.form.status = this.selRow.statusName === '启用'
        this.form.password = ''
        this.formTitle = '修改用户'
        this.formVisible = true
        this.pwdtext = '修改密码'
        this.canChangePwd = false
      }
    }, 
    remove() { // 删除
      if (this.checkSel()) {
        var id = this.selRow.id
        
        this.$confirm('确定删除该记录?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          remove(id).then(response => {
            this.$message({
              message: '删除成功',
              type: 'success'
            })
            if(this.total % this.listQuery.limit === 1){
              if(this.listQuery.page !== 1){
                this.listQuery.page = this.listQuery.page - 1
              }
            }
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
    },
    handleNodeClick(data, node) { // 选择了部门
      this.form.deptid = data.id
      this.form.deptName = data.simplename
      this.deptTree.show = false
    },

    openRole() { // 角色分配
      if (this.checkSel()) {
        if(this.selRow.id === '1'){
          this.$alert('管理员用户默认拥有【超级管理员】权限', '提示', {
            confirmButtonText: '确定',
          })
        }else{
          roleTreeListByIdUser(this.selRow.id).then(response => {
            this.roleDialog.roles = response.data.treeData
           // this.roleDialog.roles.splice(0,1) // 剔除超级管理员
           for(let i=0;i<this.roleDialog.roles.length;i++){
              if(this.roleDialog.roles[i].id === "1"){
                this.roleDialog.roles.splice(i,1)
              }
            }
	   	    this.roleDialog.checkedRoleKeys = response.data.checkedIds
            this.roleDialog.visible = true
          })
        }  
      }
    },
    setRole() { // 保存角色
      var checkedRoleKeys = this.$refs.roleTree.getCheckedKeys()
      var roleIds = ''
      for (var index in checkedRoleKeys) {
        roleIds += checkedRoleKeys[index] + ','
      }
      var data = {
        userId: this.selRow.id,
        roleIds: roleIds
      }
      setRole(data).then(response => {
        this.roleDialog.visible = false
        this.fetchData()
        this.$message({
          message: '提交成功',
          type: 'success'
        })
      })
    },
    showPwd() { // 密码可视/隐藏
      if (this.pwdType === 'password') {
        this.pwdType = ''
      } else {
        this.pwdType = 'password'
      }
    },
    showRePwd() { // 确定密码可视/隐藏
      if (this.rePwdType === 'password') {
        this.rePwdType = ''
      } else {
        this.rePwdType = 'password'
      }
    },

    closeDialog(){ // 关闭dialog清除表单残留信息
      this.$refs['form'].resetFields()
    },

    changePWD(){
      if(this.pwdtext==='修改密码'){
        this.pwdtext = '取消修改密码'
        this.canChangePwd = true
      }else{
        this.pwdtext = '修改密码'
        this.canChangePwd = false
        this.form.password = '',
        this.form.rePassword = ''
      }
    },

    validAccountInput(str){
      if(str){
        var reg = /^(?!_)(?!.*?_$)[0-9A-Za-z]+$/
			  if (reg.test(str)) {
				  return true
			  } else {
          return false
        }
      }else{
        return true
      }
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
