import request from '@/utils/request'

export function getAllActivityList(params) {
  return request({
    url: '/pro/test/activity/listAll',
    method: 'get',
    params
  })
}

export function getActivityList(params) {
  return request({
    url: '/pro/test/activity/list',
    method: 'get',
    params
  })
}

// export function getActivityListLike(params) {
//   return request({
//     url: '/pro/test/activity/listLike',
//     method: 'get',
//     params
//   })
// }

// export function count(params) {
//   return request({
//     url: '/pro/test/activity/count',
//     method: 'get',
//     params
//   })
// }

export function addActivity(params) {
  return request({
    url: '/pro/test/activity/addActivity',
    method: 'post',
    params
  })
}

export function editActivity(params) {
  return request({
    url: '/pro/test/activity/editActivity',
    method: 'post',
    params
  })
}

export function saveTaskNum(params) {
  return request({
    url: '/pro/test/activity/saveTaskNum',
    method: 'get',
    params
  })
}

export function removeActivity(id) {
  return request({
    url: '/pro/test/activity/delete',
    method: 'delete',
    params: {
      id: id
    }
  })
}
