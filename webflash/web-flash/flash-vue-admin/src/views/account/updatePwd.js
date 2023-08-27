import { updatePwd } from '@/api/user'
import { encrypt, decrypt } from '@/utils/aes'

export default {
  data() {
    return {
      pwdType_old: 'password',
      pwdType_new: 'password',
      pwdType_re: 'password',
      form: {
        oldPassword: '',
        password: '',
        rePassword: ''
      },
      activeName: 'updatePwd',
      user: {},
      rules:{
        password: [
          { required: true,
            validator:(rule,value,callback)=>{
            if(value===''){
              callback(new Error('请输入密码'))
            }else{
              console.log('--------',this.user.roles)
              if(this.user.account === 'intellitest'){
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
      }
    }
  },

  mounted() {
    this.init()
  },
  methods: {
    init(){
      this.user = this.$store.state.user.profile
    },
    handleClick(tab, event){
      this.$router.push({ path: '/account/'+tab.name})
    },
    updatePwd() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          if(this.form.oldPassword === this.form.password){
            this.$notify.error({
              title: '错误',
              message: '新旧密码一致'
            })
          }else{
            this.pwdType_old = 'password'
            this.pwdType_new = 'password'
            this.pwdType_re = 'password'
            let oldPassword = encrypt(this.form.oldPassword)
            let password = encrypt(this.form.password)
            let rePassword = encrypt(this.form.rePassword)
            updatePwd({
              oldPassword: oldPassword,
              password: password,
              rePassword: rePassword
            }).then(response => {
              this.$message({
                message: '密码修改成功',
                type: 'success'
              })
              //退出登录，该操作是个异步操作，所以后面跳转到登录页面延迟1s再执行（如果有更好的方法再调整）
              this.$store.dispatch('user/logout')
              const self = this
              setTimeout(function(){
                self.$router.push(`/login`)
              },1000)
            }).catch((err) => {
            })
          }
        } else {
          return false
        }
      })
    },

    showPwd_old() { // 密码可视/隐藏
      if (this.pwdType_old === 'password') {
        this.pwdType_old = ''
      } else {
        this.pwdType_old = 'password'
      }
    },

    showPwd_new() { // 密码可视/隐藏
      if (this.pwdType_new === 'password') {
        this.pwdType_new = ''
      } else {
        this.pwdType_new = 'password'
      }
    },

    showPwd_re() { // 密码可视/隐藏
      if (this.pwdType_re === 'password') {
        this.pwdType_re = ''
      } else {
        this.pwdType_re = 'password'
      }
    },

  }
}
