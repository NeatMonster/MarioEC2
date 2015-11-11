package com.mojang.mario.sprites;

import com.mojang.mario.Art;

public class Particle extends Sprite
{
    public int life;
    public SpriteContext ctx;
    
    public Particle(SpriteContext ctx, int x, int y, float xa, float ya)
    {
        this(ctx, x, y, xa, ya, (int)(Math.random()*2), 0);
    }

    public Particle(SpriteContext ctx, int x, int y, float xa, float ya, int xPic, int yPic)
    {
        this.ctx = ctx;
        sheet = Art.particles;
        this.x = x;
        this.y = y;
        this.xa = xa;
        this.ya = ya;
        this.xPic = xPic;
        this.yPic = yPic;
        this.xPicO = 4;
        this.yPicO = 4;
        
        wPic = 8;
        hPic = 8;
        life = 10;
    }

    public void move()
    {
        if (life--<0) ctx.removeSprite(this);
        x+=xa;
        y+=ya;
        ya*=0.95f;
        ya+=3;
    }
}