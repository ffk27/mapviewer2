package ffk27.mapviewer2;

import javafx.geometry.BoundingBox;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Gebruiker on 2/18/2017.
 */
public class RasterImage {
    private BufferedImage image;
    private BoundingBox boundingBox;
    private Graphics2D g2d;

    public RasterImage(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public RasterImage(BufferedImage image, BoundingBox boundingBox) {
        this(boundingBox);
        this.image=image;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public Graphics2D getG2D() {
        if (g2d==null) {
            if (image!=null) {
                g2d = image.createGraphics();
            }
        }
        return g2d;
    }
}
