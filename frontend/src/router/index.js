import Vue from 'vue'
import VueRouter from 'vue-router'
import Layout from '@/views/Layout.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '营业报表', icon: 'el-icon-data-line' }
      },
      {
        path: 'movies',
        name: 'Movies',
        component: () => import('@/views/Movies.vue'),
        meta: { title: '影片管理', icon: 'el-icon-film' }
      },
      {
        path: 'halls',
        name: 'Halls',
        component: () => import('@/views/Halls.vue'),
        meta: { title: '影厅管理', icon: 'el-icon-office-building' }
      },
      {
        path: 'schedules',
        name: 'Schedules',
        component: () => import('@/views/Schedules.vue'),
        meta: { title: '排片管理', icon: 'el-icon-date' }
      },
      {
        path: 'tickets',
        name: 'Tickets',
        component: () => import('@/views/Tickets.vue'),
        meta: { title: '票务销售', icon: 'el-icon-tickets' }
      },
      {
        path: 'verify',
        name: 'Verify',
        component: () => import('@/views/Verify.vue'),
        meta: { title: '取票核销', icon: 'el-icon-postcard' }
      },
      {
        path: 'members',
        name: 'Members',
        component: () => import('@/views/Members.vue'),
        meta: { title: '会员管理', icon: 'el-icon-user' }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
