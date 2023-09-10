import '~bootstrap/dist/css/bootstrap.min.css'
import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const colorSchemeQueryList = window.matchMedia('(prefers-color-scheme: dark)')

const setColorScheme = (e: MediaQueryList | MediaQueryListEvent) => {
  if (e.matches) {
    // Dark
    console.log('Dark mode')
    document.documentElement.setAttribute('data-bs-theme', 'dark')
  } else {
    // Light
    console.log('Light mode')
    document.documentElement.setAttribute('data-bs-theme', 'light')
  }
}

setColorScheme(colorSchemeQueryList)
colorSchemeQueryList.addEventListener('change', setColorScheme)

const app = createApp(App)

app.use(router)

app.mount('#app')
