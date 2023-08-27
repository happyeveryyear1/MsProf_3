import request from '@/utils/request'

export function getTaskList(params) {
    return request({
        url: '/pro/task/list',
        method: 'get',
        params
    })
}

export function getAllTaskList(params) {
    return request({
        url: '/pro/task/listAll',
        method: 'get',
        params
    })
}

// export function count(params) {
//   return request({
//     url: '/pro/task/count',
//     method: 'get',
//     params
//   })
// }

export function addTask(params) {
    return request({
        url: '/pro/task/addTask',
        method: 'post',
        params
    })
}

export function editTask(params) {
    return request({
        url: '/pro/task/editTask',
        method: 'post',
        params
    })
}

export function saveResult(params) {
    return request({
        url: '/pro/task/saveResult',
        method: 'get',
        params
    })
}

export function removeTask(id) {
    return request({
        url: '/pro/task/delete',
        method: 'delete',
        params: {
            id: id
        }
    })
}

export function executeTask(params) {
    return request({
        url: '/pro/task/executeTask',
        method: 'get',
        params
    })
}

export function getJobResult(params) {
    return request({
        url: '/pro/task/getJobResult',
        method: 'get',
        params
    })
}

export function getJobResultFile(params) {
    return request({
        url: '/pro/task/getJobResultFile',
        method: 'get',
        params
    })
}

export function mvcDiagnoserAnalyzeHar(params) {
    return request({
        url: '/pro/task/mvcDiagnoserAnalyzeHar',
        method: 'post',
        params
    })
}

export function mvcDiagnoserAnalyzeSwagger(params) {
    return request({
        url: '/pro/task/mvcDiagnoserAnalyzeSwagger',
        method: 'post',
        params
    })
}

export function getAllExecInfoFromDB(params) {
    return request({
        url: '/pro/task/getAllExecInfoFromDB',
        method: 'post',
        params
    })
}

export function getInterfaceData(params) {
    return request({
        url: '/pro/task/getInterfaceData',
        method: 'post',
        params
    })
}

export function anomalyDetect(params) {
    return request({
        url: '/pro/task/anomalyDetect',
        method: 'post',
        params
    })
}

export function getAllExecInfo(params) {
    return request({
        url: '/pro/task/getAllExecInfo',
        method: 'post',
        params
    })
}

export function getRootCause(params) {
    return request({
        url: '/pro/task/getRootCause',
        method: 'post',
        params
    })
}

export function getCurrentStatus(params) {
    return request({
        url: '/pro/task/getCurrentStatus',
        method: 'post',
        params
    })
}

export function getTests(params) {
    return request({
        url: '/pro/task/getTests',
        method: 'post',
        params
    })
}
