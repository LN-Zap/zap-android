package ln_zap.zap.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleImmutableEntry;

/**
 * This class helps to defend against some attack vectors.
 * It securely randomizes the positions at which the digits of the numpad will be shown.
 */
public class ScrambledNumpad {

    private List<SimpleImmutableEntry<Integer, Integer>> mNumpad;
    private List<Integer> mPositions;

    public ScrambledNumpad() {

        mPositions = new ArrayList<>();
        mPositions.add(0);
        mPositions.add(1);
        mPositions.add(2);
        mPositions.add(3);
        mPositions.add(4);
        mPositions.add(5);
        mPositions.add(6);
        mPositions.add(7);
        mPositions.add(8);
        mPositions.add(9);

        mNumpad = new ArrayList<>();

        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 10; i++) {

            int randomPos = random.nextInt(mPositions.size());
            SimpleImmutableEntry<Integer, Integer> pair = new SimpleImmutableEntry<>(i, mPositions.get(randomPos));
            mPositions.remove(randomPos);
            mNumpad.add(pair);

        }

    }


    public List<SimpleImmutableEntry<Integer, Integer>> getNumpad() {
        return mNumpad;
    }

}
