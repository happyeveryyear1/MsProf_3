import request from '@/utils/request'

export function getAllProList(params) {
    return request({
        url: '/pro/list/listAll',
        method: 'get',
        params
    })
}

export function getProList(params) {
    return request({
        url: '/pro/list',
        method: 'get',
        params
    })
}

export function addPro(params) {
    return request({
        url: '/pro/list/addPro',
        method: 'post',
        params
    })
}

export function editPro(params) {
    return request({
        url: '/pro/list/editPro',
        method: 'post',
        params
    })
}

export function saveActivityNum(params) {
    return request({
        url: '/pro/list/saveActivityNum',
        method: 'get',
        params
    })
}

export function removePro(id) {
    return request({
        url: '/pro/list/delete',
        method: 'delete',
        params: {
            id: id
        }
    })
}

export function getApplicationName() {
    return request({
        url: '/pro/list/getApplicationName',
        method: 'get'
    })
}
