import Mock from 'mockjs'

const data = Mock.mock({
  'items|30': [{
    'id|+1': 1,
    'version|1-9.1': 1,
    'task_num|1-100': 1,
    title: '@sentence(10, 20)',
    'status|1': ['published', 'draft', 'deleted'],
    author: 'name',
    display_time: '@datetime',
    pageviews: '@integer(300, 5000)'
  }]
})

export default [
  {
    url: '/vue-admin-template/report/list',
    type: 'get',
    response: config => {
      const { id, page = 1, limit = 10 } = config.query
      const items = data.items
      const mockList = items.filter(item => {
        if (id && item.id.indexOf(id) < 0) return false
        return true
      })
      const pageList = mockList.filter((item, index) => index < limit * page && index >= limit * (page - 1))
      return {
        code: 20000,
        data: {
          total: mockList.length,
          items: pageList
        }
      }
    }
  }
]
