import Vue from 'vue'
import Router from 'vue-router'
import Login from '@/components/Login.vue'
import Information from '@/components/Information.vue'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Login',
      component: Login
    },
    {
      path: '/information',
      name: 'Information',
      component: Information
    }
  ]
})
