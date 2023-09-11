<script setup lang="ts">
import { client } from '@/utils/StompClient'
import TheLobby from '../components/TheLobby.vue'

console.log(client)
</script>

<template>
  <main>
    <div class="container p-0">
      <TheLobby />
    </div>
  </main>
</template>
