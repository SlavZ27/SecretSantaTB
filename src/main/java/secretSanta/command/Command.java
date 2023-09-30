package secretSanta.command;

import lombok.Getter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import secretSanta.security.Roles;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Getter
public enum Command {
    ///////////////////////////// index menu 0 "main"
    START(
            0,
            "/start",
            "Главное меню",
            false,
            true,
            0,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    JOIN_GROUP(
            1,
            "/JOIN_GROUP",
            "Вступить в группу",
            true,
            true,
            0,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    CREATE_GROUP(
            2,
            "/CREATE_GROUP",
            "Создать группу",
            true,
            true,
            0,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    MY_CELLS(
            3,
            "/MY_CELLS",
            "Тайный Санта",
            true,
            true,
            0,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    MANAGE_CELL(
            3,
            "/MANAGE_CELL",
            "Управление Группой",
            false,
            false,
            4,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    CHANGE_ALIAS(
            5,
            "/CHANGE_ALIAS",
            "Изменить алиас",
            false,
            false,
            0,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    ///////////////////////////// index menu 3 "SECRET_SANTA"
    ABOUT_GROUP(
            1,
            "/ABOUT_GROUP",
            "О группе",
            true,
            false,
            3,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    EXIT_GROUP(
            2, "/EXIT_GROUP",
            "Выйти из группы",
            true,
            false,
            3,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    LIST_PARTICIPANTS(
            3,
            "/LIST_PARTICIPANTS",
            "Список участников",
            true,
            false,
            3,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    MY_DATA(
            4,
            "/MY_DATA",
            "Мои данные",
            true,
            false,
            3,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    CALC_CELL(
            5,
            "/CALC_CELL",
            "Распределить участников",
            true,
            false,
            3,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    WARD(
            6,
            "/WARD",
            "Подопечный",
            true,
            false,
            3,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    MENU_BACK3(
            99,
            "/MENU_BACK3",
            "Назад",
            true,
            false,
            3,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    ///////////////////////////// index menu 4 "MY_DATA"
    CHANGE_ALIAS_NAME(
            1, "/CHANGE_ALIAS_NAME",
            "Изменить имя",
            true,
            false,
            4,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    CHANGE_ALIAS_DREAM(
            2, "/CHANGE_ALIAS_DREAM",
            "Изменить желание",
            true,
            false,
            4,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    MENU_BACK4(
            99,
            "/MENU_BACK4",
            "Назад",
            true,
            false,
            4,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    ///////////////////////////// index menu 5 "WARD"
    REQUEST_WISHES(
            1, "/REQUEST_WISHES",
            "Запросить желания",
            true,
            false,
            5,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    MENU_BACK5(
            99,
            "/MENU_BACK5",
            "Назад",
            true,
            false,
            5,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    /////////////////////////////
    CHANGE_CALENDAR(
            99,
            "/CHANGE_CALENDAR",
            "CHANGE_CALENDAR",
            false,
            false,
            0,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    CLOSE_UNFINISHED_REQUEST(
            99,
            "/CLOSE_UNFINISHED_REQUEST",
            "Cancel",
            false,
            false,
            0,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT)),
    EMPTY_CALLBACK_DATA_FOR_BUTTON(
            -1,
            "...",
            "...",
            false,
            false,
            0,
            Set.of(
                    Roles.ADMIN,
                    Roles.CLIENT));

    private final int order;
    private final String textCommand;
    private final String nameButton;
    private final Boolean isShow;
    private final Boolean isFunctionMenu;
    private final int indexMenu;
    private final Set<Roles> roles;

    Command(int order, String textCommand, String nameButton, Boolean isShow, Boolean isFunctionMenu, int indexMenu, Set<Roles> roles) {
        this.order = order;
        this.textCommand = textCommand;
        this.nameButton = nameButton;
        this.isShow = isShow;
        this.isFunctionMenu = isFunctionMenu;
        this.indexMenu = indexMenu;
        this.roles = roles;
    }

    public static Command fromStringIteration(String textCommand) {
        for (Command command : Command.values()) {
            if (command.getTextCommand().equalsIgnoreCase(textCommand)) {
                return command;
            }
        }
        return null;
    }

    public static Command fromStringUpperCase(String textCommand) {
        if (textCommand == null || textCommand.length() < 2) {
            return null;
        }
        if (textCommand.startsWith("/")) {
            textCommand = textCommand.substring(1).toUpperCase();
        }
        try {
            return Command.valueOf(Command.class, textCommand);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Set<GrantedAuthority> getGrantedAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority(r.getRole()))
                .collect(Collectors.toSet());
    }

    @Cacheable
    public static TreeSet<Command> getOnlyShowCommandIndexMenu(int indexMenu) {
        TreeSet<Command> commands = new TreeSet<>(new CommandComparatorOrder());
        for (Command value : Command.values()) {
            if (value.getIndexMenu() == indexMenu && value.isShow) {
                commands.add(value);
            }
        }
        return commands;
    }

    public static TreeSet<Command> getFunctionCommandSort() {
        TreeSet<Command> commands = new TreeSet<>(new CommandComparatorMenu());
        for (Command value : Command.values()) {
            if (value.getIsFunctionMenu()) {
                commands.add(value);
            }
        }
        return commands;
    }

    private static class CommandComparatorMenu implements Comparator<Command> {
        @Override
        public int compare(Command c1, Command c2) {
            int result = c1.getIndexMenu() - c2.getIndexMenu();
            if (result == 0) {
                result = c1.getOrder() - c2.getOrder();
            }
            return result != 0 ? result : c1.name().compareTo(c2.name());
        }
    }

    private static class CommandComparatorOrder implements Comparator<Command> {
        @Override
        public int compare(Command c1, Command c2) {
            int result = c1.getOrder() - c2.getOrder();
            return result != 0 ? result : c1.name().compareTo(c2.name());
        }
    }
}
