<script setup lang="ts">
import { type Component, type Ref, shallowRef } from 'vue'
import TheLobby from '@/components/TheLobby.vue'
import TheGame from '@/components/TheGame.vue'
import { client } from '@/utils/stompClient'

let displayedComponent: Ref<Component> = shallowRef(TheLobby)

client.registerGameAcceptedCallback((event) => {
  console.log(event)
  displayedComponent.value = TheGame
})
</script>

<template>
  <main>
    <div class="container p-0">
      <component :is="displayedComponent"></component>
      <TheGame />
    </div>
  </main>
</template>
