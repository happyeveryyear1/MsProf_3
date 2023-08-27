import { getAllProList, removePro, getProList, addPro, editPro, getApplicationName } from '@/api/project/projectList'
import { parseTime } from '@/utils/index'
// import { getList } from '@/api/system/user'
import { delProj, checkSonarId } from '@/api/quality/qproject'
import { validateGitAddress, validateName } from '@/utils/validate'
import permission from '@/directive/permission/index.js'

export default {
    directives: { permission },
    data() {
        const validateSonarId = (rule, value, callback) => {
            if (this.isAdd) {
                if (value) {
                    checkSonarId({ sonarId: value }).then(response => {
                        if (response.data.msg === 'success') {
                            callback()
                        } else {
                            callback(new Error('sonar ID不可用'))
                        }
                    }).catch(
                        //   err => {
                        //   callback(new Error('sonar ID检测异常，请重试'))
                        // }
                    )
                } else {
                    callback(new Error('sonar ID不能为空'))
                }
            } else {
                callback()
            }
        }

        const validateCaseAddress = (rule, value, callback) => {
            if (value) {
                if (!validateGitAddress(value)) {
                    callback(new Error('测试用例地址格式不对'))
                } else {
                    if (value.length > 256) {
                        callback(new Error('长度过长'))
                    } else {
                        callback()
                    }
                }
            } else {
                callback(new Error('测试用例地址不能为空'))
            }
        }

        const validDirAddress = (rule, value, callback) => {
            callback()
        }

        const validateProName = (rule, value, callback) => {
            if (value) {
                if (value.length > 20 || value.length < 3) {
                    callback(new Error('项目名的长度在3-20个字符之间'))
                } else {
                    if (!validateName(value)) {
                        callback(new Error('项目名只能含有汉字、数字、字母、小数点、下划线且不能以下划线开头和结尾'))
                    } else {
                        callback()
                    }
                }
            } else {
                callback(new Error('项目名不能为空'))
            }
        }
        return {
            url: '',
            formVisible: false,
            formTitle: '添加项目总表',
            isAdd: false,
            dataNum: 0, // 用来存储数据库该表所有数据条数,方便对比项目名是否重复
            projectLeaders: '', // 存储项目负责人
            form: {
                id: '',
                projectName: '', // 项目名
                projectIntroduction: '', // 项目简介
                // testcaseAddress: '', // 测试用例git地址
                harAddress: '', // Har地址
                swaggerAddress: '', // Swagger地址
                projectLeader: '', // 项目负责人
                sonarId: '', // 项目 sonarID
                activities: '0', // 关联测试活动数
                applicationName: 'WeblogicTest', // 应用名
                deployPlan: '', // 部署计划
                systemName: '生产系统' // 所属系统
            },

            search_projectName: '', // 项目名搜索框绑定的值
            listQuery: { // 列表查询条件
                page: 1,
                limit: 10,
                projectName: undefined,
                systemName: undefined
            },
            total: 0, // 数据数
            list: null, // 列表数据源
            listLoading: true,
            selRow: {},
            disabledInput: false, // 项目名/sonarID文本框disable
            isSaving: false, // 控制保存按钮的可用性。但在保存中时，该按钮不可单击。
            applicationNameList: [], // config文件中的applicationName和deployPlan

            formRules: {
                projectName: [
                    { required: true, trigger: 'blur', validator: validateProName }
                ],
                systemName: [
                    { required: true, message: '系统不能为空', trigger: 'change' }
                ],
                applicationName: [
                    { required: true, message: '应用名不能为空', trigger: 'change' }
                ],
                projectLeader: [
                    { required: true, message: '项目负责人不能为空', trigger: 'change' }
                ],
                sonarId: [
                    { required: true, trigger: 'blur', validator: validateSonarId }
                ],
                testcaseAddress: [
                    { required: false, trigger: 'blur', validator: validateCaseAddress }
                ],
                harAddress: [
                    { required: true, trigger: 'blur', validator: validDirAddress }
                ],
                swaggerAddress: [
                    { required: true, trigger: 'blur', validator: validDirAddress }
                ]
            }
        }
    },

    created() {
        this.init()
    },

    methods: {
        init() {
            // this.form.projectLeader = this.$store.state.user.profile.name
            this.fetchData()
            // getList({ page: 1, limit: 10000 }).then(response => { // 获取所有用户数据，获得用户列表projectLeaders
            //   this.projectLeaders = response.data.records
            //   // this.fetchData()
            // })
            getApplicationName().then(response => { // 获取config文件中的应用名和部署计划
                const nameList = response.data
                let name = {}
                this.applicationNameList = []
                for (let i = 0; i < nameList.length; i++) {
                    name = {}
                    name.applicationName = nameList[i].split(',')[0]
                    name.deployPlan = nameList[i].split(',')[1]
                    this.applicationNameList.push(name)
                }
            })
        },

        fetchData() {
            this.listLoading = true
            if (this.search_projectName) {
                this.listQuery.projectName = encodeURI(this.search_projectName)
            } else {
                this.listQuery.projectName = ''
            }
            getProList(this.listQuery).then(response => {
                this.list = response.data.records
                this.total = response.data.total
                // this.list.forEach(element => {
                //   for(let i=0;i<this.projectLeaders.length;i++){ // projectLeader对应的是id，用这个for循环改为name
                //     if(element.projectLeader === this.projectLeaders[i].id){
                //       element.projectLeader = this.projectLeaders[i].name
                //       break
                //     }
                //   }
                // })
                this.listLoading = false
            })

            getAllProList().then(response => { // 获取所有项目数据listAll以及数据量dataNum，用于检测项目名重复
                this.listAll = response.data
                this.dataNum = this.listAll.length
            })
        },

        search() {
            this.total = 0
            this.listQuery.page = 1
            this.list = []
            if (this.validInput(this.search_projectName)) {
                this.fetchData()
            } else {
                this.$notify.error({
                    title: '错误',
                    message: '输入不合法'
                })
            }
        },

        reset() {
            this.listQuery.projectName = ''
            this.listQuery.systemName = ''
            this.search_projectName = ''
            this.fetchData()
        },

        handleFilter() {
            this.listQuery.page = 1
            this.fetchData()
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
                projectName: '',
                projectIntroduction: '',
                testcaseAddress: '',
                id: '',
                sonarId: '',
                projectLeader: this.$store.state.user.profile.name,
                activities: '0',
                applicationName: '',
                deployPlan: '',
                systemName: ''
            }
        },

        add() {
            this.resetForm()
            this.formTitle = '添加项目信息'
            this.formVisible = true
            this.isAdd = true
            this.disabledInput = false
        },

        save() {
            // getAllProList().then(response => { // 获取所有项目数，方便检测是否重名
            //   this.listAll = response.data
            //   this.dataNum = response.data.length
            //   // 判断项目是否存在，存在则发出提示信息：项目已存在。若不存在就提交给后台。
            //   var i = 0
            //   while (i !== this.dataNum && this.listAll[i].projectName !== this.form.projectName) {
            //     i++
            //   }
            //   if (i < this.dataNum && this.form.id !== this.listAll[i].id) {
            //     this.$notify.error({
            //       title: '错误',
            //       message: '项目名已被使用，请重新输入'
            //     })
            //   } else {
            this.$refs.form.validate((valid) => {
                // let leader = 0
                // for(let i=0;i<this.projectLeaders.length;i++){ // 将projectLeader.name改成id
                //   if(this.form.projectLeader===this.projectLeaders[i].name){
                //     leader = this.projectLeaders[i].id
                //     break
                //   }
                // }
                for (let j = 0; j < this.applicationNameList.length; j++) {
                    if (this.form.applicationName === this.applicationNameList[j].applicationName) {
                        this.form.deployPlan = this.applicationNameList[j].deployPlan
                    }
                }
                if (valid) {
                    this.isSaving = true
                    if (this.isAdd) {
                        addPro({
                            projectName: this.form.projectName,
                            projectIntroduction: this.form.projectIntroduction,
                            testcaseAddress: this.form.testcaseAddress,
                            harAddress: this.form.harAddress,
                            // TODO: this.form, this.$ref.form
                            swaggerAddress: this.form.swaggerAddress,
                            id: this.form.id,
                            sonarId: this.form.sonarId,
                            projectLeader: this.$store.state.user.profile.id,
                            createTime: parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}'),
                            activities: this.form.activities,
                            applicationName: this.form.applicationName,
                            deployPlan: this.form.deployPlan,
                            systemName: this.form.systemName
                        }).then(response => {
                            // if (!this.disabledInput) { // 顺便创建质量模块的项目
                            //     // newProj({
                            //     //     proj_name: this.form.projectName,
                            //     //     proj_id_name: response.data,
                            //     //     proj_sonar_key: this.form.sonarId,
                            //     //     proj_language: 'java',
                            //     //     proj_ptype: 'web_backend'
                            //     // }).then(response => {
                            //     //     this.$message({
                            //     //         message: this.$t('common.optionSuccess'),
                            //     //         type: 'success'
                            //     //     })
                            //     //     this.fetchData()
                            //     //     this.formVisible = false
                            //     //     this.isSaving = false
                            //     // }).catch(
                            //     //     //   err => { // 若质量模块项目保存异常，则报错并删除新增项目
                            //     //     //   removePro(response.data)
                            //     //     //   this.$notify.error({
                            //     //     //     title: '错误',
                            //     //     //     message: '项目保存出错，请重试！'
                            //     //     //   })
                            //     //     //   this.isSaving = false
                            //     //     // }
                            //     // )
                            // } else {
                            //     this.$message({
                            //         message: this.$t('common.optionSuccess'),
                            //         type: 'success'
                            //     })
                            //     this.fetchData()
                            //     this.formVisible = false
                            //     this.isSaving = false
                            // }
                            this.$message({
                                message: this.$t('common.optionSuccess'),
                                type: 'success'
                            })
                            this.fetchData()
                            this.formVisible = false
                            this.isSaving = false
                        }).catch(err => {
                            if (err.msg === '项目名已被使用，请重新输入') {
                                //
                            } else {
                                this.$notify.error({
                                    title: '错误',
                                    message: '项目保存出错，请重试！'
                                })
                            }
                            this.isSaving = false
                        })
                    } else {
                        editPro({
                            projectName: this.form.projectName,
                            projectIntroduction: this.form.projectIntroduction,
                            testcaseAddress: this.form.testcaseAddress,
                            id: this.form.id,
                            sonarId: this.form.sonarId,
                            projectLeader: this.selRow.projectLeader,
                            createTime: parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}'),
                            activities: this.form.activities,
                            applicationName: this.form.applicationName,
                            deployPlan: this.form.deployPlan,
                            systemName: this.form.systemName
                        }).then(response => {
                            this.$message({
                                message: this.$t('common.optionSuccess'),
                                type: 'success'
                            })
                            this.fetchData()
                            this.formVisible = false
                            this.isSaving = false
                        }).catch(
                            //   err => {
                            //   this.$notify.error({
                            //     title: '错误',
                            //     message: '项目保存出错，请重试！'
                            //   })
                            //   this.isSaving = false
                            // }
                        )
                    }
                } else {
                    return false
                }
            })
            //   }
            // })
        },

        checkSel() { // 选中项目
            if (this.selRow && this.selRow.id) {
                return true
            }
            this.$message({
                message: this.$t('common.mustSelectOne'),
                type: 'warning'
            })
            return false
        },

        edit() {
            if (this.checkSel()) {
                this.disabledInput = true
                this.isAdd = false
                this.form = JSON.parse(JSON.stringify(this.selRow))
                this.form.projectLeader = this.selRow.leaderName
                this.formTitle = '编辑项目信息'
                this.formVisible = true
            }
        },

        remove() {
            if (this.checkSel()) {
                var id = this.selRow.id
                this.$confirm(this.$t('common.deleteConfirm'), this.$t('common.tooltip'), {
                    confirmButtonText: this.$t('button.submit'),
                    cancelButtonText: this.$t('button.cancel'),
                    type: 'warning'
                }).then(() => {
                    // FIXME: 删除项目
                    delProj({
                        projectName: id
                    }).then(response => { // 质量模块先删，若无异常则删除数据库项目
                        removePro(id).then(response => {
                            this.$message({
                                message: this.$t('common.optionSuccess'),
                                type: 'success'
                            })
                            if (this.total % this.listQuery.limit === 1) {
                                if (this.listQuery.page !== 1) {
                                    this.listQuery.page = this.listQuery.page - 1
                                }
                            }
                            this.fetchData()
                        }).catch(err => {
                            this.$notify.error({
                                title: '错误',
                                message: err
                            })
                        })
                    }).catch(response => {
                        this.$notify.error({
                            title: '错误',
                            message: '项目删除出错，请重试！'
                        })
                    })
                }).catch(() => {
                })
            }
        },

        closeDialog() { // 清除表单验证残留信息
            this.$refs['form'].resetFields()
        },

        viewActivity(projectName, id) { // 从测试活动数进入测试活动页面
            this.$router.push({ path: '/project/testActivity', query: { projectId: id }})
        },

        system_unique(arr) { // 系统去重
            const res = new Map()
            return arr.filter((arr) => !res.has(arr.systemName) && res.set(arr.systemName, 1))
        },

        validInput(str) {
            if (str) {
                if (validateName(str)) {
                    return true
                } else {
                    return false
                }
            } else {
                return true
            }
        }
    }
}
