import '~bootstrap/dist/css/bootstrap.min.css'
import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from '@/router'
import { client } from '@/utils/StompClient'

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

client.connect()

const app = createApp(App)

app.use(router)

app.mount('#app')
