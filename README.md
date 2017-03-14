# mapviewer
- Supports PostGIS, H2 and more.
- Basic and easy cartography styling.
- Map tiles
![alt tag](https://raw.githubusercontent.com/ffk27/mapviewer2/master/mapviewer.png)
![alt tag](https://raw.githubusercontent.com/ffk27/mapviewer2/master/mapviewer2.png)
```
   public static void main(String[] args) {
        JFrame jFrame = new JFrame("Gisser");
        jFrame.setSize(640,480);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MapView mapView = new MapView(new Coordinate(673316, 6888714),12f,3857);
        jFrame.add(mapView);

        jFrame.setVisible(true);

        try {
            TileData osm = new TileData("Openstreetmap",3857,"http://[a-c].tile.openstreetmap.org/{z}/{x}/{y}.png", Utils.webmercator, 0, 19);
            TileData osmt = new TileData("Openstreetmap Transport",3857,"http://[a-c].tile.thunderforest.com/transport/{z}/{x}/{y}.png", Utils.webmercator,0,19);

            String url = "jdbc:h2:~/gisser";
            Properties props = new Properties();
            props.setProperty("user", "sa");
            props.setProperty("password", "");
            JDBCConnection jdbcConnection = new JDBCConnection(JDBCConnection.DBType.H2,url,props);
            JDBCDataTable jdbcDataTable = new JDBCDataTable(jdbcConnection,"osm_railways");
            JDBCVectorData osmrail = new JDBCVectorData("osmrail",3857,jdbcDataTable,"the_geom");

            List<GeoDataSource> dataSources = new ArrayList<>();
            dataSources.add(osm);
            dataSources.add(osmt);
            dataSources.add(osmrail);

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
    }
```
Rule example:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<rules>
    <source sourcename="Openstreetmap"/>
    <source sourcename="osmrail">
        <IF stmt="type = 'rail'">
            <rule>
                <path stroke="#FF000000" stroke-width="7"/>
            </rule>
            <rule>
                <path stroke="#FFFF0000" stroke-width="4"/>
            </rule>
        </IF>
    </source>
</rules>
```