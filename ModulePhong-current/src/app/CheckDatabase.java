package app;

import common.DBConnection;
import common.SchemaContract;
import java.nio.file.Path;

/** Read-only connection check, never prints passwords or the full JDBC URL. */
public final class CheckDatabase {
    public static void main(String[] args) throws Exception {
        Path config = Path.of(args.length == 0 ? "config/db.properties" : args[0]);
        var db = new DBConnection(config);
        SchemaContract.verify(db);
        try (var c = db.open(); var s = c.createStatement();
             var rs = s.executeQuery("SELECT DB_NAME() AS DatabaseName, (SELECT COUNT(*) FROM dbo.Phong) AS Rooms, (SELECT COUNT(*) FROM dbo.KhachHang) AS Customers, (SELECT COUNT(*) FROM dbo.DatPhong) AS Bookings")) {
            rs.next();
            System.out.println("PASS JDBC / room-simple-1: database=" + rs.getString("DatabaseName")
                + ", rooms=" + rs.getInt("Rooms") + ", customers=" + rs.getInt("Customers")
                + ", bookings=" + rs.getInt("Bookings"));
        }
    }
}
