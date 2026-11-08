package com.centrepay.mobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** Lightweight geometric ornament used only as a background accent. */
public class MosaicAccentView extends View {
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MosaicAccentView(Context context) {
        super(context);
        line.setStyle(Paint.Style.STROKE); line.setStrokeWidth(2f); line.setColor(Color.rgb(103, 74, 57));
        fill.setStyle(Paint.Style.FILL); fill.setColor(Color.rgb(59, 40, 31));
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float tile = Math.max(22f, getWidth() / 5f);
        for (float y = -tile; y < getHeight() + tile; y += tile) for (float x = -tile; x < getWidth() + tile; x += tile) drawDiamond(canvas, x + tile / 2f, y + tile / 2f, tile * .36f);
    }

    private void drawDiamond(Canvas canvas, float x, float y, float radius) {
        Path path = new Path(); path.moveTo(x, y - radius); path.lineTo(x + radius, y); path.lineTo(x, y + radius); path.lineTo(x - radius, y); path.close(); canvas.drawPath(path, fill); canvas.drawPath(path, line); canvas.drawCircle(x, y, radius * .23f, line);
    }
}
