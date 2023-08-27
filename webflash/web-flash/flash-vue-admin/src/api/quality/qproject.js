import request from '@/utils/request'

export function getPList(params) {
  return request({
      url: '/quality/project',
      method: 'get',
      params
  })
}

export function cmp(data) {
  return request({
      url: '/quality/projectcmp',
      method: 'post',
      data
  })
}

export function getProject(params) {
  return request({
      url: '/quality/aproject',
      method: 'get',
      params
  })
}

export function newProj(data) {
  return request({
    url: '/quality/aproject',
    method: 'post',
    data
  }) 
}


export function delProj(params) {
  return request({
    url: '/quality/aproject',
    method: 'delete',
    params
  }) 
}


export function checkSonarId(params) {
  return request({
    url: '/quality/checkSonarId',
    method: 'get',
    params
  }) 
}