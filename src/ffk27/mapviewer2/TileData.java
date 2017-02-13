package ffk27.mapviewer2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Gebruiker on 2/13/2017.
 */
public class TileData extends GeoDataSource {
    private String urlformat;

    public TileData(String name, String urlformat) {
        super.name=name;
        this.urlformat = urlformat;
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

    public BufferedImage getTile(int x, int y, int z) {
        URL url = buildURL(x,y,z);
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
