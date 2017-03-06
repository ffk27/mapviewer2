package ffk27.mapviewer2;

import javafx.geometry.BoundingBox;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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

    public TileData(String name, int srid, String urlformat, BoundingBox bounds, int minzoom, int maxzoom) {
        super(name,srid);
        this.urlformat = urlformat;
        this.bounds=bounds;
        this.minzoom=minzoom;
        this.maxzoom=maxzoom;
        tileCache = Collections.synchronizedList(new ArrayList<>());
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
                if (bounds.contains(bbox)) {
                    System.out.println(url);
                    tile.setImage(ImageIO.read(url));
                    synchronized (tileCache) {
                        tileCache.add(tile);
                    }
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
}
