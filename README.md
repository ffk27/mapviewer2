# mapviewer
- Supports PostGIS, H2 and more.
- Basic and easy cartography styling.
- Map tiles
![alt tag](https://raw.githubusercontent.com/ffk27/mapviewer2/master/mapviewer.png)
![alt tag](https://raw.githubusercontent.com/ffk27/mapviewer2/master/mapviewer2.png)
```java
 JFrame jFrame = new JFrame("Gisser");
        jFrame.setSize(640,480);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        MapView mapView = new MapView(new Coordinate(673316, 6888714),12f,3857);
        jFrame.add(mapView);

        jFrame.setVisible(true);

        try {
            TileData osm = new TileData("Openstreetmap",3857,"http://[a-c].tile.openstreetmap.org/{z}/{x}/{y}.png", Utils.webmercator, 0, 19);

            String url = "jdbc:h2:D:/gis/gisdb";
            Properties props = new Properties();
            props.setProperty("user", "sa");
            props.setProperty("password", "");
            JDBCConnection jdbcConnection = new JDBCConnection(JDBCConnection.DBType.H2,url,props);

            JDBCDataTable buurtentabel = new JDBCDataTable(jdbcConnection,"buurten");

            JDBCVectorData buurten = new JDBCVectorData("wijken",3857,buurtentabel,"webgeom");

            List<GeoDataSource> dataSources = new ArrayList<>();
            dataSources.add(osm);
            dataSources.add(buurten);

            mapView.setRenderRules(RenderRule.parseXMLStyle(new String(Files.readAllBytes(Paths.get("rules.xml"))),dataSources));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Server.createWebServer().start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
```
Rule example:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<rules>
    <source sourcename="Openstreetmap"/>
    <source attributes="BU_NAAM,AANT_INW" sourcename="wijken">
        <if stmt="water = 'NEE' AND bev_dichth > 200">
            <path fill="#0FFF0000" stroke="#FF000000" stroke-width="1px"/>
            <text format="{BU_NAAM}\n\n{AANT_INW}" font-style="italic" font-size="12"/>
        </if>
    </source>
</rules>
```