import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Signup from '../views/Signup.vue'
import EvChargingZoneMonitoring from '../views/EvChargingZoneMonitoring.vue'
import EVVideoBoard from '../views/EVVideoBoard.vue'
import EVUserDashboard from '../views/EVUserDashboard.vue'
// ✅ 1. 새로 만든 DB 사용량 페이지를 불러옵니다(Import)
import EvDatabaseUsage from '../views/EvDatabaseUsage.vue'

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login
  },
  {
    path: '/signup',
    name: 'Signup',
    component: Signup
  },
  {
    path: '/EvChargingZoneMonitoring',
    name: 'EvChargingZoneMonitoring',
    component: EvChargingZoneMonitoring
  },
  {
    path: '/EVVideoBoard',
    name: 'EVVideoBoard',
    component: EVVideoBoard
  },
  {
    path: '/EVUserDashboard', 
    name: 'EVUserDashboard',
    component: EVUserDashboard
  },
  // ✅ 2. DB 사용량 대시보드 경로를 추가합니다
  {
    path: '/EvDatabaseUsage', 
    name: 'EvDatabaseUsage',
    component: EvDatabaseUsage
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router