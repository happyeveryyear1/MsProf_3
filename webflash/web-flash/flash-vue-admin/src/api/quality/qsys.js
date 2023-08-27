import request from '@/utils/request'

export function getQsys(params) {
  return request({
      url: '/quality/qsys/',
      method: 'get',
      params
  })
}

export function setQsys(params,data) {
  return request({
      url: '/quality/qsys/',
      method: 'post',
      params,
      data
  })
}

export function getQA(params) {
  return request({
      url: '/quality/qaspect/',
      method: 'get',
      params
  })
}

export function testMetric(params,data) {
  return request({
      url: '/quality/testmetric/',
      method: 'post',
      params,
      data
  })
}
