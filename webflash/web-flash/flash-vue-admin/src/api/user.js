import request from '@/utils/request'
import qs from 'qs'

export function login(data) {
  return request({
    url: '/account/login',
    method: 'post',
    data: qs.stringify({
      'username': data.username,
      'password': data.password,
      'verifyCode': data.verifyCode
    })
  })
}

export function getVerifyCodeImage() {
  return request({
    url: '/verifycode/image',
    method: 'get',
  })
}

export function loginReset(data) {
  return request({
    url: '/account/login_reset',
    method: 'post',
    data: qs.stringify({
      'username': data.username,
      'oldPassword': data.oldPassword,
      'newPassword': data.newPassword,
      'newPasswordConfirm': data.newPasswordConfirm,
      'verifyCode': data.verifyCode
    })
  })
}

export function getInfo() {
  return request({
    url: '/account/info',
    method: 'get'
  })
}

export function logout(token) {
  return request({
    url: '/logout',
    method: 'post'
  })
}

export function updatePwd(params) {
  return request({
    url: '/account/updatePwd',
    method: 'post',
    params
  })
}

