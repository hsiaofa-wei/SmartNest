import { createRouter, createWebHistory } from 'vue-router'
import store from '../store'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../views/admin/UserManagement.vue')
      },
      {
        path: 'houses/audit',
        name: 'HouseAudit',
        component: () => import('../views/admin/HouseAudit.vue')
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('../views/admin/Statistics.vue')
      },
      
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/Profile.vue')
      }
    ]
  },
  {
    path: '/landlord',
    component: () => import('../layouts/LandlordLayout.vue'),
    meta: { requiresAuth: true, roles: ['LANDLORD', 'ADMIN'] },
    children: [
      {
        path: '',
        redirect: '/landlord/houses'
      },
      {
        path: 'houses',
        name: 'LandlordHouses',
        component: () => import('../views/landlord/HouseManagement.vue')
      },
      {
        path: 'appointments',
        name: 'LandlordAppointments',
        component: () => import('../views/landlord/AppointmentManagement.vue')
      },
      {
        path: 'orders',
        name: 'LandlordOrders',
        component: () => import('../views/landlord/OrderManagement.vue')
      },
      {
        path: 'houses/publish',
        name: 'PublishHouse',
        component: () => import('../views/landlord/PublishHouse.vue')
      },
      {
        path: 'houses/edit/:id',
        name: 'EditHouse',
        component: () => import('../views/landlord/HouseEdit.vue')
      },
      {
        path: 'profile',
        name: 'LandlordProfile',
        component: () => import('../views/Profile.vue')
      }
    ]
  },
  {
    path: '/tenant',
    component: () => import('../layouts/TenantLayout.vue'),
    meta: { requiresAuth: true, roles: ['TENANT', 'ADMIN'] },
    children: [
      {
        path: '',
        redirect: '/tenant/houses'
      },
      {
        path: 'houses',
        name: 'TenantHouses',
        component: () => import('../views/tenant/HouseList.vue')
      },
      {
        path: 'houses/:id',
        name: 'HouseDetail',
        component: () => import('../views/tenant/HouseDetail.vue')
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('../views/tenant/Favorites.vue')
      },
      {
        path: 'appointments',
        name: 'TenantAppointments',
        component: () => import('../views/tenant/Appointments.vue')
      },
      {
        path: 'orders',
        name: 'TenantOrders',
        component: () => import('../views/tenant/Orders.vue')
      },
      {
        path: 'profile',
        name: 'TenantProfile',
        component: () => import('../views/Profile.vue')
      },
      {
        path: 'orders/create/:houseId',
        name: 'CreateOrder',
        component: () => import('../views/tenant/CreateOrder.vue')
      },
      {
        path: 'payment/:orderId',
        name: 'Payment',
        component: () => import('../views/tenant/Payment.vue')
      }
    ]
  },
  {
    path: '/',
    redirect: '/tenant/houses'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = store.state.auth.token
  const userRole = store.state.auth.role

  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
    } else if (to.meta.roles && !to.meta.roles.includes(userRole)) {
      next('/login')
    } else {
      next()
    }
  } else {
    if (token && (to.path === '/login' || to.path === '/register')) {
      // 已登录用户访问登录页，重定向到对应角色首页
      if (userRole === 'ADMIN') {
        next('/admin/dashboard')
      } else if (userRole === 'LANDLORD') {
        next('/landlord/houses')
      } else {
        next('/tenant/houses')
      }
    } else {
      next()
    }
  }
})

export default router

