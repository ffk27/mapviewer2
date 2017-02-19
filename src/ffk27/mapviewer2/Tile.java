package ffk27.mapviewer2;

import javafx.geometry.BoundingBox;

import java.awt.image.BufferedImage;

/**
 * Created by Gebruiker on 2/18/2017.
 */
public class Tile extends RasterImage {
    private int x,y,z;

    public Tile(BufferedImage image, BoundingBox boundingBox, int x, int y, int z) {
        super(image, boundingBox);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int[] getTilePos() {
        return new int[] {x,y,z};
    }

    @Override
    public String toString() {
        return x+","+y+","+z+" -- "+getBoundingBox();
    }
}
