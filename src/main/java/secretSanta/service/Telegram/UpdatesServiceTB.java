package secretSanta.service.Telegram;

import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import secretSanta.command.Command;
import secretSanta.entity.Chat;
import secretSanta.entity.OngoingRequestTB;
import secretSanta.entity.User;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.UserDetailsCustom;
import secretSanta.service.AuthenticateService;

import java.util.Optional;

@Service
public class UpdatesServiceTB {
    private final Logger logger = LoggerFactory.getLogger(UpdatesServiceTB.class);
    private final CommandServiceTB commandServiceTB;
    private final AuthenticateService authenticateService;
    private final NavigateServiceTB navigateServiceTB;
    private final TechServiceTB techServiceTB;
    private final CellServiceTB cellServiceTB;
    private final UserServiceTB userServiceTB;
    private final CalendarServiceTB calendarServiceTB;
    private final SecretSantaServiceTB secretSantaServiceTB;

    public UpdatesServiceTB(CommandServiceTB commandServiceTB,
                            AuthenticateService authenticateService,
                            NavigateServiceTB navigateServiceTB,
                            TechServiceTB techServiceTB,
                            CellServiceTB cellServiceTB,
                            UserServiceTB userServiceTB,
                            CalendarServiceTB calendarServiceTB, SecretSantaServiceTB secretSantaServiceTB) {
        this.commandServiceTB = commandServiceTB;
        this.authenticateService = authenticateService;
        this.navigateServiceTB = navigateServiceTB;
        this.techServiceTB = techServiceTB;
        this.cellServiceTB = cellServiceTB;
        this.userServiceTB = userServiceTB;
        this.calendarServiceTB = calendarServiceTB;
        this.secretSantaServiceTB = secretSantaServiceTB;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            logger.debug("Method processUpdate detected null update");
            return;
        }
        if (detectEmptyCommand(update)) {
            sendCallbackAnswer(update);
            logger.debug("Method processUpdate detected empty command");
            return;
        }
        Authentication authenticate = authenticateService.authenticate(update);
        User user = getCurrentUser(authenticate);
        Chat chat = user.getChatTelegram();

        Command command = UpdateMapperTB.getCommand(update);
        if (command == null) {
            command = Optional.ofNullable(chat)
                    .map(Chat::getOngoingRequestTB)
                    .map(OngoingRequestTB::getCommand)
                    .orElse(null);
        }
        if (command == null) {
            techServiceTB.sendUnknownProcess();
        } else {
//            if (!commandServiceTB.approveLaunchCommand(command)) {
//                techServiceTB.sendSorryIKnowThis();
//                return;
//            }
            switch (command) {
                ////////// index menu 0 "main"
                case START -> navigateServiceTB.menuStart0();
                case JOIN_GROUP -> cellServiceTB.joinGroup(update);
                case CREATE_GROUP -> cellServiceTB.createGroup(update);
                case MY_CELLS -> navigateServiceTB.menuMyCells3();
                case MANAGE_CELL -> navigateServiceTB.menuManageCell3(update);
                //////////////////// index menu 3 "SECRET_SANTA"
                case ABOUT_GROUP -> cellServiceTB.aboutGroup(update);
                case EXIT_GROUP -> cellServiceTB.exitGroup(update);
                case LIST_PARTICIPANTS -> cellServiceTB.listParticipants(update);
                case MY_DATA -> navigateServiceTB.menuMyData4(update);
                case CALC_CELL -> secretSantaServiceTB.calcCell(update);
                case WARD -> navigateServiceTB.menuWard5(update);
                case MENU_BACK3 -> navigateServiceTB.menuStart0();
                ////////////////////////////// index menu 4 "MY_DATA"
                case CHANGE_ALIAS_NAME -> userServiceTB.changeAliasName(update);
                case CHANGE_ALIAS_DREAM -> userServiceTB.changeDream(update);
                case MENU_BACK4 -> navigateServiceTB.menuManageCell3(update);
                ////////////////////////////// index menu 5 "WARD"
                case REQUEST_WISHES -> secretSantaServiceTB.requestWishes(update);
                case MENU_BACK5 -> navigateServiceTB.menuManageCell3(update);
                //
                case CHANGE_CALENDAR -> calendarServiceTB.changeCalendar(update);
                case CLOSE_UNFINISHED_REQUEST -> techServiceTB.closeUnfinishedRequest(update);
                case EMPTY_CALLBACK_DATA_FOR_BUTTON -> {
                }
            }
        }
        sendCallbackAnswer(update);
    }

    private void sendCallbackAnswer(Update update) {
        String callbackQueryId = UpdateMapperTB.getCallbackQueryId(update);
        if (callbackQueryId != null && !callbackQueryId.isBlank()) {
            techServiceTB.sendCallbackAnswer(callbackQueryId);
        }
    }

    private Boolean detectEmptyCommand(Update update) {
        return update.callbackQuery() != null &&
                update.callbackQuery().data() != null &&
                update.callbackQuery().data().equals(Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());
    }

    private User getCurrentUser(Authentication authentication) {
        return Optional.ofNullable(
                ((UserDetailsCustom)
                        authentication.getPrincipal()).getUser()
        ).orElseThrow(() -> new UserNotFoundException("User is absent"));
    }

}

