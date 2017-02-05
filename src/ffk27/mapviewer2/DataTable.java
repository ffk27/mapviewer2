package ffk27.mapviewer2;

/**
 * Created by Gebruiker on 2/5/2017.
 */
public abstract class DataTable {
    protected String[] columnNames;
    protected String[] columnTypes;

    public boolean columnNameExist(String columnName) {
        for (int i=0; i<columnNames.length; i++) {
            if (columnNames[i].equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String string = "";
        for (int i=0; i< columnNames.length; i++) {
            string+=", " + columnNames[i] + " : " + columnTypes[i];
        }
        if (string.length()>0) {
            return string.substring(1);
        }
        return string;
    }
}
