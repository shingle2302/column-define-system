import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

import App from './App.vue'
import { routes } from './router'
import { isLoggedIn } from './stores/auth'
import './styles/global.css'

const app = createApp(App)

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  if (to.path !== '/login' && !isLoggedIn()) {
    next('/login')
  } else {
    next()
  }
})

app.use(router)
app.use(Antd)
app.mount('#app')
