import request from '@/utils/request'

export function getList(params) {
    return request({
        url: '/test/boy/list',
        method: 'get',
        params
    })
}


export function save(params) {
    return request({
        url: '/test/boy',
        method: 'post',
        params
    })
}

export function remove(id) {
    return request({
        url: '/test/boy',
        method: 'delete',
        params: {
            id: id
        }
    })
}
