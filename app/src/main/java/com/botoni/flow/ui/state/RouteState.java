package com.botoni.flow.ui.state;

import java.util.List;

public class RouteState {
    private final List<String> points;
    private final double distance;

    public RouteState(List<String> points, double distance) {
        this.points = points;
        this.distance = distance;
    }

    public List<String> getPoints() {
        return points;
    }

    public double getDistance() {
        return distance;
    }
}