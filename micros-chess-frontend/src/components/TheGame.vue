<script setup lang="ts">
// import '@/assets/chessboard.js'
import { onMounted } from 'vue'
import { Chess, type Move } from 'chess.js'
import { client } from '@/utils/stompClient'

import '~chessboardjs/chessboard2.min.css'
import '~chessboardjs/chessboard2.min.js'

const props = defineProps({
  gameId: String,
  playerColor: String
})

function playMove(move: Move) {
  client.sendMove(props.gameId!, move)
}

onMounted(() => {
  const game = new Chess()

  const boardConfig = {
    draggable: true,
    orientation: props.playerColor === 'w' ? 'white' : 'black',
    position: game.fen(),
    onDragStart,
    onDrop
  }
  const board = (window as any)['Chessboard2']('myBoard', boardConfig)

  const statusEl = byId('gameStatus')
  const fenEl = byId('gameFEN')
  const pgnEl = byId('gamePGN')

  updateStatus()

  client.registerMovePlayedCallback((event) => {
    if (event.playerId !== client.playerUUID) {
      console.log(`Received move ${event.move.from} to ${event.move.to}`)
      try {
        const move = game.move({
          from: event.move.from,
          to: event.move.to,
          promotion: 'q' // NOTE: always promote to a queen for simplicity
        })

        if (move) {
          board.fen(game.fen(), () => {
            updateStatus()
          })
        }
      } catch (error) {
        console.error(error)
      }
    }
  })

  function onDragStart(dragStartEvt: { piece: any; square: any }) {
    if (game.isGameOver()) return false

    if (game.turn() !== props.playerColor) return false

    if (game.turn() === 'w' && !isWhitePiece(dragStartEvt.piece)) return false
    if (game.turn() === 'b' && !isBlackPiece(dragStartEvt.piece)) return false
  }

  function isWhitePiece(piece: string) {
    return /^w/.test(piece)
  }
  function isBlackPiece(piece: string) {
    return /^b/.test(piece)
  }

  function onDrop(dropEvt: { source: any; target: any }) {
    // see if the move is legal
    try {
      const move = game.move({
        from: dropEvt.source,
        to: dropEvt.target,
        promotion: 'q' // NOTE: always promote to a queen for simplicity
      })

      if (move) {
        board.fen(game.fen(), () => {
          updateStatus()
          playMove(move)
        })
      }
    } catch (error) {
      return 'snapback'
    }
  }

  function updateStatus() {
    let statusHTML = ''
    const whosTurn = game.turn() === 'w' ? 'White' : 'Black'

    if (!game.isGameOver()) {
      if (game.isCheck()) statusHTML = whosTurn + ' is in check! '
      statusHTML = statusHTML + whosTurn + ' to move.'
    } else if (game.isCheckmate() && game.turn() === 'w') {
      statusHTML = 'Game over: white is in checkmate. Black wins!'
    } else if (game.isCheckmate() && game.turn() === 'b') {
      statusHTML = 'Game over: black is in checkmate. White wins!'
    } else if (game.isStalemate() && game.turn() === 'w') {
      statusHTML = 'Game is drawn. White is stalemated.'
    } else if (game.isStalemate() && game.turn() === 'b') {
      statusHTML = 'Game is drawn. Black is stalemated.'
    } else if (game.isThreefoldRepetition()) {
      statusHTML = 'Game is drawn by threefold repetition rule.'
    } else if (game.isInsufficientMaterial()) {
      statusHTML = 'Game is drawn by insufficient material.'
    } else if (game.isDraw()) {
      statusHTML = 'Game is drawn by fifty-move rule.'
    }

    statusEl!.innerHTML = statusHTML
    fenEl!.innerHTML = game.fen()
    pgnEl!.innerHTML = game.pgn()
  }

  function byId(id: string) {
    return document.getElementById(id)
  }
})
</script>

<template>
  <h4>Game</h4>
  <div class="p-1 border rounded">
    <div class="row">
      <div class="col-lg">
        <div id="myBoard" class="mx-auto mx-lg-0" style="width: 100%"></div>
      </div>
      <div class="col-lg">
        <label>Status:</label>
        <div id="gameStatus"></div>
        <label>FEN:</label>
        <div id="gameFEN"></div>
        <label>PGN:</label>
        <div id="gamePGN"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#myBoard {
  max-width: 500px;
}
</style>
