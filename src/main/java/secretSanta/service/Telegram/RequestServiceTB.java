package secretSanta.service.Telegram;

import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import secretSanta.Util.Messages;
import secretSanta.entity.Chat;
import secretSanta.entity.OngoingRequestTB;
import secretSanta.command.Command;
import secretSanta.entity.User;
import secretSanta.exception.UserNotFoundException;
import secretSanta.repository.OngoingRequestTBRepository;
import secretSanta.security.UserDetailsCustom;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class progress command unfinished request telegram, using method repository date base
 * {@link OngoingRequestTBRepository }
 */
@Service
public class RequestServiceTB {
    private final OngoingRequestTBRepository ongoingRequestTBRepository;

    public RequestServiceTB(OngoingRequestTBRepository ongoingRequestTBRepository) {
        this.ongoingRequestTBRepository = ongoingRequestTBRepository;
    }

    public void save(Chat chat, Command command, Pair<String, String>... parameters) {
        OngoingRequestTB ongoingRequestTB = new OngoingRequestTB();
        ongoingRequestTB.setId(chat.getId());
        ongoingRequestTB.setChat(chat);
        ongoingRequestTB.setCommand(command);
        if (parameters != null) {
            String param = Arrays.stream(parameters)
                    .map(pair -> {
                        if (pair.getFirst().contains(Messages.PARAM_SPLIT_SYMBOL) ||
                                pair.getSecond().contains(Messages.PARAM_SPLIT_SYMBOL)) {
                            throw new IllegalArgumentException(
                                    "Params must don't contains " + Messages.PARAM_SPLIT_SYMBOL);
                        }
                        return pair.getFirst() + "=" + pair.getSecond();
                    })
                    .collect(Collectors.joining(Messages.PARAM_SPLIT_SYMBOL));
            ongoingRequestTB.setParam(param);
        }
        ongoingRequestTBRepository.save(ongoingRequestTB);
    }

    public OngoingRequestTB find(Chat chat) {
        return ongoingRequestTBRepository.findByIdChat(chat.getId())
                .orElse(null);
    }

    public OngoingRequestTB del(Chat chat) {
        if (chat.getId() == null) {
            throw new IllegalArgumentException("chat.Id must be not null");
        }
        OngoingRequestTB requestTB = ongoingRequestTBRepository.findByIdChat(chat.getId()).orElse(null);
        if (requestTB != null) {
            ongoingRequestTBRepository.delete(chat.getId());
        }
        return requestTB;
    }

    private User getCurrentUser() {
        return Optional.ofNullable(
                ((UserDetailsCustom)
                        SecurityContextHolder.getContext().getAuthentication()
                                .getPrincipal())
                        .getUser()
        ).orElseThrow(() -> new UserNotFoundException("User is absent"));
    }

    private OngoingRequestTB getOngoingRequestCurrentUser() {
        try {
            return getCurrentUser().getChatTelegram().getOngoingRequestTB();
        } catch (UserNotFoundException ignored) {
            return null;
        }

    }

}
