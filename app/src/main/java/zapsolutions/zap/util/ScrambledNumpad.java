package zapsolutions.zap.util;

import java.security.SecureRandom;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;

/**
 * This class helps to protect the PIN.
 * It is used to securely randomizes the digits on the numpad.
 * This makes it impossible for an attacker to guess a PIN
 * by observing the finger motion of a user.
 */
public class ScrambledNumpad {

    private final List<SimpleImmutableEntry<Integer, Integer>> mNumpad;

    public ScrambledNumpad() {
        List<Integer> positions;
        positions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            positions.add(i);
        }
        mNumpad = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 10; i++) {
            int randomPos = random.nextInt(positions.size());
            SimpleImmutableEntry<Integer, Integer> pair = new SimpleImmutableEntry<>(i, positions.get(randomPos));
            positions.remove(randomPos);
            mNumpad.add(pair);
        }
    }

    public List<SimpleImmutableEntry<Integer, Integer>> getNumpad() {
        return mNumpad;
    }

}
