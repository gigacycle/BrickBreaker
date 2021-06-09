package gigacycle.projectilebrickbreaker.Model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.Random;

import gigacycle.projectilebrickbreaker.Core.GameCore;

/**
 * Created by Gigacycle on 10/14/2018.
 */

public class Brick implements Comparable<Brick>{

    @Override
    public int compareTo(Brick brick) {
        if (position.y > brick.position.y) {
            return 1;
        }
        else if (position.y < brick.position.y) {
            return -1;
        }
        else {
            return 0;
        }
    }

    public interface BrickDestroyListener {
        void onBrickDestroyed();
    }
    public interface BrickIsOutOfGameArea {
        void onGameOver();
    }

    private Point position;
    private Size size;
    private int tenacity;
    private ShapeType shapeType;
    private int color;
    private int seams;
    private BrickDestroyListener brickDestroyListener;
    private BrickIsOutOfGameArea brickIsOutOfGameArea;
    private int destroyAnimateFrameCount;
    private boolean animating;
    private PointF lastCollisionPoint;
    private boolean drawCollidingEffect = false;
    private Rect[] brickParticles;
    private Random rand = new Random();
    private int DISTROY_ANIMATION_FRAME_COUNT = 50;

    public boolean isAnimating() {
        return animating;
    }

    public void setBrickDestroyListener(BrickDestroyListener listener) {
        this.brickDestroyListener = listener;
    }

    public void setBrickGameOverListener(BrickIsOutOfGameArea listener) {
        this.brickIsOutOfGameArea = listener;
    }

    public Brick(Point pos, Size size, int tenacity, ShapeType st, float seamSize) {
        this.color = Color.rgb(255, 0, 0);
        this.position = pos;
        this.seams = (int)seamSize;
        this.size = size;
        this.tenacity = tenacity;
        this.shapeType = st;
        brickDestroyListener = null;
        brickParticles = new Rect[12];
        int c = 0;
        int w = size.getWidth() / 4;
        int h = size.getHeight() / 3;
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 3; j++) {
                brickParticles[c++] = new Rect(position.x + (i * w), position.y + (j * h), position.x + (i * w) + w, position.y + (j * h) + h);
            }
    }

    void hit() {
        tenacity--;
        color = GraphicHelper.getColorOfDegradable(Color.rgb(255, 160, 0), Color.rgb(255, 0, 0), (tenacity * 100) / GameCore.maxTenacity);
        if (tenacity < 1) {
            tenacity = 0;
            destroyAnimateFrameCount = DISTROY_ANIMATION_FRAME_COUNT;
            animating = true;
        }
    }

    public void flowDown() {
        position.set(position.x, position.y + size.getHeight() + seams);
        for (int i = 0; i < brickParticles.length; i++) {
            brickParticles[i].top += size.getHeight() + seams;
            brickParticles[i].bottom += size.getHeight() + seams;
        }
        if (position.y + size.getHeight() > GraphicHelper.GameArea.bottom)
            brickIsOutOfGameArea.onGameOver();
    }

    public void draw(Canvas canvas, Paint paint){
        if (destroyAnimateFrameCount == 0) {
            paint.setColor(Color.LTGRAY);
            canvas.drawRect(position.x + seams, position.y + seams, position.x + size.getWidth() + seams, position.y + size.getHeight() + (seams * 2), paint);
            paint.setColor(color);
            canvas.drawRect(position.x, position.y, position.x + size.getWidth(), position.y + size.getHeight(), paint);
            paint.setColor(Color.WHITE);
            String strTenacity = String.valueOf(tenacity);
            Rect textBound = new Rect();
            paint.getTextBounds(strTenacity, 0, strTenacity.length(), textBound);
            canvas.drawText(String.valueOf(tenacity), position.x + size.getWidth() / 2.0f - textBound.centerX(), position.y + size.getHeight() / 2.0f - textBound.centerY(), paint);
            if (drawCollidingEffect){
                int c = Color.argb(80, 0, 0, 0);
                paint.setColor(c);
                canvas.drawRect(position.x, position.y, position.x + size.getWidth(), position.y + size.getHeight(), paint);
                c = Color.argb(120, 255, 255, 255);
                paint.setColor(c);
                canvas.drawCircle(lastCollisionPoint.x, lastCollisionPoint.y, size.getHeight()/3.0f, paint);
                drawCollidingEffect = false;
            }
        }
        else {
            drawBrickParticles(canvas, paint);
        }
    }

    private void drawBrickParticles(Canvas canvas, Paint paint) {
        color = Color.argb((int)(255 * (destroyAnimateFrameCount / (float)DISTROY_ANIMATION_FRAME_COUNT)), Color.red(color), Color.green(color), Color.blue(color));
        int shadowColor = Color.argb((int)(255 * (destroyAnimateFrameCount / (float)DISTROY_ANIMATION_FRAME_COUNT)), Color.red(Color.LTGRAY), Color.green(Color.LTGRAY), Color.blue(Color.LTGRAY));
        for (int i = 0; i < brickParticles.length; i++) {
            paint.setColor(shadowColor);
            canvas.drawRect(brickParticles[i].left + seams, brickParticles[i].top + seams, brickParticles[i].right + seams, brickParticles[i].bottom + (seams * 2), paint);
            paint.setColor(color);
            canvas.drawRect(brickParticles[i], paint);
            int x, y;
            if (destroyAnimateFrameCount > DISTROY_ANIMATION_FRAME_COUNT - 15) {
                y = rand.nextInt(40) + 1;
                x = rand.nextInt(5) + 1;
            }
            else
            {
                x = 0; y = 20;
            }
            brickParticles[i].top += y;
            brickParticles[i].bottom += y;
            if (i < brickParticles.length / 2) {
                brickParticles[i].left -= x;
                brickParticles[i].right -= x;
            }
            else{
                brickParticles[i].left += x;
                brickParticles[i].right += x;
            }

        }
        destroyAnimateFrameCount--;
        if (destroyAnimateFrameCount < 1) {
            animating = false;
            brickDestroyListener.onBrickDestroyed();
        }
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public int getTenacity() {
        return this.tenacity;
    }

    public Point getCenter(){
        return new Point(position.x + (size.getWidth() / 2), position.y + (size.getHeight() / 2));
    }

    public Size getSize() {
        return size;
    }

    public PointF getLastCollisionPoint() {
        return lastCollisionPoint;
    }

    CollisionResult isCollided(Ball ball, boolean simulating) {
        if (destroyAnimateFrameCount > 0)
            return new CollisionResult();

        if ((ball.getPosition().y + ball.getRadius() >= position.y) && (ball.getPosition().y - ball.getRadius() <= position.y + size.getHeight()) &&
                (ball.getPosition().x + ball.getRadius() >= position.x) && (ball.getPosition().x - ball.getRadius() <= position.x + size.getWidth())) {
            BrickSides rv = BrickSides.top;

            DistResult distT = GraphicHelper.DistOfPointFromSegmentLine(ball.getPosition(), position, new Point(position.x + size.getWidth(), position.y));
            DistResult distL = GraphicHelper.DistOfPointFromSegmentLine(ball.getPosition(), position, new Point(position.x, position.y + size.getHeight()));
            DistResult distR = GraphicHelper.DistOfPointFromSegmentLine(ball.getPosition(), new Point(position.x + size.getWidth(), position.y), new Point(position.x + size.getWidth(), position.y + size.getHeight()));
            DistResult distB = GraphicHelper.DistOfPointFromSegmentLine(ball.getPosition(), new Point(position.x, position.y + size.getHeight()), new Point(position.x + size.getWidth(), position.y + size.getHeight()));

            DistResult dist = distT;

            if (distL.distance < dist.distance) {
                dist = distL;
                rv = BrickSides.left;
            }

            if (distR.distance < dist.distance) {
                dist = distR;
                rv = BrickSides.right;
            }

            if (distB.distance < dist.distance) {
                dist = distB;
                rv = BrickSides.bottom;
            }

            lastCollisionPoint = new PointF(dist.closetPoint.x, dist.closetPoint.y);
            if (!simulating)
                drawCollidingEffect = true;
            else
                drawCollidingEffect = false;

/*            if ((lastCollisionPoint.x >= position.x) && (lastCollisionPoint.x < position.x + seams) &&
                    (lastCollisionPoint.y >= position.y) && (lastCollisionPoint.y < position.y + seams))
                rv = BrickSides.topLeftCorner;
            else if ((lastCollisionPoint.x <= position.x + size.getWidth()) && (lastCollisionPoint.x > position.x + size.getWidth() - seams) &&
                    (lastCollisionPoint.y >= position.y) && (lastCollisionPoint.y < position.y + seams))
                rv = BrickSides.topRightCorner;
            else if ((lastCollisionPoint.x >= position.x) && (lastCollisionPoint.x < position.x + seams) &&
                    (lastCollisionPoint.y <= position.y + size.getHeight()) && (lastCollisionPoint.y > position.y + size.getHeight() - seams))
                rv = BrickSides.bottomLeftCorner;
            else if ((lastCollisionPoint.x <= position.x + size.getWidth()) && (lastCollisionPoint.x > position.x + size.getWidth() - seams) &&
                    (lastCollisionPoint.y <= position.y + size.getHeight()) && (lastCollisionPoint.y > position.y + size.getHeight() - seams))
                rv = BrickSides.bottomRightCorner;*/

            if ((lastCollisionPoint.x == position.x) && (lastCollisionPoint.y == position.y))
                rv = BrickSides.topLeftCorner;
            else if ((lastCollisionPoint.x == position.x + size.getWidth()) &&  (lastCollisionPoint.y == position.y))
                rv = BrickSides.topRightCorner;
            else if ((lastCollisionPoint.x == position.x) && (lastCollisionPoint.y == position.y + size.getHeight()))
                rv = BrickSides.bottomLeftCorner;
            else if ((lastCollisionPoint.x == position.x + size.getWidth()) && (lastCollisionPoint.y == position.y + size.getHeight()))
                rv = BrickSides.bottomRightCorner;

            CollisionResult result = new CollisionResult();
            result.collidedSides = rv;
            result.collidedPoint = new PointF(lastCollisionPoint.x, lastCollisionPoint.y);
            result.collidedBrick = this;
            return result;
        }
        return new CollisionResult();
    }

}
