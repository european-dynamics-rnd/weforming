import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import SearchResultsView from '../views/SearchResultsView.vue'
import AppDetailView from '../views/AppDetailView.vue'
import CreateAppView from '../views/CreateAppView.vue'
import InstallView from '../views/InstallView.vue'
import LoginView from '../views/LoginView.vue'
import MyAppsView from '../views/MyAppsView.vue'
import AssetEditorView from '../views/AssetEditorView.vue'

const routes = [
  { path: '/login', name: 'login', component: LoginView },
  { path: '/', component: HomeView, meta: { requiresAuth: true } },
  { path: '/search', component: SearchResultsView, meta: { requiresAuth: true } },
  { path: '/app/:id', component: AppDetailView, meta: { requiresAuth: true } },
  { path: '/create-app', component: CreateAppView, meta: { requiresAuth: true, requiresDeveloper: true } },
  { path: '/install/:id', name: 'install', component: InstallView, props: true, meta: { requiresAuth: true } },
  { path: '/my-apps', name: 'my-apps', component: MyAppsView, meta: { requiresAuth: true } },
  { path: '/assets', name: 'assets', component: AssetEditorView, meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return { name: 'login' }
  }
  if (to.name === 'login' && token) {
    return { path: '/' }
  }
  // Developer-only routes: non-developers are sent back to the home page.
  if (to.meta.requiresDeveloper && localStorage.getItem('isDeveloper') !== 'true') {
    return { path: '/' }
  }
})

export default router
