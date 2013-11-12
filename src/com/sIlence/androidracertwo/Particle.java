/*
 *
 * This file is part of AndroidRacerTwo
 *
 * AndroidRacerTwo is free software: you can redistribute it and/or modify
 * it under the term of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the Licence, or
 * (at your option) any later version.
 * 
 * AndroidRacerTwo is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with AndroidRacerTwo. If not, see <http://www.gnu.org/licenses/>
 *
 * Copyright: 2013 Mytchel Hammond <mytchel.hammond@gmail.com>
 *
*/


package com.sIlence.androidracertwo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Surface;
import java.util.Random;
import java.lang.Math;
import android.util.Log;

public class Particle {

    private float mx, my;
    private float xv, yv;
    private int age;
    private int	stop;
    private int	color;
    private Random rand;
    private boolean alive;
    private Paint brush;

    public Particle(int c, int x, int y, int O, int Oerror, float speed, float sError, int sto) {
        rand = new Random();
        brush = new Paint(Paint.ANTI_ALIAS_FLAG);

        color = c;
        mx = x;
        my = y;

        stop = rand.nextInt(sto * 2);
        age = 0;
        alive = true;

        float rspeed = speed + ((rand.nextFloat() * sError) - (sError / 2));

        Oerror = (int) (-60f * rspeed + 180f) + Oerror;
        int d = O + ((int) (rand.nextFloat() * Oerror)) - (Oerror / 2);

        if (d > 360) d -= 360;
        if (d < 0) d += 360;

        O = d;
        d = d % 90; 
        float r = (float) d / 180 * (float) Math.PI;
        float a = (float) Math.cos(r) * rspeed;
        float o = (float) Math.sin(r) * rspeed;
        if (O < 90) {
            xv = o;
            yv = -a;
        }
        if (O > 90 && O < 180) {
            xv = a;
            yv = o;
        }
        if (O > 180 && O < 270) {
            xv = -o;
            yv = a;
        }
        if (O > 270) {
            xv = -a;
            yv = -o;
        }
    }

    public Particle(int c, int x, int y, int O, float speed, int sto) {
        this(c, x, y, O, 40, speed, 0.5f, sto);
    }

    public void update() {
        if (alive) {

            if (age > stop) {
                int a = Color.alpha(color);
                a = (int) (255f * (1 - Math.sin(Math.toRadians(age * 1.5f))));
                color = Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
                if (a < 20) alive = false;
            }

            mx += xv * (1 - (float) Math.sin(Math.toRadians(age * 1.5f)));
            my += yv * (1 - (float) Math.sin(Math.toRadians(age * 1.5f)));

            age++;
        }
    }

    public void render (Canvas c) {
        brush.setColor(color);
        c.drawPoint(mx, my, brush);
    }

    public boolean isAlive() {
        return alive;
    }
}
