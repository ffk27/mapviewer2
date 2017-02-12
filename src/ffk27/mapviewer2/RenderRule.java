package ffk27.mapviewer2;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ffk27 on 9-8-16.
 */
public abstract class RenderRule {
    protected RenderRule parent;
    protected List<RenderRule> rules;
    protected GeoDataSource dataSource;
    protected float zoommin;
    protected float zoommax;

    public abstract void draw(RenderRule renderRule, Graphics2D g2d, MapView mapView);

    public RenderRule getParent() {
        return parent;
    }

    public void setParent(RenderRule parent) {
        this.parent = parent;
    }

    public List<RenderRule> getRules() {
        return rules;
    }

    public void setRules(List<RenderRule> rules) {
        this.rules = rules;
    }

    public float getZoommin() {
        return zoommin;
    }

    public void setZoommin(float zoommin) {
        this.zoommin = zoommin;
    }

    public float getZoommax() {
        return zoommax;
    }

    public void setZoommax(float zoommax) {
        this.zoommax = zoommax;
    }

    public GeoDataSource getDataSource() {
        if (dataSource==null && parent!=null) {
            return parentDataSource(parent);
        }
        return dataSource;
    }

    public void setDataSource(GeoDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private GeoDataSource parentDataSource(RenderRule renderRule) {
        if (renderRule.dataSource!=null) {
            return renderRule.dataSource;
        }
        else if (renderRule.getParent()!=null) {
            return parentDataSource(renderRule.getParent());
        }
        return null;
    }

    public static RenderRule parseXMLStyle(String xml, List<GeoDataSource> dataSources) {
        RenderRule renderRule = null;
        try {
            Document doc = Utils.loadXMLFromString(xml);
            Node nRule = doc.getFirstChild();
            renderRule = getStyleRule(nRule,null, dataSources);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return renderRule;
    }

    private static RenderRule getStyleRule(Node nRule, RenderRule parent, List<GeoDataSource> dataSources) {
        RenderRule renderRule=null;
        NamedNodeMap namedNodeMap = nRule.getAttributes();

        String sourceType = Utils.getAttributeStringValue("sourcetype",namedNodeMap);
        //currently only jdbcvectorsource supported.
        if (sourceType==null || sourceType.equals("jdbcvector")) {
            renderRule = new VectorRenderRule();
        }

        renderRule.setZoommin(Utils.getAttributeFloatValue("zoom-min",namedNodeMap));
        renderRule.setZoommax(Utils.getAttributeFloatValue("zoom-max",namedNodeMap));

        String sourceName = Utils.getAttributeStringValue("sourcename",namedNodeMap);
        if (sourceName!=null) {
            // if sourcename isset, find the matching geodatasource object.
            for (GeoDataSource dataSource : dataSources) {
                if (dataSource.getName().equals(sourceName)) {
                    renderRule.setDataSource(dataSource);
                    break;
                }
            }
        }

        if (parent!=null) {
            renderRule.setParent(parent);
        }
        boolean hasChilds=false;
        List<RenderRule> styleRules = null;
        for (int i=0; i<nRule.getChildNodes().getLength(); i++) {
            if (nRule.getChildNodes().item(i).getNodeName()=="rule") {
                hasChilds=true;
                if (styleRules==null) {
                    styleRules = new ArrayList<>();
                }
                RenderRule sc = getStyleRule(nRule.getChildNodes().item(i),renderRule,dataSources);
                if (sc!=null) {
                    styleRules.add(sc);
                }
            }
        }
        renderRule.setRules(styleRules);

        if (renderRule instanceof VectorRenderRule) {
            VectorRenderRule vectorRenderRule = (VectorRenderRule)renderRule;

            String attrs = Utils.getAttributeStringValue("attributes",namedNodeMap);
            if (attrs!=null && !attrs.isEmpty()) {
                vectorRenderRule.setAttributes(attrs.split(","));
            }
            vectorRenderRule.setStatement(Utils.getAttributeStringValue("stmt",namedNodeMap));

            for (int i=0; i<nRule.getChildNodes().getLength(); i++) {
                if (vectorRenderRule.getStyles()==null) {
                    vectorRenderRule.setStyles(new ArrayList<>());
                }
                Node node = nRule.getChildNodes().item(i);
                String nodename = node.getNodeName();
                if (nodename.equals("path") || nodename.equals("circle") || nodename.equals("text")) { //current supported styletags
                    NamedNodeMap attributes = node.getAttributes();
                    Color fill = Utils.getAttributeColor("fill", attributes);
                    Color line = Utils.getAttributeColor("stroke", attributes);
                    String strokewidth = Utils.getAttributeStringValue("stroke-width", attributes);
                    Stroke stroke = null;
                    if (strokewidth != null) {
                        strokewidth=strokewidth.trim();
                        if (strokewidth.endsWith("px")) {

                        } else if (strokewidth.endsWith("em")) {

                        } else if (strokewidth.endsWith("%")) {

                        } else {
                            stroke = new BasicStroke(Float.parseFloat(strokewidth), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                        }
                    }
                    if (nodename.equals("path")) {
                        vectorRenderRule.getStyles().add(new Style(vectorRenderRule,fill, line, stroke));
                    }
                    else if (nodename.equals("circle")) {
                        float radius = Utils.getAttributeFloatValue("radius",attributes);
                        vectorRenderRule.getStyles().add(new CircleStyle(vectorRenderRule,fill,line,stroke,radius));
                    }
                    else if (nodename.equals("text")) {
                        String format = Utils.getAttributeStringValue("format",attributes);
                        String fontfamily = Utils.getAttributeStringValue("font-family",attributes);
                        String fontstyle = Utils.getAttributeStringValue("font-style",attributes);
                        int fstyle=0;
                        if (fontstyle.contains("bold")) {
                            fstyle=fstyle|Font.BOLD;
                        }
                        if (fontstyle.contains("italic")) {
                            fstyle=fstyle|Font.ITALIC;
                        }
                        int fontsize = Utils.getAttributeIntValue("font-size",attributes);
                        Font font = new Font(fontfamily,fstyle,fontsize);
                        vectorRenderRule.getStyles().add(new TextStyle(vectorRenderRule,fill,line,stroke,format,font));
                    }
                }
            }
            return vectorRenderRule;
        }
        return renderRule;
    }
}
