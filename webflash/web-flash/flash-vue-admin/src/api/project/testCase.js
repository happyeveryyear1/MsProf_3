import request from '@/utils/request'

export function getCaseList(params) {
    return request({
        url: '/pro/case/list',
        method: 'get',
        params
    })
}

export function getAllCaseList(params) {
    return request({
        url: '/pro/case/listAll',
        method: 'get',
        params
    })
}

export function addCase(params) {
    return request({
        url: '/pro/case/addCase',
        method: 'post',
        params
    })
}

export function editCase(params) {
    return request({
        url: '/pro/case/editCase',
        method: 'post',
        params
    })
}

// export function changeCase(params) {
//   return request({
//     url: '/pro/case/changeCase',
//     method: 'post',
//     params
//   })
// }

export function removeCase(id) {
    return request({
        url: '/pro/case/delete',
        method: 'delete',
        params: {
            id: id
        }
    })
}

export function importAllTestCase(params) { // 导入所有测试用例
    return request({
        url: '/pro/case/importAll',
        method: 'post',
        params
    })
}

export function selectTestCase(data) {
    return request({
        url: '/pro/case/selectCase',
        method: 'post',
        data
    })
}

export function collectInfo(data) {
    return request({
        url: '/pro/case/collectInfo',
        method: 'post',
        data
    })
}

export function importArtifacts(params) { // 导入组件信息
    return request({
        url: '/pro/case/projectInfo',
        method: 'post',
        params
    })
}

export function deleteSelect(params) {
    return request({
        url: '/pro/case/deleteSelect',
        method: 'get',
        params
    })
}

export function harAnalysis(params) {
    return request({
        url: '/pro/case/harAnalysis',
        method: 'get',
        params
    })
}

export function resultCheck(params) {
    return request({
        url: '/pro/case/resultCheck',
        method: 'get',
        params
    })
}

export function pageInfo(params) {
    return request({
        url: '/pro/case/pageInfo',
        method: 'get',
        params
    })
}

export function pageNumber(params) {
    return request({
        url: '/pro/case/pageNumber',
        method: 'get',
        params
    })
}

export function performanceAnalysis(params) {
    return request({
        url: '/pro/case/performanceAnalysis',
        method: 'post',
        params
    })
}

export function performanceResult(params) {
    return request({
        url: '/pro/case/performanceResult',
        method: 'post',
        params
    })
}

export function performancePercent(params) {
    return request({
        url: '/pro/case/performancePercent',
        method: 'post',
        params
    })
}

export function functionAnalysis(params) {
    return request({
        url: '/pro/case/functionAnalysis',
        method: 'post',
        params
    })
}

export function functionResult(params) {
    return request({
        url: '/pro/case/functionResult',
        method: 'post',
        params
    })
}

export function checkAnalysisResult(params) {
    return request({
        url: '/pro/case/checkAnalysisResult',
        method: 'post',
        params
    })
}

export function getAllRecord() {
    return request({
        url: '/pro/case/getAllRecord',
        method: 'post'
    })
}

export function getCurrentVersion(params) {
    return request({
        url: '/pro/case/getCurrentVersion',
        method: 'post',
        params
    })
}

export function bottleneck(params) {
    return request({
        url: '/pro/case/bottleneck',
        method: 'post',
        params
    })
}

export function getRecord(params) {
    return request({
        url: '/pro/case/getsvcAnaRecord',
        method: 'post',
        params
    })
}

export function tmp() {
    return request({
        url: '/pro/task/tmp',
        method: 'get'
    })
}

export function changeVersion(params) {
    return request({
        url: '/pro/case/changeVersion',
        method: 'post',
        params
    })
}

export function getSpecVersion() {
    return request({
        url: '/pro/case/getSpecVersion',
        method: 'post'
    })
}

export function getHarDepData(params) {
    return request({
        url: '/pro/case/getHarDepData',
        method: 'post',
        params
    })
}

export function getSwaggerDepData(params) {
    return request({
        url: '/pro/case/getSwaggerDepData',
        method: 'post',
        params
    })
}

