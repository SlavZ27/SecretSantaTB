package secretSanta.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import secretSanta.command.Command;

@Converter(autoApply = true)
public class CommandConverter implements AttributeConverter<Command, String> {

    @Override
    public String convertToDatabaseColumn(Command command) {
        if (command == null) {
            return null;
        }
        return command.getTextCommand();
    }

    @Override
    public Command convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return Command.fromStringUpperCase(s);
    }
}
