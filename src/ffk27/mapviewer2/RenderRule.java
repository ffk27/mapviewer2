package ffk27.mapviewer2;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ffk27 on 9-8-16.
 */
public class RenderRule {
    protected RenderRule parent;
    protected List<RenderRule> rules;
    private GeoDataSource dataSource;
    private float zoommin;
    private float zoommax;

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

    public static RenderRule getStyleRule(Node nRule, RenderRule parent, List<GeoDataSource> dataSources) {
        NamedNodeMap namedNodeMap = nRule.getAttributes();
        String sourceType = Utils.getAttributeStringValue("sourcetype",namedNodeMap);
        RenderRule renderRule=null;
        if (sourceType==null || sourceType.equals("jdbcvector")) {
            renderRule = new VectorRenderRule();
        }
        else {
            renderRule=new RenderRule();
        }
        renderRule.setZoommin(Utils.getAttributeFloatValue("zoom-min",namedNodeMap));
        renderRule.setZoommax(Utils.getAttributeFloatValue("zoom-max",namedNodeMap));
        String sourceName = Utils.getAttributeStringValue("sourcename",namedNodeMap);
        if (sourceName!=null) {
            for (GeoDataSource dataSource : dataSources) {
                if (dataSource.getName().equals(sourceName)) {
                    renderRule.setDataSource(dataSource);
                    break;
                }
            }
        }
        if (parent!=null) {
            parent.setParent(parent);
        }
        boolean hasChilds=false;
        List<RenderRule> styleRules = null;
        for (int i=0; i<nRule.getChildNodes().getLength(); i++) {
            if (nRule.getChildNodes().item(i).getNodeName()=="rule") {
                hasChilds=true;
                if (styleRules==null) {
                    styleRules = new ArrayList<>();
                }
                RenderRule  sc = getStyleRule(nRule.getChildNodes().item(i),renderRule,dataSources);
                if (sc!=null) {
                    styleRules.add(sc);
                }
            }
        }
        renderRule.setRules(styleRules);

        if (sourceType==null || sourceType.equals("jdbcvector")) {
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
                        String fontsize = Utils.getAttributeStringValue("font-size",attributes);
                        //TODO parse font
                        vectorRenderRule.getStyles().add(new TextStyle(vectorRenderRule,fill,line,stroke,format,null));
                    }
                }
            }
            return vectorRenderRule;
        }
        return renderRule;
    }
}
