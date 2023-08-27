import request from '@/utils/request'
import qs from 'qs'

export function getList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

export function addUser(data) {
  return request({
    url: '/user/addUser',
    method: 'post',
    data: qs.stringify({
      'id': data.id,
      'name': data.name,
      'account': data.account,
      'birthday': data.birthday,
      'sex': data.sex,
      'email': data.email,
      'password': data.password,
      'rePassword': data.rePassword,
      'dept': data.dept,
      'status': data.status,
      'deptid': data.deptid,
      'phone':data.phone
    })
  })
}

export function editUser(data) {
  return request({
    url: '/user/editUser',
    method: 'post',
    data: qs.stringify({
      'id': data.id,
      'name': data.name,
      'account': data.account,
      'birthday': data.birthday,
      'sex': data.sex,
      'email': data.email,
      'password': data.password,
      'rePassword': data.rePassword,
      'dept': data.dept,
      'status': data.status,
      'deptid': data.deptid,
      'phone':data.phone
    })
  })
}

export function remove(userId) {
  return request({
    url: '/user',
    method: 'delete',
    params: {
      userId
    }
  })
}

export function setRole(params) {
  return request({
    url: '/user/setRole',
    method: 'get',
    params
  })
}

export function changeStatus(userId) {
  return request({
    url: '/user/changeStatus',
    method: 'get',
    params:{
      userId
    }
  })
}
