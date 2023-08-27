import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'             the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
    {
        path: '/login',
        component: () => import('@/views/login/index'),
        hidden: true
    },
    {
        path: '/login_reset',
        component: () => import('@/views/login/index_reset'),
        hidden: true
    },
    // {
    //   path: '/register',
    //   component: () => import('@/views/register/index'),
    //   // hidden: true
    // },
    {
        path: '/404',
        component: () => import('@/views/404'),
        hidden: true
    },

    {
        path: '/dashboard',
        component: Layout,
        redirect: '/dashboard',
        children: [{
            path: 'dashboard',
            name: 'Dashboard',
            component: () => import('@/views/dashboard/index'),
            meta: { title: 'dashboard', icon: 'dashboard', affix: true }
        }]
    },
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard/dashboard'
    // children: [{
    //   path: 'dashboard',
    //   name: 'Dashboard',
    //   component: () => import('@/views/dashboard/index'),
    //   meta: { title: 'dashboard', icon: 'dashboard', affix: true }
    // }]
    },
    {
        path: '/pro',
        component: Layout,
        redirect: '/pro/pro',
        meta: { title: '项目管理', icon: 'component' },
        hidden: true,
        children: [
            //     {
            //       path: 'projectList',
            //       name: 'ProjectList',
            //       component: () => import('@/views/project/projectList/index'),
            //       meta: { title: '项目总表信息', icon: 'list' },
            //       hidden: true
            //     },
            //     {
            //       path: 'testActivity',
            //       name: 'testActivity',
            //       component: () => import('@/views/project/testActivity/index'),
            //       meta: { title: '项目测试活动', icon: 'list' },
            //       hidden: true
            //     },
            //     {
            //       path: 'testTask/:testactivityName',
            //       name: 'testTask',
            //       component: () => import('@/views/project/testTask/index'),
            //       meta: { title: '测试任务', icon: 'list' },
            //       hidden: true
            //     },
            //     {
            //       path: 'executeTask/:taskName',
            //       name: 'executeTask',
            //       component: () => import('@/views/project/testTask/executeTask/index'),
            //       meta: { title: '执行任务', icon: 'list' },
            //       hidden:true
            //     },
            {
                path: 'viewMonitoring/:taskName',
                name: 'viewMonitoring',
                component: () => import('@/views/project/testTask/viewMonitoringInfo/index'),
                meta: { title: '查看监控信息', icon: 'list' },
                hidden: true
            },
            {
                path: 'viewHar/:taskName',
                name: 'viewHar',
                component: () => import('@/views/project/testTask/viewHarInfo/index'),
                meta: { title: '查看Har信息', icon: 'list' },
                hidden: true
            },
            {
                path: 'viewSwagger/:taskName',
                name: 'viewSwagger',
                component: () => import('@/views/project/testTask/viewSwaggerInfo/index'),
                meta: { title: '查看Swagger信息', icon: 'list' },
                hidden: true
            },
            {
                path: 'viewStatistic/:taskName',
                name: 'viewStatistic',
                component: () => import('@/views/project/testTask/viewStatisticInfo/index'),
                meta: { title: '接口统计信息', icon: 'list' },
                hidden: true
            },
            {
                path: 'viewSvc/:taskName',
                name: 'viewSvc',
                component: () => import('@/views/project/testTask/viewSvcInfo/index'),
                meta: { title: '系统统计信息', icon: 'list' },
                hidden: true
            },
            {
                path: 'viewAnalyzeInfo/:taskName',
                name: 'viewAnalyzeResult',
                component: () => import('@/views/project/testTask/viewAnalyzeInfo/index'),
                meta: { title: '性能分析结果', icon: 'list' },
                hidden: true
            }
            //     {
            //       path: 'testCase/:taskName',
            //       name: 'testCase',
            //       component: () => import('@/views/project/testCase/index'),
            //       meta: { title: '测试用例', icon: 'list' },
            //       hidden: true,
            //     }
            //   ]
            // },
            // {
            //   path: '/tasks',
            //   component: Layout,
            //   children: [
            //     {
            //       path: 'taskList',
            //       name: 'taskList',
            //       component: () => import('@/views/tasks/taskList/index'),
            //       meta: { title: '任务管理', icon: 'form' }
            //     },
            //     {
            //       path: 'execution',
            //       name: 'execution',
            //       component: () => import('@/views/testcase/execution/index'),
            //       meta: { title: '用例执行', icon: 'list' },
            //       hidden: true
            //     },
            //     {
            //       path: 'executionResult',
            //       name: 'executionResult',
            //       component: () => import('@/views/testcase/executionResult/index'),
            //       meta: { title: '用例执行结果', icon: 'list' },
            //       hidden: true
            //     }]
            // },
            // {
            //   path: '/manage',
            //   component: Layout,
            //   redirect: '/manage/qdmanage',
            //   name: 'Manage',
            //   meta: {
            //     title: '质量数据',
            //     icon: 'nested'
            //   },
            //   children: [
            //     {
            //       path: 'qdmanage',
            //       component: () => import('@/views/manage/qdmanage/index'), // Parent router-view
            //       name: 'qdmanage',
            //       meta: { title: '质量数据管理' }
            //     },
            //     {
            //       path: 'qdconf',
            //       component: () => import('@/views/manage/qdconf/index'),
            //       name: 'qdconf',
            //       meta: { title: '质量数据配置项' }
            //     }
            //   ]
            // },

            // {
            //   path: '/analysis',
            //   component: Layout,
            //   redirect: '/analysis/problemAnalysis',
            //   name: 'Analysis',
            //   meta: {
            //     title: '测试问题分析',
            //     icon: 'monitor'
            //   },
            //   hidden: true,
            //   children: [
            //     {
            //       path: 'problemAnalysis',
            //       component: () => import('@/views/analysis/problemAnalysis/index'), // Parent router-view
            //       name: 'problemAnalysis',
            //       meta: { title: '测试性能分析' },
            //       hidden:true
            //     },
        ]
    },

    // {
    //   path: '/quality',
    //   component: Layout,
    //   redirect: '/quality/proList',
    //   hidden: true,
    //   children: [
    //     {
    //       path: 'proList',
    //       name: 'proList',
    //       component: () => import('@/views/quality/proList/index'),
    //       meta: { title: '综合质量评估', icon: 'quality' },
    //       hidden: true,
    //     },
    //     {
    //       path: 'vlist/:id',
    //       name: 'vlist',
    //       component: () => import('@/views/quality/vlist/index'),
    //       meta: { title: '版本列表', activeMenu: '/quality'},
    //       hidden: true
    //     },
    //     {
    //       path: 'qsys/:id',
    //       name: 'qsys',
    //       component: () => import('@/views/quality/qsys/index'),
    //       meta: { title: '质量体系管理', activeMenu: '/quality' },
    //       hidden: true
    //     },
    //     {
    //       path: 'report/:id',
    //       name: 'report',
    //       component: () => import('@/views/quality/report/index'),
    //       meta: { title: '评估报告', activeMenu: '/quality//vlist' },
    //       hidden: true
    //     },
    //     {
    //       path: 'comparison',
    //       name: 'comparison',
    //       component: () => import('@/views/quality/comparison/index'),
    //       meta: { title: '对比', activeMenu: '/quality' },
    //       hidden: true
    //     }
    //   ]
    // },
    // {
    //   path: '/report',
    //   component: Layout,
    //   redirect: '/report/list',
    //   name: 'Report',
    //   meta: {
    //     title: '质量报告',
    //     icon: 'clipboard'
    //   },
    //   children: [
    //     {
    //       path: 'list',
    //       component: () => import('@/views/report/list/index'),
    //       name: 'list',
    //       meta: { title: '质量报告查看', icon: 'list' }
    //     },
    //     {
    //       path: 'conf',
    //       component: () => import('@/views/report/conf/index'),
    //       name: 'conf',
    //       meta: { title: '质量报告生成', icon: 'cfg' }
    //     }
    //   ]
    // },
    // {
    //   path: '/boy',
    //   component: Layout,
    //   children: [
    //     {
    //       path: '/boy',
    //       name: 'boy',
    //       component: () => import('@/views/test/boy/index'),
    //       meta: { title: '代码测试界面', icon: 'form' }
    //     }]
    // },

    {
        path: '/account',
        component: Layout,
        hidden: true,
        redirect: 'noredirect',
        children: [
            {
                path: 'profile',
                name: '个人资料',
                component: () => import('@/views/account/profile.vue'),

                meta: { title: '个人资料' }

            },
            {
                path: 'timeline',
                name: '最近活动',
                component: () => import('@/views/account/timeline.vue'),
                hidden: true,
                meta: { title: '最近活动' }

            },
            {
                path: 'updatePwd',
                name: '修改密码',
                component: () => import('@/views/account/updatePwd.vue'),
                hidden: true,
                meta: { title: '修改密码' }
            }
        ]
    }

]

const createRouter = () => new Router({
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRoutes
})

const router = createRouter()

export function resetRouter() {
    const newRouter = createRouter()
    router.matcher = newRouter.matcher
}

export default router
