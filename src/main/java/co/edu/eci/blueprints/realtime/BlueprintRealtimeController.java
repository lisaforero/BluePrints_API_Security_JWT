package co.edu.eci.blueprints.realtime;

import co.edu.eci.blueprints.persistence.BlueprintNotFoundException;
import co.edu.eci.blueprints.services.BlueprintsServices;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Controller
@Validated
public class BlueprintRealtimeController {

    private final BlueprintsServices services;
    private final SimpMessagingTemplate messagingTemplate;

    public BlueprintRealtimeController(BlueprintsServices services, SimpMessagingTemplate messagingTemplate) {
        this.services = services;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/draw")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public void draw(@Valid @Payload DrawPointMessage message) throws BlueprintNotFoundException {
        services.addPoint(message.author(), message.name(), message.point().x(), message.point().y());
        DrawPointBroadcast payload =
                new DrawPointBroadcast(message.author(), message.name(), List.of(message.point()));

        String topic = "/topic/blueprints.%s.%s".formatted(message.author(), message.name());
        messagingTemplate.convertAndSend(topic, payload);
    }

    @MessageExceptionHandler({ BlueprintNotFoundException.class, IllegalArgumentException.class })
    @SendToUser("/queue/errors")
    public String handleDrawErrors(Exception exception) {
        return exception.getMessage();
    }
}
