<script setup lang="ts">
import GameProposalLobbyElement from '@/components/GameProposalLobbyElement.vue'
</script>

<template>
  <div class="container p-0">
    <h4>Lobby</h4>
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
          <GameProposalLobbyElement v-for="n in 3" :key="n" :n="n" />
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.table-wrapper {
  height: 400px;
  overflow-y: scroll;
}
</style>
