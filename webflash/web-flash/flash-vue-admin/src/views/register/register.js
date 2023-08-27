
import { isvalidUsername } from '@/utils/validate'
import LangSelect from '@/components/LangSelect'
export default {
  name: 'register',
  components: { LangSelect },
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!isvalidUsername(value)) {
        callback(new Error(this.$t('register.errorAccount')))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 5) {
        callback(new Error(this.$t('register.errorPassword')))
      } else {
        callback()
      }
    }
    return {
      registerForm: {
        username: '',
        password: '',
        role: '项目管理员'
      },
      registerRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      loading: false,
      pwdType: 'password',
      redirect: '/'
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
    },
    showPwd() {
      if (this.pwdType === 'password') {
        this.pwdType = ''
      } else {
        this.pwdType = 'password'
      }
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.$store.dispatch('user/login', this.registerForm).then(() => {
            this.loading = false
            this.$router.push({ path: this.redirect })
          }).catch((err) => {
            this.loading = false
          })
        } else {
          return false
        }
      })
    },
    save() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          save({
            projectName: this.form.projectName,
            projectIntroduction: this.form.projectIntroduction,
            projectAddressList: this.form.projectAddressList,
            id: this.form.id
          }).then(response => {
            this.$message({
              message: this.$t('common.optionSuccess'),
              type: 'success'
            })
            this.fetchData()
            this.formVisible = false
          })
        } else {
          return false
        }
      })
    }
  }
}
