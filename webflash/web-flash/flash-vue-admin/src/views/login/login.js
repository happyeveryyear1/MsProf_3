import { getVerifyCodeImage} from '@/api/user'

import { isvalidUsername } from '@/utils/validate'
import LangSelect from '@/components/LangSelect'
import { encrypt, decrypt } from '@/utils/aes'
export default {
  name: 'login',
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
      loginForm: {
        username: '',
        password: '',
        verifyCode: ''

      },
      key: '',
      loginRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        verifyCode: [{ required: true, trigger: 'blur', validator: validateVerifyCode}],
      },
      loading: false,
      pwdType: 'password',
      redirect: '/',
      base64: 'data:image/png;base64,',
      imageSrc: ''
    }
  },
  mounted() {
    this.init()
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
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          let pdata = {}
          pdata.username = this.loginForm.username
          pdata.password = encrypt(this.loginForm.password)
          pdata.verifyCode = this.loginForm.verifyCode
          this.$store.dispatch('user/login', pdata).then(() => {
            this.loading = false
            this.$router.push({ path: this.redirect })
          }).catch((err) => {
            this.loading = false
            console.error(err)
            if (err.msg=="密码已失效")
              setTimeout(()=>{
                this.$router.push({ path: '/login_reset' })
              },10)
            getVerifyCodeImage().then(response => {
              this.imageSrc = this.base64 + response.data
            })
            this.loginForm.verifyCode = ''
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
