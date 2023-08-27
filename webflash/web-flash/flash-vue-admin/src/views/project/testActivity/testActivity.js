import { getAllActivityList, removeActivity, getActivityList, addActivity, editActivity } from '@/api/project/testActivity'
import { getAllProList } from '@/api/project/projectList'
import { parseTime } from '@/utils/index'
import { validateName } from '@/utils/validate'
import permission from '@/directive/permission/index.js'

export default {
    directives: { permission },
    data() {
        const validateActivityName = (rule, value, callback) => {
            if (value) {
                if (value.length > 20 || value.length < 3) {
                    callback(new Error('分析活动名的长度在3-20个字符之间'))
                } else {
                    if (!validateName(value)) {
                        callback(new Error('分析活动名只能含有汉字、数字、字母、小数点、下划线且不能以下划线开头和结尾'))
                    } else {
                        callback()
                    }
                }
            } else {
                callback(new Error('分析活动名不能为空'))
            }
        }
        return {
            formVisible: false,
            formTitle: '添加项目分析活动信息',
            proList: [], // 用于接收项目总表信息
            isAdd: true,
            form: {
                id: '',
                projectName: '', // 项目名
                testactivityName: '', // 测试活动名
                testactivityIntroduction: '', // 测试活动简介
                tasks: '0'
            },
            projectName: '', // 接收路由项目名
            projectId: '', // 接收路由项目id
            search_testactivityName: '', // 跟查询条件的“测试活动名”绑定
            listQuery: {
                page: 1,
                limit: 10,
                projectName: undefined,
                testactivityName: undefined
            },
            total: 0,
            dataNum: 0, // 所有测试活动总数目
            list: null, // 列表数据源
            listAll: null, // 所有测试活动列表
            listLoading: true,
            selRow: {},
            isSaving: false, // 控制保存按钮的可用性。但在保存中时，该按钮不可单击。

            formRules: {
                testactivityName: [
                    { required: true, trigger: 'blur', validator: validateActivityName }
                ]
            }
        }
    },
    created() {
        this.init()
    },
    methods: {
        init() {
            this.projectId = this.$route.query.projectId
            this.listQuery.projectName = this.projectId // 查询用的是项目id
            this.fetchData()
            getAllProList().then(response => { // 先获取项目总表信息
                this.proList = response.data
                for (let i = 0; i < this.proList.length; i++) {
                    if (this.projectId === this.proList[i].id) {
                        this.projectName = this.proList[i].projectName
                    }
                }
            })
        },

        fetchData() {
            this.listLoading = true
            getAllActivityList().then(response => { // 先获取所有测试活动列表
                this.listAll = response.data
                this.dataNum = this.listAll.length
            })
            if (this.search_testactivityName) {
                this.listQuery.testactivityName = encodeURI(this.search_testactivityName)
            } else {
                this.listQuery.testactivityName = ''
            }
            getActivityList(this.listQuery).then(response => {
                this.list = response.data.records
                this.total = response.data.total
                // this.list.forEach(element => {
                // for(let i=0;i<this.proList.length;i++){ // 将projectname.id都替换成name
                //   if(element.projectName === this.proList[i].id){
                //     element.projectName = this.proList[i].projectName
                //     break
                //   }
                // }
                // })
                this.listLoading = false
            })
        },

        search() { // 搜索查询相似的，也就是包含文字就行
            this.total = 0
            this.listQuery.page = 1
            this.list = []
            if (this.validInput(this.search_testactivityName)) {
                this.fetchData()
            } else {
                this.$notify.error({
                    title: '错误',
                    message: '输入不合法'
                })
            }
        },

        reset() {
            this.listQuery.testactivityName = ''
            this.search_testactivityName = ''
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
                projectName: '', // 项目名
                testactivityName: '', // 测试活动名
                testactivityIntroduction: '', // 简介
                id: '',
                tasks: '0'
            }
        },

        add() {
            this.resetForm()
            if (this.projectName) {
                this.form.projectName = this.projectName
            }
            this.formTitle = '添加项目分析活动信息'
            this.formVisible = true
            this.isAdd = true
        },
        save() {
            this.$refs['form'].validate((valid) => {
                if (valid) {
                    this.isSaving = true
                    let proID
                    for (let i = 0; i < this.proList.length; i++) {
                        if (this.form.projectName === this.proList[i].projectName) {
                            proID = this.proList[i].id
                            break
                        }
                    }
                    if (this.isAdd) {
                        addActivity({
                            projectName: proID,
                            testactivityName: this.form.testactivityName,
                            testactivityIntroduction: this.form.testactivityIntroduction,
                            id: this.form.id,
                            createTime: parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}'),
                            tasks: this.form.tasks
                        }).then(response => {
                            this.$message({
                                message: this.$t('common.optionSuccess'),
                                type: 'success'
                            })
                            this.isSaving = false
                            this.fetchData()
                            this.formVisible = false
                        }).catch(err => {
                            if (err.msg === '分析活动名已被使用，请重新输入') {
                                // pass
                            } else {
                                this.$notify.error({
                                    title: '错误',
                                    message: '分析活动保存出错，请重试！'
                                })
                            }
                            this.isSaving = false
                        })
                    } else {
                        editActivity({
                            projectName: proID,
                            testactivityName: this.form.testactivityName,
                            testactivityIntroduction: this.form.testactivityIntroduction,
                            id: this.form.id,
                            createTime: parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}'),
                            tasks: this.form.tasks
                        }).then(response => {
                            this.$message({
                                message: this.$t('common.optionSuccess'),
                                type: 'success'
                            })
                            this.isSaving = false
                            this.fetchData()
                            this.formVisible = false
                        }).catch(err => {
                            if (err.msg === '分析活动名已被使用，请重新输入') {
                                // pass
                            } else {
                                this.$notify.error({
                                    title: '错误',
                                    message: '分析活动保存出错，请重试！'
                                })
                            }
                            this.isSaving = false
                        })
                    }
                } else {
                    return false
                }
            })
        },

        checkSel() {
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
                this.isAdd = false
                this.form = JSON.parse(JSON.stringify(this.selRow))
                this.form.projectName = this.selRow.proName
                this.formTitle = '编辑项目分析活动信息'
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
                    // let pdata = {}
                    // for(let i=0;i<this.proList.length;i++){
                    //   if(this.selRow.projectName === this.proList[i].projectName){
                    //     pdata.id = this.proList[i].id
                    //     pdata.num = Number(this.proList[i].activities) - 1
                    //   }
                    // }
                    removeActivity(id).then(response => {
                        this.$message({
                            message: this.$t('common.optionSuccess'),
                            type: 'success'
                        })
                        // saveActivityNum(pdata)
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
                }).catch(() => {
                })
            }
        },

        closeDialog() { // 清除表单验证残留信息
            this.$refs['form'].resetFields()
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
