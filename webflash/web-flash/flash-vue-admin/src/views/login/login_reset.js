
import { isvalidUsername } from '@/utils/validate'
import LangSelect from '@/components/LangSelect'
import { encrypt, decrypt } from '@/utils/aes'
import { getVerifyCodeImage} from '@/api/user'

export default {
  name: 'loginReset',
  components: { LangSelect },
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!isvalidUsername(value)) {
        callback(new Error(this.$t('login.errorAccount')))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error(this.$t('login.errorPassword')))
      } else {
        callback()
      }
    }
    const validateVerifyCode = (rule, value, callback) => {
      if (value.length ===0 || value.length > 4) {
        callback(new Error('请输入正确验证码'))
      } else {
        callback()
      }
    }
    return {
      loginResetForm: {
        username: '',
        oldPassword: '',
        newPassword: '',
        newPasswordConfirm: '',
        verifyCode: ''
      },
      key: '',
      loginRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        oldPassword: [{ required: true, trigger: 'blur', validator: validatePassword }],
        newPassword: [
          { required: true,
            validator:(rule,value,callback)=>{
            if(value===''){
              callback(new Error('请输入新密码'))
            }else{
              if(this.loginResetForm.username === 'intellitest'){
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

        newPasswordConfirm:[
          { required:true,message:'请输入确认密码',trigger:'blur'},
          { validator:(rule,value,callback)=>{
            if(value===''){
              callback(new Error('请再次输入密码'))
            }else if(value!==this.loginResetForm.newPassword){
              callback(new Error('两次输入密码不一致'))
            }else{
              callback( )
            }
          },
          trigger:'blur'
          }
        ],
        verifyCode: [{ required: true, trigger: 'blur', validator: validateVerifyCode}],
      },
      loading: false,
      pwdType: 'password',
      pwdType2: 'password',
      redirect: '/',
      base64: 'data:image/png;base64,',
      imageSrc: '',
    }
  },
  mounted() {
    this.init()
    this.$nextTick()
  },
  created() {
    this.$forceUpdate()
  },
  methods: {
    init() {
      const redirect = this.$route.query.redirect
      console.log('redirect', redirect)
      if (redirect) {
        this.redirect = redirect
      }
      getVerifyCodeImage().then(response => {
        this.imageSrc = this.base64 + response.data
      })
    },
    showPwd() {
      if (this.pwdType === 'password') {
        this.pwdType = ''
      } else {
        this.pwdType = 'password'
      }
    },
    showPwd2() {
      if (this.pwdType2 === 'password') {
        this.pwdType2 = ''
      } else {
        this.pwdType2 = 'password'
      }
    },
  
    handleLoginReset() {
      this.$refs.loginResetForm.validate(valid => {
        if (valid) {
          this.loading = true
          let pdata = {}
          pdata.username = this.loginResetForm.username
          pdata.oldPassword = encrypt(this.loginResetForm.oldPassword)
          pdata.newPassword = encrypt(this.loginResetForm.newPassword)
          pdata.newPasswordConfirm = encrypt(this.loginResetForm.newPasswordConfirm)
          pdata.verifyCode = this.loginResetForm.verifyCode
          this.$store.dispatch('user/loginReset', pdata).then(() => {
            this.loading = false
            this.$router.push({ path: this.redirect })
          }).catch((err) => {
            this.loading = false
            this.refreshCode()
            this.loginResetForm.verifyCode = ''
          })
        } else {
          return false
        }
      })
    },

    refreshCode(){
      getVerifyCodeImage().then(response => {
        this.imageSrc = this.base64 + response.data
      })
    }
  }
}
