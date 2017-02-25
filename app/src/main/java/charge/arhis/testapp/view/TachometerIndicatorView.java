package charge.arhis.testapp.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import charge.arhis.testapp.R;

/**
 * Created by arhis on 24.02.2017.
 */

public class TachometerIndicatorView extends View {

    private int[] paddings = new int[4];
    private Rect rect = new Rect();
    private RectF rectF = new RectF();
    private int gaugeBackgroundColor = 0xff303030;
    private int pointerColor = 0xffff3030;
    private int pinColor = 0xff808080;
    private int textColor = 0xff50ff50;

    private int gaugeMaxValue = 10;

    private float rpm = 0;

    private Paint paint = new Paint();

    private ObjectAnimator animator;

    float[] pointerPath = new float[] {
            0, -0.9f,
            -0.02f, -0.3f,

            -0.02f, -0.3f,
            -0.03f, 0.18f,

            -0.03f, 0.18f,
            0.00f, 0.173f,

            0.00f, 0.173f,
            0.03f, 0.18f,

            0.03f, 0.18f,
            0.02f, -0.3f,

            0.02f, -0.3f,
            0, -0.9f
    };

    public TachometerIndicatorView(Context context) {
        super(context);
        init(null);
    }

    public TachometerIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TachometerIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            int[] attributes = new int[] {android.R.attr.paddingLeft, android.R.attr.paddingTop, android.R.attr.paddingRight, android.R.attr.paddingBottom};
            TypedArray arr = getContext().obtainStyledAttributes(attrs, attributes);
            try {
                for (int i = 0; i < 4; i++) {
                    paddings[i] = arr.getDimensionPixelOffset(i, 0);
                }
            } finally {
                arr.recycle();
            }

            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.TachometerIndicatorView,
                    0, 0);

            try {
                pointerColor = a.getColor(R.styleable.TachometerIndicatorView_indicatorColor, 0xffff3030);
            } finally {
                a.recycle();
            }
        }

        paint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        //calculate diagram size on canvas
        int size = Math.min(getWidth() - paddings[0] - paddings[2], getHeight() - paddings[1] - paddings[3]);

        int totalXPadding = paddings[0] + (getWidth() - paddings[0] - paddings[2] - size) / 2;
        int totalYPadding = paddings[1] + (getHeight() - paddings[1] - paddings[3] - size) / 2;
        rect.set(totalXPadding, totalYPadding, totalXPadding + size, totalYPadding + size);
        rectF.set(rect);

        int restoreTo = canvas.save();

        canvas.clipRect(rectF);

        //draw background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(gaugeBackgroundColor);
        canvas.drawOval(rectF, paint);

        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);

        //draw text labels
        paint.setColor(textColor);
        paint.setTextSize(50f);
        int textXShift = (int)paint.measureText("8")/2;
        int textYShift = (int)paint.getFontMetrics().ascent/2;
        for (int i = 0; i <= gaugeMaxValue; i++) {
            float angle = -(270 / gaugeMaxValue * i);
            int labelX = -textXShift + (int)(0.65 * (size / 2 * Math.sin((double) 2 * Math.PI * (angle/360))));
            int labelY = -textYShift + (int)(0.65 * (size / 2 * Math.cos((double) 2 * Math.PI * (angle/360))));
            canvas.drawText(Integer.toString(i), labelX, labelY, paint);
        }

        float angle = 45;
        paint.setTextSize(30f);
        int labelX = -textXShift + (int)(0.4 * (size / 2 * Math.sin((double) 2 * Math.PI * (angle/360))));
        int labelY = -textYShift + (int)(0.4 * (size / 2 * Math.cos((double) 2 * Math.PI * (angle/360))));
        canvas.drawText("R.P.M", labelX, labelY, paint);
        labelY += paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
        canvas.drawText("x1000", labelX, labelY, paint);

        canvas.scale(size / 2, size / 2);
        canvas.rotate(180);

        paint.setColor(pointerColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0.02f);
        //draw pointer arrow
        canvas.save();
        canvas.rotate((rpm/gaugeMaxValue) * 270);
        canvas.drawLines(pointerPath, paint);
        canvas.restore();

        //draw arrow pivot
        paint.setColor(pinColor);
        paint.setStyle(Paint.Style.FILL);
        rectF.set(.05f, .05f, -.05f, -.05f);
        canvas.drawCircle(0, 0, 0.06f, paint);

        //draw gauge marks
        paint.setColor(pointerColor);
        paint.setStyle(Paint.Style.STROKE);
        canvas.save();
        float gaugeRotationStep = (float)270 / (gaugeMaxValue * 5);
        for (int i = 0; i <= gaugeMaxValue * 5; i++) {
            if (i % 5 == 0) {
                paint.setStrokeWidth(0.02f);
                canvas.drawLine(0, -1, 0, -0.8f, paint);
            } else {
                paint.setStrokeWidth(0.015f);
                canvas.drawLine(0, -0.9f, 0, -0.8f, paint);
            }
            canvas.rotate(gaugeRotationStep);
        }
        canvas.restore();

        canvas.restore();

        canvas.restoreToCount(restoreTo);
    }

    public void setAnimated(final float rpm) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        animator = ObjectAnimator.ofFloat(this, "rpm", this.rpm, rpm);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    public void setRpm(float rpm) {
        this.rpm = rpm;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int)event.getX();
            int y = (int)event.getY();
            double angle = -Math.atan2(x - getWidth() / 2, y - getHeight() / 2);
            if ((y - getHeight() / 2) < 0 && (x - getWidth() / 2) > 0) {
                angle += 2*Math.PI;
            }
            float newRmp = (float) (angle / (2 * Math.PI  - Math.PI / 2)) * gaugeMaxValue;
            if (newRmp >= 0 && newRmp <= gaugeMaxValue) {
                setAnimated(newRmp);
            }
        }
        return true;
    }

    public Parcelable onSaveInstanceState() {
        if (animator != null && animator.isRunning()) {
            animator.end();
        }
        Parcelable superState = super.onSaveInstanceState();
        return new State(superState).setRpm(rpm);
    }

    public void onRestoreInstanceState(Parcelable savedState) {
        State state = (State) savedState;
        super.onRestoreInstanceState(state.getSuperState());
        setRpm(state.rpm);
    }

    private static class State extends BaseSavedState {

        float rpm;

        public State(Parcel source) {
            super(source);
            rpm = source.readFloat();
        }

        public State(Parcelable superState) {
            super(superState);
        }

        public State setRpm(float rpm) {
            this.rpm = rpm;
            return this;
        }

        public float getRpm() {
            return rpm;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(rpm);
        }

        public static final Parcelable.Creator<State> CREATOR = new Parcelable.Creator<State>() {
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            public State[] newArray(int size) {
                return new State[size];
            }
        };
    }
}
