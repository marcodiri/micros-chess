package io.github.marcodiri.webservice.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import io.github.marcodiri.lobbyservice.api.event.GameProposalCreated;

@Controller
public class EventController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void notifyGameProposalCreated(GameProposalCreated event) {
        this.simpMessagingTemplate.convertAndSend("/topic/game-proposals", event);
    }

}
