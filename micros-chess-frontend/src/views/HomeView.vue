<script setup lang="ts">
import { ref, type Component, type Ref } from 'vue'
import TheLobby from '@/components/TheLobby.vue'
import TheGame from '@/components/TheGame.vue'

let displayedComponent: Ref<Component> = ref(TheLobby)

function switchComponent(event: any) {
  displayedComponent.value = TheGame
}
</script>

<template>
  <main>
    <div class="container p-0">
      <component :is="displayedComponent" @game-proposal-accepted="switchComponent"></component>
    </div>
  </main>
</template>
