package com.mojang.mario.sprites;

import com.mojang.mario.Art;


public class CoinAnim extends Sprite
{
    private int life = 10;
    private SpriteContext ctx;

    public CoinAnim(SpriteContext ctx, int xTile, int yTile)
    {
        this.ctx = ctx;
        sheet = Art.level;
        wPic = hPic = 16;

        x = xTile * 16;
        y = yTile * 16 - 16;
        xa = 0;
        ya = -6f;
        xPic = 0;
        yPic = 2;
    }

    public void move()
    {
        if (life-- < 0)
        {
            ctx.removeSprite(this);
            for (int xx = 0; xx < 2; xx++)
                for (int yy = 0; yy < 2; yy++)
                    ctx.addSprite(new Sparkle(ctx, (int)x + xx * 8 + (int) (Math.random() * 8), (int)y + yy * 8 + (int) (Math.random() * 8), 0, 0, 0, 2, 5));
        }

        xPic = life & 3;

        x += xa;
        y += ya;
        ya += 1;
    }
}