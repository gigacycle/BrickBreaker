package gigacycle.projectilebrickbreaker.Model;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.util.List;
import java.util.TimerTask;

/**
 * Created by Gigacycle on 10/16/2018.
 */

public class BallsDirectionGuide {
    private Point   startPos;
    private Point   endPos;
    private PointF  direction;
    private int     color;
    private boolean visible;
    private boolean simulationIsBusy;
    private boolean isCollided;
    private Ball    simulatingBall;

    public void show(Point secondTouchPos, final Ball pioneerBall, final List<Brick> bricks) {
        if (!visible) visible = true;
        startPos = pioneerBall.getPosition();
        float angle = GraphicHelper.getAngle(startPos, secondTouchPos, new Point(startPos.x + 100, startPos.y));
        angle = (angle > 170) ? 170 : ((angle < 10) ? 10 : angle);
        direction = new PointF((float) Math.cos(Math.toRadians(angle)), (float) Math.sin(Math.toRadians(angle)));
        direction.y = -Math.abs(direction.y);
        int xDest = direction.x > 0 ? GraphicHelper.GameArea.right : GraphicHelper.GameArea.left;
        float n = (xDest - startPos.x) / direction.x;
        int yDest = (int) (startPos.y + (direction.y * n));
        if (yDest < GraphicHelper.GameArea.top) {
            yDest = GraphicHelper.GameArea.top;
            n = (yDest - startPos.y) / direction.y;
            xDest = (int) (startPos.x + (direction.x * n));
        }
        endPos = new Point(xDest, yDest);

        brickCollisionSimulation(bricks, pioneerBall);
    }

    private void brickCollisionSimulation(List<Brick> bricks, Ball pioneerBall) {
        //if (simulationIsBusy) return;
        Log.e(">>>>>>>>", "simu called");
        //simulationIsBusy = true;
        isCollided = false;
        Ball b = new Ball(pioneerBall);
        b.setDirection(direction);
        b.setPosition(startPos);

        while (GraphicHelper.getDistance(endPos, b.getPosition()) > b.getRadius()*3) {
            if (b.getPosition().y < GraphicHelper.GameArea.top || b.getPosition().x < GraphicHelper.GameArea.left || b.getPosition().x > GraphicHelper.GameArea.right)
                break;
//                b.start();
//            b.move(bricks, true);
            PointF dir = new PointF(direction.x * b.getSpeed(), direction.y * b.getSpeed());
            b.setPosition(new Point((int)(b.getPosition().x + dir.x), (int)(b.getPosition().y + dir.y)));

            for (Brick brick : bricks) {
                if (GraphicHelper.getDistance(brick.getCenter(), b.getPosition()) > brick.getSize().getWidth() + b.getRadius())
                    continue;
                CollisionResult cr = brick.isCollided(b, true);
                if (cr.collidedSides != BrickSides.none) {
                    b.setLastBrickCollisionInfo(cr);
                    break;
                }
            }

            if (b.getLastBreakCollisionInfo() != null) {
                isCollided = true;
                break;
            }
        }

        if (simulatingBall == null)
            simulatingBall = new Ball(b);
        else
            simulatingBall.setValue(b);
        //simulationIsBusy = false;
    }

    public void hide() {
        visible = false;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public PointF getDirection() {
        return direction;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (!visible)
            return;
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[]{10, 40}, 0));
        paint.setStrokeWidth(6);
        canvas.drawLine(startPos.x, startPos.y, endPos.x, endPos.y, paint);
        if (isCollided) {
            /*canvas.drawLine(startPos.x, startPos.y, simulatingBall.getPosition().x, simulatingBall.getPosition().y, paint);
            canvas.drawLine(
                    simulatingBall.getPosition().x,
                    simulatingBall.getPosition().y,
                    simulatingBall.getPosition().x + (simulatingBall.getDirection().x * simulatingBall.getSpeed() * 2),
                    simulatingBall.getPosition().y + (simulatingBall.getDirection().y * simulatingBall.getSpeed() * 2), paint);*/
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(simulatingBall.getPosition().x, simulatingBall.getPosition().y, simulatingBall.getRadius(), paint);
        } /*else {
            canvas.drawLine(startPos.x, startPos.y, endPos.x, endPos.y, paint);
        }*/
    }
}
