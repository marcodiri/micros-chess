<script setup lang="ts">
// import '@/assets/chessboard.js'
import { onMounted } from 'vue'
import { Chess } from 'chess.js'
import '~chessboardjs/chessboard2.min.css?url'
import '~chessboardjs/chessboard2.min.js?url'

onMounted(() => {
  const game = new Chess()

  const boardConfig = {
    draggable: true,
    position: game.fen(),
    onDragStart,
    onDrop
    // onSnapEnd
  }
  const board = (window as any)['Chessboard2']('myBoard', boardConfig)

  const statusEl = byId('gameStatus')
  const fenEl = byId('gameFEN')
  const pgnEl = byId('gamePGN')

  updateStatus()

  function onDragStart(dragStartEvt: { piece: any; square: any }) {
    // do not pick up pieces if the game is over
    if (game.isGameOver()) return false

    // only pick up pieces for the side to move
    if (game.turn() === 'w' && !isWhitePiece(dragStartEvt.piece)) return false
    if (game.turn() === 'b' && !isBlackPiece(dragStartEvt.piece)) return false

    // what moves are available to from this square?
    const legalMoves = game.moves({
      square: dragStartEvt.square,
      verbose: true
    })

    // place Circles on the possible target squares
    legalMoves.forEach((move) => {
      board.addCircle(move.to)
    })
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
        promotion: 'q' // NOTE: always promote to a queen for example simplicity
      })

      if (move) {
        // update the board position with the new game position, then update status DOM elements
        board.fen(game.fen(), () => {
          updateStatus()
        })
      }
    } catch (error) {
      return 'snapback'
    }

    // remove all Circles from the board
    board.clearCircles()
  }

  // update the board position after the piece snap
  // for castling, en passant, pawn promotion
  // function onSnapEnd () {
  //   board.position(game.fen())
  // }

  // update DOM elements with the current game status
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
  <div id="myBoard" style="width: 400px"></div>

  <label>Status:</label>
  <div id="gameStatus"></div>
  <label>FEN:</label>
  <div id="gameFEN"></div>
  <label>PGN:</label>
  <div id="gamePGN"></div>
</template>
