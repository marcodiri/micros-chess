import '~bootstrap/css/bootstrap.min.css'
import '@/assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from '@/router'
import { colorSchemeFromSystem } from '@/utils/colorSchemePlugin'
import { client } from '@/utils/stompClient'

client.connect()

const app = createApp(App)

app.use(colorSchemeFromSystem)
app.use(router)

app.mount('#app')
