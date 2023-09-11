import { v4 as uuidv4 } from 'uuid'
import { Client } from '@stomp/stompjs'

class MicrosChessClient {
  private readonly playerUUID: string
  private readonly stompClient: Client
  private _connected: boolean = false
  private readonly gameProposals: any[] = []
  private currentGameUUID?: string

  public get connected(): boolean {
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
    this._connected = connected
    this.subscribeGameProposalsChannel()
  }

  private subscribeGameProposalsChannel() {
    this.stompClient.subscribe('/topic/game-proposals', (message) => {
      const gameProposal = JSON.parse(message.body)
      this.gameProposals.push(gameProposal)
    })
    console.log('Subscribed to game-proposals channel')
  }

  private subscribePlayerChannel(callback: (event: any) => string) {
    this.stompClient.subscribe(`/topic/player/${this.playerUUID}`, (message) => {
      const event = JSON.parse(message.body)
      this.currentGameUUID = event.gameId
      if (event.type == 'CREATED') {
        callback(event)
      }
    })
    console.log('Subscribed to player channel')
  }

  public connect() {
    this.stompClient.activate()
  }

  public disconnect() {
    this.stompClient.deactivate()
  }

  public sendCreateGameProposalRequest(gameAcceptedCallback: (event: any) => string) {
    this.subscribePlayerChannel(gameAcceptedCallback)
    this.stompClient.publish({
      destination: '/app/create-game-proposal',
      body: `${this.playerUUID}`
    })
  }

  public sendAcceptGameProposalRequest(
    gameProposalId: string,
    gameAcceptedCallback: (event: any) => string
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
