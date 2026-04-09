package co.edu.eci.blueprints.realtime;

import co.edu.eci.blueprints.model.Point;

import java.util.List;

public record DrawPointBroadcast(
        String author,
        String name,
        List<Point> points
) {
}
