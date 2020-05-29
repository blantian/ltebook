package com.lantian.base.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;


public class MongolTextView extends AppCompatTextView {

    private TextPaint textPaint;
    private Context context;

    public MongolTextView(Context context, AttributeSet attributeSet, int defstyle){
        super(context,attributeSet,defstyle);
        this.context=context;
        init();
    }
    public MongolTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MongolTextView(Context context) {
        super(context);
        init();
    }
    public void init()
    {
        //MongolFontMirrored
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/mongolianbaiti.ttf");
        setTypeface(tf);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(),getMeasuredWidth());

    }
    @Override
    protected  void onDraw(Canvas canvas)
    {
        textPaint = getPaint();
        textPaint.setColor(getCurrentTextColor());
        textPaint.drawableState = getDrawableState();

        canvas.save();

        // flip and rotate the canvas
        canvas.translate(getWidth(), 0);
        canvas.rotate(90);
        //canvas.translate(0, getWidth());
        //canvas.scale(1, -1);
        //canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());

        //20190427 设置文字居中注释
        //getLayout().draw(canvas);
        //设置水平居中
        textPaint.setTextAlign(Paint.Align.CENTER);

        float fontWeight=textPaint.measureText(this.getText().toString());
        //设置垂直居中
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
// 计算文字高度 
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
// 计算文字baseline 
        float textBaseY = getMeasuredHeight() - (getMeasuredHeight() - fontHeight) / 2 - fontMetrics.bottom;
        //textBaseY=(getMeasuredHeight() - (textPaint.descent() - textPaint.ascent())) / 2 - textPaint.ascent();
//        float textBaseY = bounds.height() - (bounds.height() - fontHeight) / 2 - fontMetrics.bottom;

        //当前文字

        //canvas.drawText(this.getText().toString(), getMeasuredWidth() / 2, textBaseY- fontHeight / 2, textPaint);
        canvas.drawText(this.getText().toString(), getMeasuredHeight()/ 2, (getMeasuredWidth()) / 2, textPaint);

        //20190427 设置文字居中注释
        //canvas.restore();
    }
}
