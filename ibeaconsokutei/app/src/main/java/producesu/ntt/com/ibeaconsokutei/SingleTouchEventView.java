package producesu.ntt.com.ibeaconsokutei;

/**
 * Created by kphongagsorn on 11/12/14.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class SingleTouchEventView extends View {
    private Paint paint = new Paint();
    private Path path = new Path();
    private ArrayList _graphics = new ArrayList();
    float eventX;
    float eventY;

    public SingleTouchEventView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(15f);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawPath(path, paint);
        canvas.drawPoint(eventX, eventY, paint);


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        eventX = event.getX();
        eventY = event.getY();

        /*
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
                path.lineTo(eventX, eventX);
             //   _graphics.add(path);
                break;
            default:
                return false;
        }
        */

        // Schedules a repaint.
        invalidate();
        return true;
    }
}