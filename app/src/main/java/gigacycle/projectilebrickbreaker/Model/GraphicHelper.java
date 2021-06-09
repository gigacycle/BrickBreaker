package gigacycle.projectilebrickbreaker.Model;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

/**
 * Created by Gigacycle on 10/15/2018.
 */

public class GraphicHelper {
    public static int ScreenWidth;
    public static int ScreenHeight;
    public static Rect GameArea;
    public static float Scale;

    static DistResult DistOfPointFromSegmentLine(Point point, Point a, Point b){
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        PointF closest = new PointF(a.x, a.y);;
        if ((dx == 0) && (dy == 0))
        {
            dx = point.x - a.x;
            dy = point.y - a.y;
            return new DistResult(closest, (float) Math.sqrt(dx * dx + dy * dy));
        }

        // Calculate the t that minimizes the distance.
        float t = ((point.x - a.x) * dx + (point.y - a.y) * dy) /
                (dx * dx + dy * dy);

        // See if this represents one of the segment's
        // end points or a point in the middle.
        if (t < 0)
        {
            dx = point.x - a.x;
            dy = point.y - a.y;
        }
        else if (t > 1)
        {
            closest = new PointF(b.x, b.y);
            dx = point.x - b.x;
            dy = point.y - b.y;
        }
        else
        {
            closest = new PointF(a.x + t * dx, a.y + t * dy);
            dx = point.x - closest.x;
            dy = point.y - closest.y;
        }

        return new DistResult(closest, (float)Math.sqrt(dx * dx + dy * dy));
    }

    public static int getColorOfDegradable(int colorStart, int colorEnd, int percent){
        return Color.rgb(
                getColorOfDegradableCalculation(Color.red(colorStart), Color.red(colorEnd), percent),
                getColorOfDegradableCalculation(Color.green(colorStart), Color.green(colorEnd), percent),
                getColorOfDegradableCalculation(Color.blue(colorStart), Color.blue(colorEnd), percent)
        );
    }

    private static int getColorOfDegradableCalculation(int colorStart, int colorEnd, int percent) {
        if (colorStart > colorEnd)
            return Math.max(colorStart, colorEnd) - (((Math.min(colorStart, colorEnd) * (100-percent)) + (Math.max(colorStart, colorEnd) * percent)) / 100);
        else
            return ((Math.min(colorStart, colorEnd) * (100-percent)) + (Math.max(colorStart, colorEnd) * percent)) / 100;
    }

    static float getAngle(Point p1, Point p2, Point p3){

        Log.e(">>>>>>", "p1 : " + p1);
        Log.e(">>>>>>", "p2 : " + p2);
        Log.e(">>>>>>", "p3 : " + p3);

        double numerator = p2.y*(p1.x-p3.x) + p1.y*(p3.x-p2.x) + p3.y*(p2.x-p1.x);
        double denominator = (p2.x-p1.x)*(p1.x-p3.x) + (p2.y-p1.y)*(p1.y-p3.y);
        double ratio = numerator/denominator;

        double angleRad = Math.atan(ratio);
        double angleDeg = -(angleRad*180)/Math.PI; //using a minus to rectification display coordination

        if(angleDeg<0){
            angleDeg = 180+angleDeg;
        }

        return (float) angleDeg;
    }

    static float getAngle(PointF unitVector){
        return (float) Math.toDegrees(Math.atan2(unitVector.y, unitVector.x));
    }

    public static float getDistance(Point a, Point b){
        return (float)Math.sqrt(Math.pow(b.y - a.y,2) + Math.pow(b.x - a.x, 2));
    }
}

class DistResult {
    float distance;
    PointF closetPoint;

    DistResult(PointF closestPoint, float distance) {
        this.distance = distance;
        this.closetPoint = closestPoint;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public PointF getClosetPoint() {
        return closetPoint;
    }

    public void setClosetPoint(PointF closetPoint) {
        this.closetPoint = closetPoint;
    }

    public float diff(DistResult dr) {
        return Math.abs(distance - dr.distance);
    }
}