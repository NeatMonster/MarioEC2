package fr.neatmonster.labs;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public class DummyGC extends GraphicsConfiguration {

    @Override
    public BufferedImage createCompatibleImage(final int width,
            final int height, final int transparency) {
        return new BufferedImage(width, height, transparency);
    }

    @Override
    public Rectangle getBounds() {
        return null;
    }

    @Override
    public ColorModel getColorModel() {
        return null;
    }

    @Override
    public ColorModel getColorModel(final int transparency) {
        return null;
    }

    @Override
    public AffineTransform getDefaultTransform() {
        return null;
    }

    @Override
    public GraphicsDevice getDevice() {
        return null;
    }

    @Override
    public AffineTransform getNormalizingTransform() {
        return null;
    }
}
