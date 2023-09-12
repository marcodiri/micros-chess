<script setup lang="ts">
import GameProposalLobbyElement from '@/components/GameProposalLobbyElement.vue'
import { client } from '@/utils/stompClient'
import { readonly } from 'vue'

const emit = defineEmits(['game-proposal-accepted'])

const gameProposals = readonly(client.gameProposals)

function createGameProposal() {
  client.sendCreateGameProposalRequest((event) => {
    console.log(event)
    emit('game-proposal-accepted', event)
  })
}

function acceptGameProposal(gameProposal: any) {
  if (gameProposal.creatorId !== client.playerUUID) {
    client.sendAcceptGameProposalRequest(gameProposal.gameProposalId, (event) => {
      console.log(event)
      emit('game-proposal-accepted', event)
    })
  }
}
</script>

<template>
  <h4>Lobby</h4>
  <button
    type="button"
    class="btn btn-primary"
    :class="{ disabled: !client.isConnected.value }"
    @click="createGameProposal"
  >
    Create game
  </button>
  <div class="table-wrapper border rounded">
    <table class="table table-striped table-hover">
      <thead>
        <tr>
          <th scope="col">#</th>
          <th scope="col">Game proposal ID</th>
          <th scope="col">State</th>
        </tr>
      </thead>
      <tbody>
        <GameProposalLobbyElement
          v-for="(gp, n) in gameProposals"
          :key="n"
          :class="gp.creatorId !== client.playerUUID ? 'clickable' : 'unclickable'"
          @click="acceptGameProposal(gp)"
        >
          <template #num>{{ n + 1 }}</template>
          <template #id>{{ gp.gameProposalId }}</template>
          <template #state>{{ gp.type === 'CREATED' ? 'OPEN' : 'CLOSED' }}</template>
        </GameProposalLobbyElement>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.table-wrapper {
  height: 400px;
  overflow-y: scroll;
}

.clickable {
  cursor: pointer;
}

.unclickable {
  cursor: not-allowed;
}
</style>
