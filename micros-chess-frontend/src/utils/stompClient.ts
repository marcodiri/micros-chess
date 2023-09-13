import { v4 as uuidv4 } from 'uuid'
import { Client } from '@stomp/stompjs'
import { ref, type Ref } from 'vue'

class MicrosChessClient {
  public readonly playerUUID: string
  private readonly stompClient: Client
  private _connected = ref(false)
  private subscribedToPlayerChannel: boolean = false
  public readonly gameProposals: Ref<any[]> = ref([])
  private currentGameUUID?: string
  private gameAcceptedCallbacks = new Map<(event: any) => void, (event: any) => void>()
  private movePlayedCallbacks = new Map<(event: any) => void, (event: any) => void>()

  public get isConnected() {
    return this._connected
  }

  public registerGameAcceptedCallback(callback: (event: any) => void) {
    this.gameAcceptedCallbacks.set(callback, callback)
  }

  public removeGameAcceptedCallback(callback: (event: any) => void) {
    return this.gameAcceptedCallbacks.delete(callback)
  }

  public registerMovePlayedCallback(callback: (event: any) => void) {
    this.movePlayedCallbacks.set(callback, callback)
  }

  public removeMovePlayedCallback(callback: (event: any) => void) {
    return this.movePlayedCallbacks.delete(callback)
  }

  constructor(public uri: string) {
    this.playerUUID = uuidv4()
    this.stompClient = new Client({
      brokerURL: uri,
      onConnect: (frame) => {
        this.setConnected(true)
        console.log(frame)
      },
      onDisconnect: (frame) => {
        this.setConnected(false)
        console.log(frame)
      },
      onWebSocketError: (error) => {
        console.error('Error with websocket', error)
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message'])
        console.error('Additional details: ' + frame.body)
      }
    })
  }

  private setConnected(connected: boolean) {
    this._connected.value = connected
    if (connected) {
      this.subscribeGameProposalsChannel()
    }
  }

  private subscribeGameProposalsChannel() {
    this.stompClient.subscribe('/topic/game-proposals', (message) => {
      const gameProposal = JSON.parse(message.body)
      this.gameProposals.value.push(gameProposal)
      console.log(gameProposal)
    })
    console.log('Subscribed to game-proposals channel')
  }

  private subscribePlayerChannel() {
    if (!this.subscribedToPlayerChannel) {
      this.stompClient.subscribe(`/topic/player/${this.playerUUID}`, (message) => {
        const event = JSON.parse(message.body)
        if (event.type == 'CREATED') {
          this.currentGameUUID = event.gameId
          this.subscribeGameChannel(event.gameId)
          this.gameAcceptedCallbacks.forEach((callback) => {
            callback(event)
          })
        }
      })
      this.subscribedToPlayerChannel = true
      console.log('Subscribed to player channel')
    }
  }

  private subscribeGameChannel(gameId: string) {
    this.stompClient.subscribe(`/topic/game/${gameId}`, (message) => {
      const event = JSON.parse(message.body)
      if (event.type == 'MOVE') {
        this.movePlayedCallbacks.forEach((callback) => {
          callback(event)
        })
      }
    })
    console.log('Subscribed to game channel')
  }

  public connect() {
    this.stompClient.activate()
  }

  public disconnect() {
    this.stompClient.deactivate()
  }

  public sendCreateGameProposalRequest() {
    this.subscribePlayerChannel()
    this.stompClient.publish({
      destination: '/app/create-game-proposal',
      body: this.playerUUID
    })
    console.log(`Creating game proposal`)
  }

  public sendAcceptGameProposalRequest(gameProposalId: string) {
    this.subscribePlayerChannel()
    this.stompClient.publish({
      destination: `/app/accept-game-proposal/${gameProposalId}`,
      body: this.playerUUID
    })
    console.log(`Accepting ${gameProposalId}`)
  }

  public sendMove(move: { from: string; to: string }) {
    if (this.currentGameUUID) {
      this.stompClient.publish({
        destination: `/app/game/${this.currentGameUUID}/${this.playerUUID}`,
        body: JSON.stringify(move)
      })
      console.log(`Playing move ${move.from} to ${move.to}`)
    }
  }
}

export const client = new MicrosChessClient('ws://localhost:8080/ws')
