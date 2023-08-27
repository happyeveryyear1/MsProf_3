import request from '@/utils/request'

export function getVList(params) {
  return request({
    url: '/quality/version',
    method: 'get',
    params
  })
}

export function measureVer(params) {
  return request({
    url: '/quality/measure',
    method: 'get',
    params,
    timeout: 1000 * 60 * 2,
  })
}


export function newTask(params) {
  return request({
    url: '/quality/task',
    method: 'post',
    params
  }) 
}

export function delTask(params) {
  return request({
    url: '/quality/task',
    method: 'delete',
    params
  }) 
}