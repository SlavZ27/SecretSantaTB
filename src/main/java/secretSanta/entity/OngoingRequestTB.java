package secretSanta.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import secretSanta.Util.Messages;
import secretSanta.command.Command;

import java.util.HashMap;

/**
 * This entity is engaged in creating a data model for the ability to make a Unfinished Request.
 * This entity is used in several classes.
 * The class must have constructor, getters, setters.
 * Since other classes need them for their functioning and for better data protection.
 */

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "ongoing_request_telegram")
@Table(indexes = @Index(columnList = "telegram_chat_id"))
public class OngoingRequestTB {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @OneToOne
    @JoinColumn(name = "telegram_chat_id", unique = true)
    @NotNull
    private Chat chat;
    @Column(name = "command", nullable = false)
    @Enumerated(EnumType.STRING)
    private Command command;
    @Column(name = "param")
    private String param;

    public HashMap<String, String> getParams() {
        if (param == null || param.isEmpty()) {
            return new HashMap<>();
        }
        HashMap<String, String> result = new HashMap<>();
        String[] split = param.split(Messages.PARAM_SPLIT_SYMBOL);
        for (String s : split) {
            int i = s.indexOf("=");
            if (i > 0 && s.length() > i + 1) {
                result.put(s.substring(0, i), s.substring(i + 1));
            }
        }
        return result;
    }

}
