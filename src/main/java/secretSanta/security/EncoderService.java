package secretSanta.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class EncoderService {
    private int countToken = 20;
    private int countTokenDB = 10;
    private final PasswordEncoder passwordEncoder;

    private static final String PAS_PREFIX = "{bcrypt}";

    public EncoderService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String generateToken() {
        return generateString(countToken);
    }

    public String getHash(String token) {
        return PAS_PREFIX + passwordEncoder.encode(token);
    }
    public String getTokenDB(String token) {
        return token.substring(0, countTokenDB);
    }

    public String generateHash() {
        return PAS_PREFIX + passwordEncoder.encode(generateToken());
    }

    public boolean matches(String token, String hash) {
        if (hash.startsWith(PAS_PREFIX)) {
            return passwordEncoder.matches(token, hash.substring(PAS_PREFIX.length()));
        }
        return passwordEncoder.matches(token, hash);

    }

    private String generateString(int count) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
}
