package ffk27.mapviewer2;

import com.vividsolutions.jts.geom.Coordinate;
import ffk27.mapviewer2.*;
import javafx.geometry.BoundingBox;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.activation.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by ffk27 on 15-4-16.
 */
public class Utils {
    public static final BoundingBox webmercator = new BoundingBox(-20037508.34,-20037508.34,40075016.68,40075016.68);

    public static String geomTypeCode2Name(int code) {
        String name="";
        switch (code) {
            case 0:
                name="Geometry";
                break;
            case 1:
                name="Point";
                break;
            case 2:
                name="LineString";
                break;
            case 3:
                name="Polygon";
                break;
            case 4:
                name="MultiPoint";
                break;
            case 5:
                name="MultiLineString";
                break;
            case 6:
                name="MultiPolygon";
                break;
            case 7:
                name="GeometryCollection";
                break;
            //Below unsupported.
            case 8:
                name="CircularString";
                break;
            case 9:
                name="CompoundCurve";
                break;
            case 10:
                name="CurvePolygon";
                break;
            case 11:
                name="MultiCurve";
                break;
            case 12:
                name="MultiSurface";
                break;
            case 13:
                name="Curve";
                break;
            case 14:
                name="Surface";
                break;
            case 15:
                name="PolyhedralSurface";
                break;
            case 16:
                name="TIN";
                break;
            case 17:
                name="Triangle";
                break;
        }
        return name;
    }

    public static String getAttributeStringValue(String attribute, NamedNodeMap namedNodeMap) {
        Node node = namedNodeMap.getNamedItem(attribute);
        if (node!=null) {
            return node.getTextContent();
        }
        return null;
    }

    public static float getAttributeFloatValue(String attribute, NamedNodeMap namedNodeMap) {
        String value = getAttributeStringValue(attribute,namedNodeMap);
        if (value!=null) {
            return Float.parseFloat(value);
        }
        return 0.0f;
    }

    public static int getAttributeIntValue(String attribute, NamedNodeMap namedNodeMap) {
        String value = getAttributeStringValue(attribute,namedNodeMap);
        if (value!=null) {
            return Integer.parseInt(value);
        }
        return 0;
    }

    public static Color getAttributeColor(String attribute, NamedNodeMap namedNodeMap) {
        String value = getAttributeStringValue(attribute, namedNodeMap);
        if (value!=null) {
            if (value.startsWith("#")) {
                try {
                    return new Color((int) Long.parseLong(value.substring(1), 16), true);
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public static double unitToPixel(double c, double unitSize) {
        return c * unitSize;
    }

    public static double pixelToUnit(double p, double unitSize) {
        return p/unitSize;
    }

    public static Point coordinateToPixels(Coordinate c, double unitSize, BoundingBox boundingBox) {
        Point point = new Point();
        point.setLocation(unitToPixel(c.x-boundingBox.getMinX(),unitSize),unitToPixel(boundingBox.getMaxY()-c.y,unitSize));
        return point;
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
