package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collections;
import java.util.List;

public class BusRouteMapView extends View {
    private static final int VERTICAL_RECTANGLE_WIDTH = 20;
    private static final int VERTICAL_RECTANGLE_HEIGHT = 100;
    private static final int TEXT_SIZE = 45;

    // Constants for rectangle dimensions
    private static final int RECTANGLE_HEIGHT = 20; // Height of the rectangles

    private Paint rectPaint;
    private Paint circlePaint;
    private Paint arrowPaint;

    private int numStations;
    private String Current_site;
    private int Current_i;
    private List<String> stationNames;

    public BusRouteMapView(Context context) {
        super(context);
        init();
    }

    public BusRouteMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BusRouteMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Initialize any required configurations or variables
        circlePaint = new Paint();
        rectPaint = new Paint();
        arrowPaint = new Paint();
    }

    public void setBusRouteData(int numStations, List<String> stationNames, String Current_site) {
        if(!stationNames.get(0).contains("起点")
                && !stationNames.get(0).contains("终点")){
            stationNames.set(0,"起点·"+stationNames.get(0));
        }
        if(stationNames.size() != 1){
            if(!stationNames.get(stationNames.size()-1).contains("终点")
            && !stationNames.get(stationNames.size()-1).contains("起点")){
                stationNames.set(stationNames.size()-1,"终点·"+stationNames.get(stationNames.size()-1));
            }
        }
        this.numStations = numStations;
        this.stationNames = stationNames;
        this.Current_site = Current_site;
        for(int i=0 ; i<stationNames.size() ; i++){
            if(stationNames.get(i).equals(Current_site)){
                stationNames.set(i,"当前·"+stationNames.get(i));
                Current_i = i;
            }
        }
        Collections.reverse(stationNames);
        invalidate();
    }

    float slideOffset;

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // Calculate slide offset based on touch position
                float touchX = event.getX();
                slideOffset = (getWidth() / 2 - touchX) / (getWidth() / 2);

                // Update slideOffset in BusRouteMapView or a variable within the view

                // Trigger redraw of the view
                invalidate();
                break;
        }
        return true;
    }

    private static final int CIRCLE_RADIUS = 30; // Set the desired circle radius
    private static final int RECTANGLE_WIDTH = 80; // Set the desired rectangle width
    private static final int HORIZONTAL_SPACING = 100; // Set the desired horizontal spacing

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Calculate the available width for drawing each station's components
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();

        // Calculate the total width required for drawing all the stations
        int totalWidth = (RECTANGLE_WIDTH + HORIZONTAL_SPACING) * numStations;

        // Calculate the amount of offset for sliding the view sideways
        int offsetX = (int) (totalWidth * slideOffset);

        // Draw the circles, rectangles, and station names
        for (int i = numStations-1; i >=0 ; i--) {
            int centerX = getPaddingLeft() + (RECTANGLE_WIDTH + HORIZONTAL_SPACING) * (Current_i-i) - offsetX;
            centerX = centerX*2+725;
            int centerY = getHeight() / 2;

            // Draw the circle
            drawCircle(canvas, centerX, centerY, i);

            // Calculate the position for drawing the rectangle below the circle
            int rectLeft = centerX - RECTANGLE_WIDTH / 2;
            int rectTop = centerY + CIRCLE_RADIUS;
            int rectRight = centerX + RECTANGLE_WIDTH;
            int rectBottom = rectTop + RECTANGLE_HEIGHT;

            if(i!=numStations-1){
                // Draw the rectangle
                drawRectangle(canvas, rectLeft-275, rectTop-40, rectRight-125, rectBottom-40,i);
            }

            // Calculate the position for drawing the vertical rectangle
            int verticalRectLeft = centerX - VERTICAL_RECTANGLE_WIDTH / 2;
            int verticalRectTop = rectBottom;
            int verticalRectRight = centerX + VERTICAL_RECTANGLE_WIDTH / 2;
            int verticalRectBottom = verticalRectTop + VERTICAL_RECTANGLE_HEIGHT;

            // Draw the vertical rectangle
            drawVerticalRectangle(canvas, verticalRectLeft, verticalRectTop, verticalRectRight, verticalRectBottom,i);

            // Draw the station name below the vertical rectangle
            drawStationName(canvas, verticalRectLeft, verticalRectBottom + TEXT_SIZE, stationNames.get(i),i);
        }
    }

    private void drawCircle(Canvas canvas, int centerX, int centerY, int i) {
        Paint circlePaint = new Paint();
        if(i>Current_i) {
            circlePaint.setColor(Color.parseColor("#d4d4d4"));
        }else if(Current_i == i) {
            circlePaint.setColor(Color.YELLOW);
        }else{
            circlePaint.setColor(Color.parseColor("#1da905"));
        }
        circlePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, CIRCLE_RADIUS+10, circlePaint);
    }

    private void drawRectangle(Canvas canvas, int left, int top, int right, int bottom, int i) {
        Paint rectanglePaint = new Paint();
        if(i>Current_i) {
            rectanglePaint.setColor(Color.parseColor("#d4d4d4"));
        }else if(Current_i == i) {
            rectanglePaint.setColor(Color.parseColor("#d4d4d4"));
        }else{
            rectanglePaint.setColor(Color.parseColor("#1da905"));
        }
        rectanglePaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(left, top, right, bottom, rectanglePaint);
    }

    private void drawVerticalRectangle(Canvas canvas, int left, int top, int right, int bottom, int i) {
        Paint verticalRectPaint = new Paint();
        if(i<Current_i) {
            verticalRectPaint.setColor(Color.parseColor("#1da905"));
        }else if(Current_i == i) {
            verticalRectPaint.setColor(Color.YELLOW);
        }else{
            verticalRectPaint.setColor(Color.parseColor("#d4d4d4"));
        }
        verticalRectPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(left, top, right, bottom, verticalRectPaint);
    }

    private void drawStationName(Canvas canvas, int left, int top, String stationName, int j) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float x = left + RECTANGLE_WIDTH / 2;
        float y = top;

        // Split the station name into individual characters
        char[] characters = stationName.toCharArray();

        // Calculate the vertical spacing between lines
        float lineHeight = textPaint.getFontSpacing();

        // Draw each character on a separate line
        for (int i = 0; i < characters.length; i++) {
            // Calculate the Y position for each line
            float lineY = y + (i * lineHeight);

            // Draw each character on its own line
            canvas.drawText(String.valueOf(characters[i]), x-25, lineY, textPaint);
        }
    }
}







