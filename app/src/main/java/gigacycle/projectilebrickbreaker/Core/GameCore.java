package gigacycle.projectilebrickbreaker.Core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import gigacycle.projectilebrickbreaker.Model.Ball;
import gigacycle.projectilebrickbreaker.Model.BallsDirectionGuide;
import gigacycle.projectilebrickbreaker.Model.Brick;
import gigacycle.projectilebrickbreaker.Model.GraphicHelper;
import gigacycle.projectilebrickbreaker.Model.ShapeType;
import gigacycle.projectilebrickbreaker.Model.Size;

/**
 * Created by Gigacycle on 10/14/2018.
 * Core of the graphics
 */

public class GameCore extends View {

    //region Static members
    public static boolean gameIsOver = false;
    public static boolean isStopped = false;
    public static boolean allBallsAreStarted = false;
    public static boolean startingBallMovements = false;
    public static boolean preventAddingExtraRows = false;
    public static int maxTenacity = 1;
    //endregion

    Paint paint = new Paint();
    float scale = 1;
    private List<Brick> Bricks = new ArrayList<>();
    private List<Ball> Balls = new ArrayList<>();
    private BallsDirectionGuide bdg;
    private Point startTouchPoint, endTouchPoint;
    private Date[] startTimes = new Date[2];
    private TimerTask gameLoop;
    Ball pioneerBall;
    Rect textBound;

    public GameCore(Context context) {
        super(context);
        scale = context.getResources().getDisplayMetrics().scaledDensity;
        int w = context.getResources().getDisplayMetrics().widthPixels;
        int h = context.getResources().getDisplayMetrics().heightPixels;
        GraphicHelper.GameArea = new Rect(0, (int)(h * 0.21f), w, h - (int)(h * 0.21f));
        GraphicHelper.ScreenWidth = w;
        GraphicHelper.ScreenHeight = h;
        GraphicHelper.Scale = scale;

        bdg = new BallsDirectionGuide();
        bdg.setColor(Color.argb(255, 26, 202, 85));

        maxTenacity = 0;

        //addNewBall();

        addNewBricksRow();

        invalidate();

        setOnTouchListener(createTouchListener());

    }

    private Brick.BrickDestroyListener createDestroyListener(final Brick brick) {
        return new Brick.BrickDestroyListener() {

            @Override
            public void onBrickDestroyed() {
                Bricks.remove(brick);
            }
        };
    }

    private Brick.BrickIsOutOfGameArea createGameOverListener() {
        return new Brick.BrickIsOutOfGameArea() {
            @Override
            public void onGameOver() {
                gameIsOver = true;
            }
        };
    }

    public Ball.BallIsOnTheGroundEventListener onBallIsOnTheGround(){
        return new Ball.BallIsOnTheGroundEventListener() {
            @Override
            public void onBallIsOnTheGround(Ball sender) {
                if (pioneerBall == null) {
                    pioneerBall = sender;
                    pioneerBall.stop();
                    sender.setPioneerBall(null);
                    for(Ball ball: Balls) {
                        if (ball == sender)
                            continue;
                        ball.setPioneerBall(pioneerBall);
                    }
                }
            }
        };
    }

    private void flowDownBricks() {
        for (Brick brick : Bricks)
            brick.flowDown();
    }

    private void updateBricksColor() {
        Collections.sort(Bricks);
        for (Brick brick : Bricks)
            brick.setColor(GraphicHelper.getColorOfDegradable(Color.rgb(255, 160, 0), Color.rgb(255, 0, 0), (brick.getTenacity() * 100) / maxTenacity));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.reset();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);
        paint.setTextSize(20 * scale);
        paint.setFakeBoldText(true);
        paint.setAntiAlias(true);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10.0f);
        canvas.drawRect(GraphicHelper.GameArea, paint);

        paint.setStyle(Paint.Style.FILL);

        try {
            for (Brick brick : Bricks)
                brick.draw(canvas, paint);
        }catch(Exception ignored){}

        for (Ball ball: Balls)
            ball.draw(canvas, paint);

        bdg.draw(canvas, paint);

        writeTimestamp(canvas, paint);

        if (gameIsOver)
        {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(200, 255, 255, 255));
            canvas.drawRect(GraphicHelper.GameArea.left, GraphicHelper.GameArea.top - 100, GraphicHelper.GameArea.right, GraphicHelper.GameArea.bottom + 100, paint);
            paint.setTextSize(40 * scale);
            paint.setFakeBoldText(true);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.getTextBounds("Game Over", 0, "Game Over".length(), textBound);
            canvas.drawText("Game Over", GraphicHelper.GameArea.width() / 2.0f - textBound.centerX(), GraphicHelper.GameArea.top + (GraphicHelper.GameArea.height()/2.0f) - textBound.centerY(),paint);
        }

    }

    private void writeTimestamp(Canvas canvas, Paint paint){
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(14 * scale);
        paint.setFakeBoldText(true);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        Rect textBound = new Rect();
        if (startTimes[0] !=  null) {
            String s = "1 : " + msToString(startTimes[0].getTime());
            paint.getTextBounds(s, 0, s.length(), textBound);
            canvas.drawText(s, GraphicHelper.ScreenWidth / 2.0f - textBound.centerX(), 50 - textBound.centerY(), paint);
        }
        if (startTimes[1] != null) {
            String s = "2 : " + msToString(startTimes[1].getTime());
            paint.getTextBounds(s, 0, s.length(), textBound);
            canvas.drawText(s, GraphicHelper.ScreenWidth / 2.0f - textBound.centerX(), 100 - textBound.centerY(), paint);
        }

    }

    public String msToString(long ms) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ms);
        return cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND)+":"+cal.get(Calendar.MILLISECOND);
    }

    private void startBalls(PointF direction) {
        int delay = 0;
        final Ball mainBall;
        if (pioneerBall == null)
            mainBall = Balls.get(0);
        else
        {
            mainBall = new Ball(pioneerBall);
            pioneerBall = null;
        }
        final int[] cnt = {0};
        for (final Ball ball : Balls) {
            ball.setPosition (new Point(mainBall.getPosition().x, (int) (GraphicHelper.GameArea.bottom - ball.getRadius() - (2 * scale))));
            ball.setDirection (new PointF(direction.x, direction.y));
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ball.start();
                    cnt[0] +=1;
                    if (cnt[0] >= Balls.size()) {
                        allBallsAreStarted = true;
                        startingBallMovements = false;
                    }
                }
            }, delay);
            delay += 36;
        }
    }

    private boolean areAllBallsOnTheGround(boolean reset) {
        boolean rv = true;
        for (Ball ball:Balls) {
            if (ball.isMoving())
            {
                preventAddingExtraRows = false;
                rv = false;
                break;
            }
        }
        if (rv && reset) {
            allBallsAreStarted = false;
            startingBallMovements = false;
        }
        return rv;
    }

    private boolean checkBricksAnimation() {
        boolean rv = false;
        for (Brick brick: Bricks)
            if (brick.isAnimating()) {
                rv = true;
                break;
            }
        return rv;
    }

    public void addNewBricksRow() {
        updateRowGenTime(Calendar.getInstance().getTime());
        flowDownBricks();
        int w = GraphicHelper.ScreenWidth - (int) (10 + ((2 * scale) * 6));
        maxTenacity++;
        List<Brick> row = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Random rand = new Random();
            int n = rand.nextInt(50) + 1;
            if (!(i >= 5 && row.size() < 1)) {
                if (n < 25)
                    continue;
            }
            Size sz = new Size(w / 6, (GraphicHelper.GameArea.height() / 9));
            int seams = (int) (2 * scale);
            Point pnt = new Point(7 + i * (sz.getWidth() + (2 * (int) scale)), GraphicHelper.GameArea.top + (2 * (int) scale) + sz.getHeight() + seams);
            final Brick brick = new Brick(pnt, sz, maxTenacity, ShapeType.Rect, seams);
            row.add(brick);
            brick.setBrickDestroyListener(createDestroyListener(brick));
            brick.setBrickGameOverListener(createGameOverListener());
        }
        Bricks.addAll(row);
        updateBricksColor();
        addNewBall();
    }

    public void addNewBall() {
        Ball ball;
        if (Balls.size() == 0) {
            ball = new Ball();
            ball.setRadius(7 * scale);
            ball.setColor(Color.rgb(91, 167, 244));
            ball.setSpeed(7 * scale);
            ball.setDirection(new PointF(0.5f, -0.5f));
            ball.setPosition(new Point(GraphicHelper.ScreenWidth / 2, (int) (GraphicHelper.GameArea.bottom - ball.getRadius() - 5)));
        } else {
            if (pioneerBall == null)
                ball = new Ball(Balls.get(0));
            else
                ball = new Ball(pioneerBall);
            ball.setBallIsOnTheGroundEventListener(onBallIsOnTheGround());
        }
        Balls.add(ball);
        Log.e(">>>>", "maxTenacity :" + maxTenacity + " | ball count : " + Balls.size());
    }

    public void moveBalls() {
        for (Ball ball : Balls) {
            ball.move(Bricks, false);
        }
    }

    public void startGameLoop() {

        isStopped = false;
        final Handler handler = new Handler();

        final Timer gameLoopTimer = new Timer();
        gameLoop = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if (!isStopped) {
                                moveBalls();
                                if (areAllBallsOnTheGround(true) && !checkBricksAnimation() && !preventAddingExtraRows) {
                                    addNewBricksRow();
                                    preventAddingExtraRows = true;
                                }
                            }
                            invalidate();
                        } catch (Exception ignored) {}

                        if (isStopped) {
                            gameLoopTimer.cancel();
                            gameLoopTimer.purge();
                        }
                    }
                });
            }
        };
        gameLoopTimer.schedule(gameLoop, 0, 12); //execute in every 15 ms
    }

    private void updateRowGenTime(Date currentTime) {
        startTimes[0] = startTimes[1];
        startTimes[1] = currentTime;
    }

    private OnTouchListener createTouchListener(){
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (allBallsAreStarted || gameIsOver || startingBallMovements)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTouchPoint = new Point((int) event.getX(), (int) event.getY());
                        break;

                    case MotionEvent.ACTION_MOVE:
                        endTouchPoint = new Point((int) event.getX(), (int) event.getY());
                        if (areAllBallsOnTheGround(false) && GraphicHelper.getDistance(endTouchPoint, startTouchPoint) > (10 * scale)) {
                            if ((new Rect(0, 0, GraphicHelper.ScreenWidth, GraphicHelper.GameArea.bottom)).contains((int) event.getX(), (int) event.getY())) {
                                bdg.show(endTouchPoint, pioneerBall == null ? Balls.get(0) : pioneerBall, Bricks);
                                invalidate();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (areAllBallsOnTheGround(true) && GraphicHelper.getDistance(endTouchPoint, startTouchPoint) > (10 * scale)) {
                            startingBallMovements = true;
                            bdg.hide();
                            startBalls(bdg.getDirection());
                            if (gameLoop == null)
                                startGameLoop();
                        }
                        break;
                }
                return true;
            }
        };
    }
}
