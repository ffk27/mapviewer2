package ffk27.mapviewer2;

import javafx.geometry.BoundingBox;
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
    private boolean enabled;
    protected RenderRule parent;
    protected List<RenderRule> rules;
    protected GeoDataSource dataSource;
    protected float zoommin;
    protected float zoommax;

    public RenderRule(GeoDataSource geoDataSource) {
        this.dataSource=geoDataSource;
        enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public abstract void draw(RenderRule renderRule, Graphics2D g2d, Drawer drawer);

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
        return dataSource;
    }

    public void setDataSource(GeoDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static List<RenderRule> parseXMLStyle(String xml, List<GeoDataSource> dataSources) {
        List<RenderRule> renderRules = new ArrayList<>();
        try {
            Document doc = Utils.loadXMLFromString(xml);
            Node nRule = doc.getFirstChild();
            for (int i = 0; i < nRule.getChildNodes().getLength(); i++) {
                Node child = nRule.getChildNodes().item(i);
                if (child.getNodeName().toLowerCase().equals("source")) {
                    renderRules.add(getStyleRule(child, null, dataSources));
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return renderRules;
    }

    private static RenderRule getStyleRule(Node nRule, RenderRule parent, List<GeoDataSource> dataSources) {
        NamedNodeMap namedNodeMap = nRule.getAttributes();
        GeoDataSource geoDataSource = null;
        String sourceName = Utils.getAttributeStringValue("sourcename", namedNodeMap);
        if (sourceName != null) {
            // if sourcename isset, find the matching geodatasource object.
            for (GeoDataSource dataSource : dataSources) {
                if (dataSource.getName().equals(sourceName)) {
                    geoDataSource = dataSource;
                    break;
                }
            }
            if (geoDataSource == null) {
                System.out.println("Error: source " + sourceName + " unknown!");
            }
        }
        else if (parent!=null) {
            geoDataSource=parent.getDataSource();
        }

        if (geoDataSource != null) {
            RenderRule renderRule = null;
            if (geoDataSource instanceof JDBCVectorData) {
                renderRule = new JDBCRenderRule(geoDataSource);
            } else if (geoDataSource instanceof Geoms) {
                renderRule = new GeomsRenderRule(geoDataSource);
            } else if (geoDataSource instanceof TileData) {
                renderRule = new TileRule(geoDataSource);
            }

            renderRule.setDataSource(geoDataSource);

            if (parent != null) {
                renderRule.setParent(parent);
            }

            renderRule.setZoommin(Utils.getAttributeFloatValue("zoom-min", namedNodeMap));
            renderRule.setZoommax(Utils.getAttributeFloatValue("zoom-max", namedNodeMap));

            if (renderRule.getZoommin() > renderRule.getZoommax()) {
                renderRule.setZoommax(24);
            }

            if (renderRule instanceof VectorRenderRule) {
                VectorRenderRule vectorRenderRule = (VectorRenderRule) renderRule;
                String attrstring = Utils.getAttributeStringValue("attributes", namedNodeMap);
                String[] attrs = null;
                if (attrstring != null) {
                    attrs = attrstring.split(",");
                }
                vectorRenderRule.setAttributes(getAllAttributes((VectorRenderRule) vectorRenderRule.getParent(), attrs));
                String stmt = Utils.getAttributeStringValue("stmt", namedNodeMap);
                String[] stmts = null;
                if (stmt != null) {
                    stmts = new String[]{stmt};
                }
                vectorRenderRule.setStatement(getAllStatements((VectorRenderRule) vectorRenderRule.getParent(), stmts));

                for (int i = 0; i < nRule.getChildNodes().getLength(); i++) {
                    if (vectorRenderRule.getStyles() == null) {
                        vectorRenderRule.setStyles(new ArrayList<>());
                    }
                    Node node = nRule.getChildNodes().item(i);
                    String nodename = node.getNodeName().toLowerCase();
                    if (nodename.equals("path") || nodename.equals("circle") || nodename.equals("text")) { //current supported styletags
                        NamedNodeMap attributes = node.getAttributes();
                        Color fill = Utils.getAttributeColor("fill", attributes);
                        Color line = Utils.getAttributeColor("stroke", attributes);
                        String strokewidth = Utils.getAttributeStringValue("stroke-width", attributes);
                        Stroke stroke = null;
                        if (strokewidth != null) {
                            strokewidth = strokewidth.trim();
                            if (strokewidth.endsWith("px")) {

                            } else if (strokewidth.endsWith("em")) {

                            } else if (strokewidth.endsWith("%")) {

                            } else {
                                stroke = new BasicStroke(Float.parseFloat(strokewidth), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                            }
                        }
                        if (nodename.equals("path")) {
                            vectorRenderRule.getStyles().add(new Style(vectorRenderRule, fill, line, stroke));
                        } else if (nodename.equals("circle")) {
                            float radius = Utils.getAttributeFloatValue("radius", attributes);
                            vectorRenderRule.getStyles().add(new CircleStyle(vectorRenderRule, fill, line, stroke, radius));
                        } else if (nodename.equals("text")) {
                            String format = Utils.getAttributeStringValue("format", attributes);
                            String fontfamily = Utils.getAttributeStringValue("font-family", attributes);
                            String fontstyle = Utils.getAttributeStringValue("font-style", attributes);
                            int fstyle = 0;
                            if (fontstyle.contains("bold")) {
                                fstyle = fstyle | Font.BOLD;
                            }
                            if (fontstyle.contains("italic")) {
                                fstyle = fstyle | Font.ITALIC;
                            }
                            int fontsize = Utils.getAttributeIntValue("font-size", attributes);
                            Font font = new Font(fontfamily, fstyle, fontsize);
                            vectorRenderRule.getStyles().add(new TextStyle(vectorRenderRule, fill, line, stroke, format, font));
                        }
                    }
                }
            }
            boolean hasChilds = false;
            List<RenderRule> styleRules = null;
            for (int i = 0; i < nRule.getChildNodes().getLength(); i++) {
                String nodeName = nRule.getChildNodes().item(i).getNodeName().toLowerCase();
                if (nodeName.equals("rule") || nodeName.equals("if") || nodeName.equals("elseif") || nodeName.equals("else")) {
                    hasChilds = true;
                    if (styleRules == null) {
                        styleRules = new ArrayList<>();
                    }
                    RenderRule sc = getStyleRule(nRule.getChildNodes().item(i), renderRule, dataSources);
                    if (sc != null) {
                        styleRules.add(sc);
                    }
                }
            }
            renderRule.setRules(styleRules);
            return renderRule;
        }
        System.out.println("Error: no datasource found");
        return null;
    }

    private static String[] getAllStatements(VectorRenderRule vectorRenderRule, String statements[]) {
        if (vectorRenderRule != null) {
            String[] stmts = null;
            if (vectorRenderRule.getStatement() != null) {
                if (statements != null) {
                    stmts = new String[vectorRenderRule.getStatement().length + statements.length];
                    for (int i = 0; i < vectorRenderRule.getStatement().length; i++) {
                        stmts[i] = vectorRenderRule.getStatement()[i];
                    }
                    for (int i = vectorRenderRule.getStatement().length; i < vectorRenderRule.getStatement().length + statements.length; i++) {
                        stmts[i] = statements[i - vectorRenderRule.getStatement().length];
                    }
                } else {
                    stmts = vectorRenderRule.getStatement();
                }
            } else if (statements != null) {
                stmts = statements;
            }
            if (vectorRenderRule.getParent() != null) {
                stmts = getAllAttributes((VectorRenderRule) vectorRenderRule.getParent(), stmts);
            }
            return stmts;
        }
        return statements;
    }

    private static String[] getAllAttributes(VectorRenderRule vectorRenderRule, String attributes[]) {
        if (vectorRenderRule != null) {
            String[] attrs = null;
            if (vectorRenderRule.getAttributes() != null) {
                if (attributes != null) {
                    attrs = new String[vectorRenderRule.getAttributes().length + attributes.length];
                    for (int i = 0; i < vectorRenderRule.getAttributes().length; i++) {
                        attrs[i] = vectorRenderRule.getAttributes()[i];
                    }
                    for (int i = vectorRenderRule.getAttributes().length; i < vectorRenderRule.getAttributes().length + attributes.length; i++) {
                        attrs[i] = attributes[i - vectorRenderRule.getAttributes().length];
                    }
                } else {
                    attrs = vectorRenderRule.getAttributes();
                }
            } else if (attributes != null) {
                attrs = attributes;
            }
            if (vectorRenderRule.getParent() != null) {
                attrs = getAllAttributes((VectorRenderRule) vectorRenderRule.getParent(), attrs);
            }
            return attrs;
        }
        return attributes;
    }
}
