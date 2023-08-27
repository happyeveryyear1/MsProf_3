// import { getList } from '@/api/report/reportList'

export default {
  filters: {
    statusFilter(status) {
      const statusMap = {
        published: 'success',
        draft: 'info',
        deleted: 'danger'
      }
      return statusMap[status]
    }
  },
  //   props: {
  //     type: {
  //       type: String,
  //       default: 'CN'
  //     }
  //   },
  data() {
    return {
      list: null,
      total: 0,
      listQuery: {
        page: 1,
        limit: 20
      },
      loading: false
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.loading = true
    //   this.$emit('create') // for test
    //   getList(this.listQuery).then(response => {
    //     this.list = response.data.items
    //     this.total = response.data.total
    //     this.loading = false
    //   })
    },
    subItem() {
      this.$router.push({ path: '/report/subitem' })
    },
    comprehensive() {
      this.$router.push({ path: '/report/comprehensive' })
    }
  }
}
