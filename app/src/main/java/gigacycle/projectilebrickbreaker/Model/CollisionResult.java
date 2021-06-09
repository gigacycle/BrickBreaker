package gigacycle.projectilebrickbreaker.Model;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by Gigacycle on 10/26/2018.
 */

public class CollisionResult {
    public BrickSides collidedSides;
    public PointF collidedPoint;
    public Brick collidedBrick;
    public CollisionResult(){
        collidedSides = BrickSides.none;
        collidedPoint = null;
        collidedBrick = null;
    }
}
