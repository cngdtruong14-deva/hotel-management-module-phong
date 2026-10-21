package module_khachhang.dao;

import common.ConnectionFactory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import module_khachhang.model.*;

/** DAO chỉ truy cập bảng KhachHang. Mọi giá trị người dùng dùng tham số SQL. */
public final class SqlServerKhachHangDAO implements KhachHangDAO {
    private final ConnectionFactory connections;
    private static final String FIELDS = "MaKhachHang,HoTen,LoaiGiayTo,SoGiayTo,QuocGiaCap,"
            + "SoDienThoai,Email,NgaySinh,GioiTinh,QuocTich,DiaChi,NgayTao,TrangThai";
    public SqlServerKhachHangDAO(ConnectionFactory connections) {
        this.connections = Objects.requireNonNull(connections);
    }
    private static RuntimeException failure(SQLException e) {
        if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627)
            return new IllegalArgumentException("Mã hoặc giấy tờ đã tồn tại. Hãy tìm hồ sơ cũ để sử dụng.", e);
        return new IllegalStateException("Không thao tác được SQL Server. Kiểm tra kết nối và script KhachHang.", e);
    }
    @Override public List<KhachHang> danhSach() {
        try (var c = connections.open(); var s = c.prepareStatement(
                "SELECT " + FIELDS + " FROM dbo.KhachHang ORDER BY MaKhachHang"); var rs = s.executeQuery()) {
            List<KhachHang> list = new ArrayList<>();
            while (rs.next()) list.add(read(rs));
            return List.copyOf(list);
        } catch (SQLException e) { throw failure(e); }
    }
    @Override public KhachHang get(String ma) {
        try (var c = connections.open()) { return find(c, ma, false); }
        catch (SQLException e) { throw failure(e); }
    }
    public static KhachHang docChoDatPhong(Connection c, String ma) throws SQLException {
        return find(c, ma, true);
    }
    private static KhachHang find(Connection c, String ma, boolean lock) throws SQLException {
        try (var s = c.prepareStatement("SELECT " + FIELDS + " FROM dbo.KhachHang"
                + (lock ? " WITH (UPDLOCK,HOLDLOCK)" : "") + " WHERE MaKhachHang=?")) {
            s.setString(1, ma);
            try (var rs = s.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy khách hàng: " + ma);
                return read(rs);
            }
        }
    }
    @Override public KhachHang them(DuLieuKhachHang d, LocalDateTime tao) {
        try (var c = connections.open()) {
            c.setAutoCommit(false);
            try {
                long sequence;
                try (var s = c.prepareStatement("SELECT NEXT VALUE FOR dbo.SeqKhachHang"); var rs = s.executeQuery()) {
                    rs.next(); sequence = rs.getLong(1);
                }
                String ma = String.format(Locale.ROOT, "KH%08d", sequence);
                try (var s = c.prepareStatement("INSERT INTO dbo.KhachHang (" + FIELDS
                        + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                    s.setString(1, ma);
                    bind(s, d, 2);
                    s.setTimestamp(12, Timestamp.valueOf(tao));
                    s.setString(13, TrangThaiKhachHang.HOAT_DONG.name());
                    s.executeUpdate();
                }
                c.commit();
                return d.thanhHoSo(ma, tao, TrangThaiKhachHang.HOAT_DONG);
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        } catch (SQLException e) { throw failure(e); }
    }
    @Override public void sua(String ma, DuLieuKhachHang d) {
        String sql = "UPDATE dbo.KhachHang SET HoTen=?,LoaiGiayTo=?,SoGiayTo=?,QuocGiaCap=?,"
                + "SoDienThoai=?,Email=?,NgaySinh=?,GioiTinh=?,QuocTich=?,DiaChi=? WHERE MaKhachHang=?";
        try (var c = connections.open(); var s = c.prepareStatement(sql)) {
            bind(s, d, 1); s.setString(11, ma); checkUpdated(s.executeUpdate(), ma);
        } catch (SQLException e) { throw failure(e); }
    }
    @Override public void doiTrangThai(String ma, TrangThaiKhachHang status) {
        try (var c = connections.open(); var s = c.prepareStatement(
                "UPDATE dbo.KhachHang SET TrangThai=? WHERE MaKhachHang=?")) {
            s.setString(1, status.name()); s.setString(2, ma); checkUpdated(s.executeUpdate(), ma);
        } catch (SQLException e) { throw failure(e); }
    }
    private static void checkUpdated(int count, String ma) {
        if (count != 1) throw new IllegalArgumentException("Không tìm thấy khách hàng: " + ma);
    }
    private static void bind(PreparedStatement s, DuLieuKhachHang d, int offset) throws SQLException {
        s.setNString(offset++, d.hoTen());
        s.setString(offset++, d.loaiGiayTo() == null ? null : d.loaiGiayTo().name());
        s.setString(offset++, d.soGiayTo()); s.setString(offset++, d.quocGiaCap());
        s.setString(offset++, d.soDienThoai()); s.setNString(offset++, d.email());
        s.setObject(offset++, d.ngaySinh(), Types.DATE);
        s.setString(offset++, d.gioiTinh().name()); s.setString(offset++, d.quocTich());
        s.setNString(offset, d.diaChi());
    }
    public static KhachHang read(ResultSet rs) throws SQLException {
        String type = rs.getString("LoaiGiayTo");
        java.sql.Date birth = rs.getDate("NgaySinh");
        return new KhachHang(rs.getString("MaKhachHang"), rs.getNString("HoTen"),
                type == null ? null : LoaiGiayTo.valueOf(type), rs.getString("SoGiayTo"),
                rs.getString("QuocGiaCap"), rs.getString("SoDienThoai"), rs.getNString("Email"),
                birth == null ? null : birth.toLocalDate(), GioiTinh.valueOf(rs.getString("GioiTinh")),
                rs.getString("QuocTich"), rs.getNString("DiaChi"),
                rs.getTimestamp("NgayTao").toLocalDateTime(),
                TrangThaiKhachHang.valueOf(rs.getString("TrangThai")));
    }
}
