package rit.swen.architecture.road;

import java.util.Calendar;
import java.util.Random;

public class Road {
    private static final int UPPER_BOUND = 1000;
    public static void buildRoad() {
        Random random = new Random();
        int randomStep = random.nextInt(UPPER_BOUND);
        RoadType type = RoadType.INVALID_READ;
        if (randomStep % 3 == 0) {
            type = RoadType.NORMAL_ROAD;
        } else if (randomStep % 3 == 1) {
            type = RoadType.POT_HOLE;
        } else if (randomStep % 3 == 2) {
            type = RoadType.WATER_SPLASH;
        }
        LocationStep step = new LocationStep(type, Calendar.getInstance().getTime().getTime());
    }
}
