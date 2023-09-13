<script setup lang="ts">
import { type Component, type Ref, shallowRef } from 'vue'
import TheLobby from '@/components/TheLobby.vue'
import TheGame from '@/components/TheGame.vue'
import { client } from '@/utils/stompClient'

let displayedComponent: Ref<Component> = shallowRef(TheLobby)

let currentGameId: string
let currentColor: string

client.registerGameAcceptedCallback((event) => {
  console.log(event)
  currentGameId = event.gameId
  currentColor = event.player1Id == client.playerUUID ? 'w' : 'b'
  displayedComponent.value = TheGame
})
</script>

<template>
  <main>
    <div class="container p-0">
      <component
        :is="displayedComponent"
        :game-id="currentGameId"
        :player-color="currentColor"
      ></component>
    </div>
  </main>
</template>
