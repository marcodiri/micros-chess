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

  public get isConnected() {
    return this._connected
  }

  constructor(public uri: string) {
    this.playerUUID = uuidv4()
    this.stompClient = new Client({
      brokerURL: uri,
      onConnect: (frame) => {
        this.setConnected(true)
        console.log('Connected: ' + frame)
      },
      onDisconnect: (frame) => {
        this.setConnected(false)
        console.log('Disconnected: ' + frame)
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

  private subscribePlayerChannel(callback: (event: any) => void) {
    if (!this.subscribedToPlayerChannel) {
      this.stompClient.subscribe(`/topic/player/${this.playerUUID}`, (message) => {
        const event = JSON.parse(message.body)
        this.currentGameUUID = event.gameId
        if (event.type == 'CREATED') {
          callback(event)
        }
      })
      this.subscribedToPlayerChannel = true
      console.log('Subscribed to player channel')
    }
  }

  public connect() {
    this.stompClient.activate()
  }

  public disconnect() {
    this.stompClient.deactivate()
  }

  public sendCreateGameProposalRequest(gameAcceptedCallback: (event: any) => void) {
    this.subscribePlayerChannel(gameAcceptedCallback)
    this.stompClient.publish({
      destination: '/app/create-game-proposal',
      body: `${this.playerUUID}`
    })
    console.log(`Creating game proposal`)
  }

  public sendAcceptGameProposalRequest(
    gameProposalId: string,
    gameAcceptedCallback: (event: any) => void
  ) {
    this.subscribePlayerChannel(gameAcceptedCallback)
    this.stompClient.publish({
      destination: '/app/accept-game-proposal/' + gameProposalId,
      body: `${this.playerUUID}`
    })
    console.log(`Accepting ${gameProposalId}`)
  }

  public sendMove(move: string) {
    if (this.currentGameUUID) {
      console.log(`Playing move ${move}`)
    }
  }
}

export const client = new MicrosChessClient('ws://localhost:8080/ws')
