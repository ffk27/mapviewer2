package ffk27.mapviewer2;

import javafx.geometry.BoundingBox;

import java.awt.image.BufferedImage;

/**
 * Created by Gebruiker on 2/18/2017.
 */
public class RasterImage {
    private BufferedImage image;
    private BoundingBox boundingBox;

    public RasterImage(BufferedImage image, BoundingBox boundingBox) {
        this.image = image;
        this.boundingBox = boundingBox;
    }

    public BufferedImage getImage() {
        return image;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }
}
