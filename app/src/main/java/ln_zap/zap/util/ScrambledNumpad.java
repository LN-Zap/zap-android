package ln_zap.zap.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleImmutableEntry;

public class ScrambledNumpad {

    private List<SimpleImmutableEntry<Integer,Integer>> numpad;
    private List<Integer> positions;

    public ScrambledNumpad() {

        positions = new ArrayList<>();
        positions.add(0);
        positions.add(1);
        positions.add(2);
        positions.add(3);
        positions.add(4);
        positions.add(5);
        positions.add(6);
        positions.add(7);
        positions.add(8);
        positions.add(9);

        numpad = new ArrayList<>();

        init();

    }

    private void init()  {

        SecureRandom random = new SecureRandom();

        for(int i = 0; i < 10; i++)  {

            int randomPos = random.nextInt(positions.size());
            SimpleImmutableEntry<Integer,Integer> pair = new SimpleImmutableEntry<>(i, positions.get(randomPos));
            positions.remove(randomPos);
            numpad.add(pair);

        }

    }

    public List<SimpleImmutableEntry<Integer,Integer>> getNumpad()  {
        return numpad;
    }

}
