package io.github.marcodiri.webservice.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import io.github.marcodiri.gameservice.api.event.GameCreated;
import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;

@Controller
public class EventController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void notifyGameProposalCreated(GameProposalCreated event) {
        // FIXME: should not send back creatorId
        this.simpMessagingTemplate.convertAndSend("/topic/game-proposals", event);
    }

    public void notifyGameCreated(GameCreated event) {
        // FIXME: should not send back players Id
        this.simpMessagingTemplate.convertAndSend("/topic/player/" + event.getPlayer1Id(), event);
        this.simpMessagingTemplate.convertAndSend("/topic/player/" + event.getPlayer2Id(), event);
    }

}
