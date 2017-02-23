package ffk27.mapviewer2;

import javafx.geometry.BoundingBox;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Gebruiker on 2/13/2017.
 */
public class TileData extends GeoDataSource {
    private String urlformat;
    private List<Tile> tileCache;
    private BoundingBox bounds;
    public double extent;
    public int minzoom,maxzoom;
    public Thread collector;

    public TileData(String name, int srid, String urlformat, BoundingBox bounds, int minzoom, int maxzoom) {
        super(name,srid);
        this.urlformat = urlformat;
        this.bounds=bounds;
        this.minzoom=minzoom;
        this.maxzoom=maxzoom;
        tileCache = new ArrayList<>();
        extent = -1 * bounds.getMinX();
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    private URL buildURL(int x, int y, int z) {
        String url = "";
        url = urlformat.replace("{x}",""+x);
        url = url.replace("{y}",""+y);
        url = url.replace("{z}",""+z);
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            if (matcher.group().contains("-")) {
                int min = (int)matcher.group().charAt(1);
                int max = (int)matcher.group().charAt(3);
                int random = new Random().nextInt((max - min) + 1) + min;
                url = url.replace(matcher.group(),(char)random+"");
            }
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Tile tileInCache(int x, int y, int z) {
        for (Tile tile : tileCache) {
            if (tile.getTilePos()[0]==x && tile.getTilePos()[1]==y && tile.getTilePos()[2]==z) {
                return tile;
            }
        }
        return null;
    }

    public List<Tile> getTileCache() {
        return tileCache;
    }

    public Tile getTile(int x, int y, int z) {
        Tile tile = tileInCache(x,y,z);
        if (tile!=null) {
            return tile;
        }
        else {
            double size = extent*2 / Math.pow(2,z);
            double minX = -1 * extent + x*size;
            double minY = extent - (y+1)*size;
            BoundingBox bbox = new BoundingBox(minX,minY,size,size);
            tile = new Tile(bbox,x,y,z);
            URL url = buildURL(x, y, z);
            try {
                //-20037508.34
                if (bounds.contains(bbox)) {
                    System.out.println(url);
                    tile.setImage(ImageIO.read(url));
                    tileCache.add(tile);
                }
                else {
                    return null;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return tile;
    }

    public Tile[] getTiles(BoundingBox boundingBox, float zoomlvl) {
        int z = Math.round(zoomlvl);
        if (z >= minzoom && z <= maxzoom) {
            double size = extent * 2 / Math.pow(2, z);

            int xt1 = (int) Math.floor((boundingBox.getMinX() + extent) / size);
            int xt2 = (int) Math.floor((boundingBox.getMaxX() + extent) / size);

            int yt1 = (int) Math.floor((extent - boundingBox.getMaxY()) / size);
            int yt2 = (int) Math.floor((extent - boundingBox.getMinY()) / size);

            if (xt2-xt1>0 && yt2-yt1>0) {
                Tile[] tiles = new Tile[(xt2 - xt1 + 1) * (yt2 - yt1 + 1)];
                for (int xt = xt1; xt < xt2 + 1; xt++) {
                    for (int yt = yt1; yt < yt2 + 1; yt++) {
                        Tile t = getTile(xt, yt, z);
                        tiles[(xt - xt1) * (yt2 - yt1 + 1) + (yt - yt1)] = t;
                    }
                }
                return tiles;
            }
        }
        return null;
    }
}
