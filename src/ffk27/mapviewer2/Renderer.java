package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.Coordinate;
import javafx.geometry.BoundingBox;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created by ffk27 on 1-3-2017.
 */
public class Renderer extends Thread {
    private MapView mapView;
    private BoundingBox boundingBox;
    private List<RenderRule> renderRules;
    private ViewModel viewModel;
    private Drawer[] drawers;
    private boolean stop;
    private RasterImage mapImage;

    public Renderer(MapView mapView, BoundingBox boundingBox, List<RenderRule> renderRules, ViewModel viewModel) {
        this.mapView=mapView;
        this.boundingBox = boundingBox;
        this.renderRules=renderRules;
        this.viewModel=viewModel;
        start();
    }

    @Override
    public void run() {
        super.run();

        mapImage = new RasterImage(new BufferedImage(viewModel.getScreenSize().width,viewModel.getScreenSize().height,BufferedImage.TYPE_INT_ARGB), boundingBox);
        int z = (int)Math.floor(viewModel.getZoomLevel());
        double extent=0;
        if (viewModel.getSrid()==3857) {
            extent=-1*Utils.webmercator.getMinX();
        }
        double size = extent * 2 / Math.pow(2, z);

        int xt1 = (int) Math.floor((boundingBox.getMinX() + extent) / size);
        int xt2 = (int) Math.floor((boundingBox.getMaxX() + extent) / size);

        int yt1 = (int) Math.floor((extent - boundingBox.getMaxY()) / size);
        int yt2 = (int) Math.floor((extent - boundingBox.getMinY()) / size);

        drawers = new Drawer[(xt2 - xt1 + 1) * (yt2 - yt1 + 1)];

        for (int yt=yt1; yt<yt2+1; yt++) {
            for (int xt=xt1; xt<xt2+1; xt++) {
                double minX = Utils.webmercator.getMinX() + xt * size;
                double maxX = minX + size;
                double maxY = Utils.webmercator.getMaxY() - yt * size;
                double minY = maxY - size;

                if (minX < boundingBox.getMinX()) {
                    minX=boundingBox.getMinX();
                }
                if (maxX > boundingBox.getMaxX()) {
                    maxX=boundingBox.getMaxX();
                }
                if (minY < boundingBox.getMinY()) {
                    minY = boundingBox.getMinY();
                }
                if (maxY > boundingBox.getMaxY()) {
                    maxY = boundingBox.getMaxY();
                }

                BoundingBox bbox = new BoundingBox(minX,minY,maxX-minX,maxY-minY);

                drawers[(yt-yt1) * (xt2-xt1+1) + xt-xt1] = new Drawer(this,bbox,renderRules,viewModel,(int)Math.round(Utils.unitToPixel(maxX-minX,viewModel.getUnitSize())),(int)Math.round(Utils.unitToPixel(maxY-minY,viewModel.getUnitSize())),xt,yt,z);
            }
        }
    }

    public void stopt() {
        stop=true;
        if (drawers!=null) {
            for (Drawer d : drawers) {
                if (d!=null) {
                    d.stopt();
                }
            }
        }
    }

    public void updateImage(RasterImage rasterImage) {
        if (!stop) {
            Point tl = Utils.coordinateToPixels(new Coordinate(rasterImage.getBoundingBox().getMinX(), rasterImage.getBoundingBox().getMaxY()), viewModel.getUnitSize(), boundingBox);
            Point br = Utils.coordinateToPixels(new Coordinate(rasterImage.getBoundingBox().getMaxX(), rasterImage.getBoundingBox().getMinY()), viewModel.getUnitSize(), boundingBox);
            mapImage.getG2D().drawImage(rasterImage.getImage(), tl.x, tl.y, br.x, br.y, 0, 0, rasterImage.getImage().getWidth(), rasterImage.getImage().getHeight(), null);
            mapView.updateMapImage(mapImage);
        }
    }
}
