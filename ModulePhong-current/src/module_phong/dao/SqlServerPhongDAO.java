package module_phong.dao;

import common.ConnectionFactory;
import module_phong.model.*;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

public class SqlServerPhongDAO implements PhongDAO {
    private final ConnectionFactory connections;

    public SqlServerPhongDAO(ConnectionFactory connections) {
        this.connections = connections;
    }

    public <T> T giaoDich(Function<Session, T> work) {
        try (Connection c = connections.open()) {
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                T value = work.apply(session(c));
                c.commit();
                return value;
            } catch (RuntimeException | SQLException e) {
                try {
                    c.rollback();
                } catch (SQLException suppressed) {
                    e.addSuppressed(suppressed);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw fail(e);
        }
    }

    /** Ben goi quan ly giao dich; session nay khong tu commit hoac dong ket noi. */
    public Session session(Connection c) {
        return new JdbcSession(c);
    }

    private static RuntimeException fail(SQLException e) {
        if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627)
            return new IllegalArgumentException("Mã hoặc số phòng đã tồn tại.", e);
        if (e.getErrorCode() == 547)
            return new IllegalArgumentException(
                    "Dữ liệu đang được tham chiếu hoặc vi phạm ràng buộc.", e);
        return new IllegalStateException(
                "Không thể truy cập SQL Server. Kiểm tra cấu hình/kết nối. SQLState="
                        + e.getSQLState(),
                e);
    }

    private static class JdbcSession implements Session {
        final Connection c;

        JdbcSession(Connection c) {
            this.c = c;
        }

        interface Mapper<T> {
            T map(ResultSet r) throws SQLException;
        }

        <T> List<T> query(String sql, Mapper<T> mapper) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setQueryTimeout(15);
                try (ResultSet r = p.executeQuery()) {
                    List<T> list = new ArrayList<>();
                    while (r.next()) list.add(mapper.map(r));
                    return list;
                }
            } catch (SQLException e) {
                throw fail(e);
            }
        }

        void update(String sql, Object... values) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setQueryTimeout(15);
                for (int i = 0; i < values.length; i++) p.setObject(i + 1, values[i]);
                if (p.executeUpdate() != 1)
                    throw new IllegalArgumentException("Bản ghi đã thay đổi hoặc không tồn tại.");
            } catch (SQLException e) {
                throw fail(e);
            }
        }

        public List<Phong> phongs() {
            return query(
                    "SELECT * FROM dbo.Phong WITH (UPDLOCK,HOLDLOCK) ORDER BY MaPhong",
                    r ->
                            new Phong(
                                    r.getString("MaPhong"),
                                    r.getString("SoPhong"),
                                    r.getString("MaLoaiPhong"),
                                    r.getString("MaTang"),
                                    r.getBigDecimal("GiaPhong"),
                                    TrangThaiPhong.valueOf(r.getString("TrangThai")),
                                    r.getBoolean("NgungSuDung")));
        }

        public List<LoaiPhong> loais() {
            return query(
                    "SELECT * FROM dbo.LoaiPhong ORDER BY MaLoaiPhong",
                    r ->
                            new LoaiPhong(
                                    r.getString("MaLoaiPhong"),
                                    r.getString("TenLoai"),
                                    r.getInt("SoNguoiToiDa"),
                                    r.getBigDecimal("GiaTheoNgay")));
        }

        public List<Tang> tangs() {
            return query(
                    "SELECT * FROM dbo.Tang ORDER BY MaTang",
                    r -> new Tang(r.getString("MaTang"), r.getString("TenTang")));
        }

        public List<CongViecPhong> congViecs() {
            List<CongViecPhong> list =
                    query(
                            "SELECT MaVeSinh AS MaCongViec,MaPhong,NoiDung,NguoiPhuTrach,BatDau,KetThuc FROM dbo.VeSinh",
                            r -> job(r, "VESINH"));
            list.addAll(
                    query(
                            "SELECT MaBaoTri AS MaCongViec,MaPhong,NoiDung,NguoiPhuTrach,BatDau,KetThuc FROM dbo.BaoTri",
                            r -> job(r, "BAOTRI")));
            list.sort(Comparator.comparing(CongViecPhong::batDau).reversed());
            return list;
        }

        CongViecPhong job(ResultSet r, String loai) throws SQLException {
            Timestamp end = r.getTimestamp("KetThuc");
            return new CongViecPhong(
                    r.getString("MaCongViec"),
                    loai,
                    r.getString("MaPhong"),
                    r.getString("NoiDung"),
                    r.getString("NguoiPhuTrach"),
                    r.getTimestamp("BatDau").toLocalDateTime(),
                    end == null ? null : end.toLocalDateTime());
        }

        public void luuPhong(Phong p, boolean sua) {
            if (sua)
                update(
                        "UPDATE dbo.Phong SET SoPhong=?,MaLoaiPhong=?,MaTang=?,GiaPhong=?,TrangThai=?,NgungSuDung=? WHERE MaPhong=?",
                        p.soPhong(),
                        p.maLoaiPhong(),
                        p.maTang(),
                        p.giaPhong(),
                        p.trangThai().name(),
                        p.ngungSuDung(),
                        p.maPhong());
            else
                update(
                        "INSERT dbo.Phong(SoPhong,MaLoaiPhong,MaTang,GiaPhong,TrangThai,NgungSuDung,MaPhong) VALUES(?,?,?,?,?,?,?)",
                        p.soPhong(),
                        p.maLoaiPhong(),
                        p.maTang(),
                        p.giaPhong(),
                        p.trangThai().name(),
                        p.ngungSuDung(),
                        p.maPhong());
        }

        public void luuLoai(LoaiPhong p, boolean sua) {
            update(
                    sua
                            ? "UPDATE dbo.LoaiPhong SET TenLoai=?,SoNguoiToiDa=?,GiaTheoNgay=? WHERE MaLoaiPhong=?"
                            : "INSERT dbo.LoaiPhong(TenLoai,SoNguoiToiDa,GiaTheoNgay,MaLoaiPhong) VALUES(?,?,?,?)",
                    p.tenLoai(),
                    p.soNguoiToiDa(),
                    p.giaTheoNgay(),
                    p.maLoaiPhong());
        }

        public void luuTang(Tang p, boolean sua) {
            update(
                    sua
                            ? "UPDATE dbo.Tang SET TenTang=? WHERE MaTang=?"
                            : "INSERT dbo.Tang(TenTang,MaTang) VALUES(?,?)",
                    p.tenTang(),
                    p.maTang());
        }

        public void xoaLoai(String ma) {
            update("DELETE dbo.LoaiPhong WHERE MaLoaiPhong=?", ma);
        }

        public void xoaTang(String ma) {
            update("DELETE dbo.Tang WHERE MaTang=?", ma);
        }

        String table(CongViecPhong p) {
            if (p.loai().equals("VESINH")) return "VeSinh";
            if (p.loai().equals("BAOTRI")) return "BaoTri";
            throw new IllegalArgumentException("Sai loại công việc");
        }

        public void themCongViec(CongViecPhong p) {
            String t = table(p);
            update(
                    "INSERT dbo."
                            + t
                            + "(Ma"
                            + t
                            + ",MaPhong,NoiDung,NguoiPhuTrach,BatDau) VALUES(?,?,?,?,?)",
                    p.maCongViec(),
                    p.maPhong(),
                    p.noiDung(),
                    p.nguoiPhuTrach(),
                    Timestamp.valueOf(p.batDau()));
        }

        public void ketThucCongViec(CongViecPhong p) {
            String t = table(p);
            update(
                    "UPDATE dbo." + t + " SET KetThuc=? WHERE Ma" + t + "=? AND KetThuc IS NULL",
                    Timestamp.valueOf(p.ketThuc()),
                    p.maCongViec());
        }
    }
}
