<script setup lang="ts">
import { type Component, type Ref, shallowRef } from 'vue'
import TheLobby from '@/components/TheLobby.vue'
import TheGame from '@/components/TheGame.vue'

let displayedComponent: Ref<Component> = shallowRef(TheLobby)

function switchComponent(event: any) {
  displayedComponent.value = TheGame
}
</script>

<template>
  <main>
    <div class="container p-0">
      <component :is="displayedComponent" @game-proposal-accepted="switchComponent"></component>
      <TheGame />
    </div>
  </main>
</template>
