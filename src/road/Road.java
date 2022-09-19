package road;

import java.util.Random;

public class Road {
    public static RoadType[] roadAhead;
    public  static RoadType[] getRoadAhead() {
        return roadAhead;
    }
    public Road(){}

    private static final int UPPER_BOUND = 10;
    public static void buildRoad() {
        roadAhead = new RoadType[UPPER_BOUND];

        Random random = new Random();

        int randomStep = random.nextInt(UPPER_BOUND);
        RoadType type = RoadType.INVALID_READ;
        for (int i = 0;i<UPPER_BOUND;i++) {
            if (randomStep % 3 == 0) {
                type = RoadType.NORMAL_ROAD;
            } else if (randomStep % 3 == 1) {
                type = RoadType.POT_HOLE;
            } else if (randomStep % 3 == 2) {
                type = RoadType.WATER_SPLASH;
            }
            roadAhead[i] = type;
//            LocationStep step = new LocationStep(type, Calendar.getInstance().getTime().getTime());
            randomStep = random.nextInt(UPPER_BOUND);
        }
        System.out.println("Road ahead size: " + roadAhead.length);
    }

    public static void main(String[] args) {
        try{
            System.out.println("inside road build");
            Road.buildRoad();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
