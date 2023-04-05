import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class PostgreSQLJDBC {
    private Connection c;
    private final String db_url;
    private final String user;
    private final String pass;

    public PostgreSQLJDBC(String db_url, String user, String pass) {
        this.db_url = db_url;
        this.user = user;
        this.pass = pass;
    }

    public void connect() throws SQLException {
        c = DriverManager.getConnection(db_url, user, pass);
        System.out.println("Welcome to The Elo Sorter!");
    }

    public void disconnect() throws SQLException {
        if (c != null) {
            c.close();
            System.out.print("Thank you! Come again!");
        }
    }

    public ArrayList<String> get_tables() throws SQLException {
        ArrayList<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = c.getMetaData();
        ResultSet resultSet = metaData.getTables(null, null, "%", new String[] {"TABLE"});
        while (resultSet.next()) {
            tables.add(resultSet.getString("TABLE_NAME"));
        }
        return tables;
    }

    public void insert(String table, String columns, String values) throws SQLException {
        String q = "INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")";
        PreparedStatement stmt = c.prepareStatement(q);
        stmt.executeUpdate();
        System.out.println("Successful Insertion!\n");
    }

    public void update(String table, String set, String where) throws SQLException {
        String q = "UPDATE " + table + " SET " + set + " WHERE " + where;
        PreparedStatement stmt = c.prepareStatement(q);
        stmt.executeUpdate();
    }

    public ArrayList<String> randomEntry(String table) throws SQLException {
        String q = "SELECT title, elo, id FROM " + table + " ORDER BY RANDOM() LIMIT 1";
        PreparedStatement stmt = c.prepareStatement(q);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            ArrayList<String> result = new ArrayList<String>();
            result.add(rs.getString(1));
            result.add(rs.getString(2));
            result.add(rs.getString(3));
            return result;
        } else {
            return null;
        }
    }

    public int search(String table, String title) throws SQLException {
        String q = "SELECT * FROM " + table + " WHERE title = '" + title + "'";
        PreparedStatement stmt = c.prepareStatement(q);
        ResultSet rs = stmt.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int id = 0;
        if(rs.next()) {
                id = (int) rs.getObject(1);
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    if(rs.getObject(i) != null) {
                        System.out.println(rsmd.getColumnName(i) + ": " + rs.getObject(i));
                    }
                }
        } else {
            throw new SQLException("Entry not found!");
        }
        return id;
    }

    public ArrayList<ArrayList<String>> get_random_two(String table) throws SQLException {
        ArrayList<ArrayList<String>> entries = new ArrayList<>();
        String q = "SELECT id, title, elo FROM " + table + " ORDER BY random() LIMIT 2";
        PreparedStatement stmt = c.prepareStatement(q);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
            String id = rs.getString(1);
            String title = rs.getString(2);
            String elo = rs.getString(3);
            entries.add(new ArrayList<>(Arrays.asList(id, title, elo)));
        }
        return entries;
    }

    public ArrayList<ArrayList<String>> get_ranked_two(String table) throws SQLException {
        ArrayList<ArrayList<String>> entries = new ArrayList<>();
        String q = "SELECT id, title, elo FROM " + table + " ORDER BY elo OFFSET floor(random() * (SELECT COUNT(*) FROM " + table + ")) LIMIT 2";
        PreparedStatement stmt = c.prepareStatement(q);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
            String id = rs.getString(1);
            String title = rs.getString(2);
            String elo = rs.getString(3);
            entries.add(new ArrayList<>(Arrays.asList(id, title, elo)));
        }
        return entries;
    }

    public void see_ranks(String table, String what_ranks) throws SQLException{
        String q = "";
        if (what_ranks.equalsIgnoreCase("all")) {
            q = "SELECT title, elo FROM " + table + " ORDER BY elo DESC";
        } else if (what_ranks.equalsIgnoreCase("top_10")) {
            q = "SELECT title, elo FROM " + table + " ORDER BY elo DESC LIMIT 10";
        } else {
            q = "SELECT title, elo FROM " + table + " ORDER BY elo LIMIT 10";
        }
        PreparedStatement stmt = c.prepareStatement(q);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            System.out.println("Title: " + rs.getString("title") + ", Elo: " + rs.getString("elo"));
        }
    }

    public void create_table(String table_name) throws SQLException {
        String q = "CREATE TABLE " + table_name + " (\n" +
                "    id SERIAL PRIMARY KEY NOT NULL,\n" +
                "    title VARCHAR(255) NOT NULL,\n" +
                "    year INTEGER NOT NULL,\n" +
                "    genre VARCHAR(255)[],\n" +
                "    synopsis TEXT,\n" +
                "    notable_actors VARCHAR(255)[],\n" +
                "    elo INTEGER NOT NULL\n" +
                ")";
        PreparedStatement stmt = c.prepareStatement(q);
        stmt.execute();
    }

    public static void main(String[] args) throws SQLException {
        String db_url = "jdbc:postgresql://localhost:5432/test";
        String user = "postgres";
        String pass = "Sx@040802";
        PostgreSQLJDBC jdbc = new PostgreSQLJDBC(db_url, user, pass);

        jdbc.connect();

        System.out.println(jdbc.get_random_two("english_movies").get(0));
    }
}
