package gigacycle.projectilebrickbreaker.Model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 *. Created by Gigacycle on 10/14/2018.
 */

public class Ball {
    private float speed;
    private float radius;
    private int color;
    private Point position;
    private PointF direction;
    private PointF afterCollisionDirection;
    private boolean isMoving;
    private List<Point> lastTenMoves;
    private CollisionResult lastBrickCollisionInfo;
    private BallIsOnTheGroundEventListener ballIsOnTheGroundEventListener;
    private Ball pioneerBall;
    private boolean moveToPioneerPosition;

    public interface BallIsOnTheGroundEventListener {
        void onBallIsOnTheGround(Ball sender);
    }

    public void setBallIsOnTheGroundEventListener(BallIsOnTheGroundEventListener ballIsOnTheGroundEventListener) {
        this.ballIsOnTheGroundEventListener = ballIsOnTheGroundEventListener;
    }

    public Ball(){
        isMoving = false;
        lastTenMoves = new ArrayList<>();
        afterCollisionDirection = new PointF(0,0);
        lastBrickCollisionInfo = null;
        position = new Point();
        direction = new PointF();
        moveToPioneerPosition = false;
    }

    public Ball(Ball ball) {
        this();
        setValue(ball);
    }

    public void setValue(Ball ball){
        this.speed = ball.getSpeed();
        this.radius = ball.getRadius();
        this.color = ball.getColor();
        this.position.set(ball.getPosition().x, ball.getPosition().y);
        this.direction.set(ball.getDirection().x, ball.getDirection().y);
    }

    public Ball getPioneerBall() {
        return pioneerBall;
    }

    public void setPioneerBall(Ball pioneerBall) {
        this.pioneerBall = pioneerBall;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    float getSpeed() {
        return speed;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setPosition(Point pos) {
        this.position.set(pos.x, pos.y);
    }

    public Point getPosition() {
        return position;
    }

    public void setDirection(PointF direction) {
        this.direction.set(direction.x, direction.y);
    }

    PointF getDirection() {
        return direction;
    }

    public PointF getAfterCollisionDirection() {
        return afterCollisionDirection;
    }

    CollisionResult getLastBreakCollisionInfo() {
        return lastBrickCollisionInfo;
    }

    public void setLastBrickCollisionInfo(CollisionResult lastBrickCollisionInfo) {
        this.lastBrickCollisionInfo = lastBrickCollisionInfo;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (isMoving())
            drawBallTrail(canvas, paint);
        paint.setColor(color);
        canvas.drawCircle(position.x, position.y, radius, paint);
        paint.setColor(Color.RED);
        //canvas.drawLine(position.x, position.y, position.x + afterCollisionDirection.x * radius, position.y + afterCollisionDirection.y * radius, paint);
    }

    private void drawBallTrail(Canvas canvas, Paint paint) {
        for (int i = lastTenMoves.size() - 1; i >= 0; i--) {
            int clr = Color.argb((int) (80 * (i / (float) lastTenMoves.size())), Color.red(color), Color.green(color), Color.blue(color));
            paint.setColor(clr);
            canvas.drawCircle(lastTenMoves.get(i).x, lastTenMoves.get(i).y, radius * (i / (float) lastTenMoves.size()), paint);
        }
    }

    public void start()
    {
        isMoving = true;
    }

    public void stop() {
        isMoving = false;
        moveToPioneerPosition = false;
        lastTenMoves.clear();
    }

    public void move(List<Brick> bricks, boolean simulating) {
        if (!isMoving)
            return;

        if (!moveToPioneerPosition) {
            PointF dir = new PointF(direction.x * speed, direction.y * speed);
            int x = (int) (position.x + dir.x);
            int y = (int) (position.y + dir.y);
            position.set(x, y);
            checkCollision(bricks, simulating);
        }
        else
        {
            if (pioneerBall == null || pioneerBall.isMoving()) {
                stop();
                return;
            }

            if (pioneerBall.getPosition().x < position.x)
                position.set((int) (position.x - speed), position.y);
            else
                position.set((int) (position.x + speed), position.y);

            if (GraphicHelper.getDistance(position, pioneerBall.getPosition()) < speed)
            {
                position.set(pioneerBall.getPosition().x, getPosition().y);
                stop();
            }
        }
        if (lastTenMoves.size() >= 15)
            lastTenMoves.remove(0);
        lastTenMoves.add(new Point(position));
    }

    private void checkCollision(List<Brick> bricks, boolean simulating) {
        for (Brick brick : bricks) {
            CollisionResult collisionResult = brick.isCollided(this, simulating);
            if (collisionResult.collidedSides != BrickSides.none) {
                switch (collisionResult.collidedSides) {
                    case top:
                        direction.y = -Math.abs(direction.y);
                        break;
                    case bottom:
                        direction.y =  Math.abs(direction.y);
                        break;
                    case left:
                        direction.x = -Math.abs(direction.x);
                        break;
                    case right:
                        direction.x =  Math.abs(direction.x);
                        break;
                    case topLeftCorner:
                        PointF newDir = newDirOnCornerAngle(direction);
                        afterCollisionDirection = newDir;
                        direction.x = -Math.abs(newDir.x);
                        direction.y = -Math.abs(newDir.y);
                        break;
                    case topRightCorner:
                        newDir = newDirOnCornerAngle(direction);
                        afterCollisionDirection = newDir;
                        direction.x =  Math.abs(newDir.x);
                        direction.y = -Math.abs(newDir.y);
                        break;
                    case bottomLeftCorner:
                        newDir = newDirOnCornerAngle(direction);
                        afterCollisionDirection = newDir;
                        direction.x = -Math.abs(newDir.x);
                        direction.y =  Math.abs(newDir.y);
                        break;
                    case bottomRightCorner:
                        newDir = newDirOnCornerAngle(direction);
                        afterCollisionDirection = newDir;
                        direction.x = Math.abs(newDir.x);
                        direction.y = Math.abs(newDir.y);
                        break;
                }
                if (!simulating)
                    brick.hit();
                lastBrickCollisionInfo = collisionResult;
            }
            else
                lastBrickCollisionInfo = null;
        }
        /* Checking wall collisions */
        if (position.x - radius < GraphicHelper.GameArea.left)
            direction.x = Math.abs(direction.x);
        else if (position.x + radius > GraphicHelper.GameArea.right)
            direction.x = -Math.abs(direction.x);

        if (position.y - radius < GraphicHelper.GameArea.top)
            direction.y = Math.abs(direction.y);
        else if (position.y + radius > GraphicHelper.GameArea.bottom - (2 * GraphicHelper.Scale)) {
            position.set(position.x, (int) (GraphicHelper.GameArea.bottom - (2 * GraphicHelper.Scale) - radius));
            direction.y = -Math.abs(direction.y);
            moveToPioneerPosition = true;
            ballIsOnTheGroundEventListener.onBallIsOnTheGround(this);
        }
    }

    private PointF newDirOnCornerAngle(PointF unitVector) {
        float angle = GraphicHelper.getAngle(unitVector);
        float newAngle = 90.0f - Math.abs(angle);
        return new PointF((float) Math.cos(Math.toRadians(newAngle)), (float) Math.sin(Math.toRadians(newAngle)));
    }
}
