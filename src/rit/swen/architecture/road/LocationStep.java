package rit.swen.architecture.road;

public class LocationStep {
    int coordinateStep;
    long time;
    RoadType roadType;

    public LocationStep(RoadType roadType, long time) {
        this.roadType = roadType;
        this.time = time;
    }

    public void setRoadType(RoadType roadType) {
        this.roadType = roadType;
    }

    public RoadType getRoadType() {
        return roadType;
    }

    public int getCoordinateStep() {
        return coordinateStep;
    }

    public void setCoordinateStep(int coordinateStep) {
        this.coordinateStep = coordinateStep;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
