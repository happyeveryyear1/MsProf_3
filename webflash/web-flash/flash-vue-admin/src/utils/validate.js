/**
 * Created by PanJiaChen on 16/11/18.
 */

/**
 * @param {string} path
 * @returns {Boolean}
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * 校验用户名,必须是5-32位
 * @param {string} str
 * @returns {Boolean}
 */
export function isvalidUsername(str) {
  var reg = /^[0-9A-Za-z]{3,20}$/
  return reg.test(str)
}

/**
 * 校验测试机配置的ip地址,必须符合ip地址格式
 * @param {string} str
 * @returns {Boolean}
 */
export function isvalidateIP(rule, value, callback) {
  if (value === '' || value === undefined || value === null) {
    callback()
  } else {
    const reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/
    if ((!reg.test(value)) && value !== '') {
      callback(new Error('请输入正确的IP地址'))
    } else {
      callback()
    }
  }
}

/**
 * 校验测试用例git地址,必须符合http格式
 * @param {string} str
 * @returns {Boolean}
 */
export function validateGitAddress(str) {
  var reg = /(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?$/
  return reg.test(str)
}

/**
 * 校验项目名和测试活动名的格式,必须符合“只含有汉字、数字、字母、小数点、下划线不能以下划线开头和结尾”，并且长度在3-20个字符之间
 * @param {string} str
 * @returns {Boolean}
 */
export function validateName(str) {
  var reg = /^(?!_)(?!.*?_$)[a-zA-Z0-9_\.\u4e00-\u9fa5]+$/
  return reg.test(str)
}
