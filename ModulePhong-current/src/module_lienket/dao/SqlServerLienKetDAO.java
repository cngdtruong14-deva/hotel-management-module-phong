package module_lienket.dao;

import common.ConnectionFactory;
import module_phong.dao.*;
import module_lienket.model.DuLieuLienKet;
import module_khachhang.model.KhachHang;
import module_dichvu.model.*;
import module_datphong.model.*;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

public class SqlServerLienKetDAO implements LienKetDAO {
    private final ConnectionFactory connections;
    private final SqlServerPhongDAO phongDAO;

    public SqlServerLienKetDAO(ConnectionFactory connections, SqlServerPhongDAO phongDAO) {
        this.connections = connections;
        this.phongDAO = phongDAO;
    }

    public <T> T giaoDich(Function<Session, T> work) {
        try (Connection c = connections.open()) {
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                T result = work.apply(new JdbcSession(c));
                c.commit();
                return result;
            } catch (RuntimeException | SQLException e) {
                try {
                    c.rollback();
                } catch (SQLException suppressed) {
                    e.addSuppressed(suppressed);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw error(e);
        }
    }

    private static RuntimeException error(SQLException e) {
        if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627)
            return new IllegalArgumentException(
                    "Mã trùng hoặc phòng đã có lượt đặt/lưu trú đang hoạt động.", e);
        if (e.getErrorCode() == 547)
            return new IllegalArgumentException(
                    "Khóa liên kết không hợp lệ hoặc dữ liệu vi phạm ràng buộc.", e);
        return new IllegalStateException(
                "Không truy cập được bảng liên kết. Kiểm tra SQL 01_bang_chinh.sql và cấu hình. SQLState="
                        + e.getSQLState(),
                e);
    }

    private class JdbcSession implements Session {
        final Connection c;

        JdbcSession(Connection c) {
            this.c = c;
        }

        public PhongDAO.Session phongSession() {
            return phongDAO.session(c);
        }

        interface Mapper<T> {
            T map(ResultSet r) throws SQLException;
        }

        <T> List<T> query(String sql, Mapper<T> mapper) {
            try (PreparedStatement st = c.prepareStatement(sql)) {
                st.setQueryTimeout(15);
                try (ResultSet r = st.executeQuery()) {
                    List<T> list = new ArrayList<>();
                    while (r.next()) list.add(mapper.map(r));
                    return list;
                }
            } catch (SQLException e) {
                throw error(e);
            }
        }

        void write(String sql, Object... args) {
            try (PreparedStatement st = c.prepareStatement(sql)) {
                st.setQueryTimeout(15);
                for (int i = 0; i < args.length; i++) st.setObject(i + 1, args[i]);
                if (st.executeUpdate() != 1)
                    throw new IllegalArgumentException("Mã không tồn tại hoặc đã thay đổi.");
            } catch (SQLException e) {
                throw error(e);
            }
        }

        java.time.LocalDateTime time(ResultSet r, String field) throws SQLException {
            Timestamp t = r.getTimestamp(field);
            return t == null ? null : t.toLocalDateTime();
        }

        Timestamp ts(java.time.LocalDateTime t) {
            return t == null ? null : Timestamp.valueOf(t);
        }

        public DuLieuLienKet doc() {
            // Luon khoa khach hang truoc phong khi xu ly giao dich lien ket.
            var kh =
                    query(
                            "SELECT * FROM dbo.KhachHang WITH (UPDLOCK,HOLDLOCK) ORDER BY MaKhachHang",
                            module_khachhang.dao.SqlServerKhachHangDAO::read);
            phongSession().phongs();
            var dv =
                    query(
                            "SELECT * FROM dbo.DichVu ORDER BY MaDichVu",
                            r ->
                                    new DichVu(
                                            r.getString("MaDichVu"),
                                            r.getString("TenDichVu"),
                                            r.getBigDecimal("DonGia"),
                                            r.getString("DonViTinh"),
                                            r.getBoolean("NgungSuDung")));
            var dp =
                    query(
                            "SELECT * FROM dbo.DatPhong WITH (UPDLOCK,HOLDLOCK) ORDER BY MaDatPhong",
                            r ->
                                    new DatPhong(
                                            r.getString("MaDatPhong"),
                                            r.getString("MaKhachHang"),
                                            r.getString("MaPhong"),
                                            r.getBigDecimal("GiaPhongChot"),
                                            time(r, "NgayDat"),
                                            time(r, "NgayNhan"),
                                            time(r, "NgayTra"),
                                            TrangThaiDatPhong.valueOf(r.getString("TrangThai"))));
            var sd =
                    query(
                            "SELECT * FROM dbo.SuDungDichVu ORDER BY ThoiGianSuDung",
                            r ->
                                    new SuDungDichVu(
                                            r.getString("MaSuDung"),
                                            r.getString("MaDatPhong"),
                                            r.getString("MaDichVu"),
                                            r.getInt("SoLuong"),
                                            r.getBigDecimal("DonGia"),
                                            time(r, "ThoiGianSuDung")));
            return new DuLieuLienKet(kh, dv, dp, sd);
        }

        public void luuDichVu(DichVu p, boolean sua) {
            write(
                    sua
                            ? "UPDATE dbo.DichVu SET TenDichVu=?,DonGia=?,DonViTinh=?,NgungSuDung=? WHERE MaDichVu=?"
                            : "INSERT dbo.DichVu(TenDichVu,DonGia,DonViTinh,NgungSuDung,MaDichVu) VALUES(?,?,?,?,?)",
                    p.tenDichVu(),
                    p.donGia(),
                    p.donViTinh(),
                    p.ngungSuDung(),
                    p.maDichVu());
        }

        public void luuDatPhong(DatPhong p, boolean sua) {
            write(
                    sua
                            ? "UPDATE dbo.DatPhong SET MaKhachHang=?,MaPhong=?,GiaPhongChot=?,NgayDat=?,NgayNhan=?,NgayTra=?,TrangThai=? WHERE MaDatPhong=?"
                            : "INSERT dbo.DatPhong(MaKhachHang,MaPhong,GiaPhongChot,NgayDat,NgayNhan,NgayTra,TrangThai,MaDatPhong) VALUES(?,?,?,?,?,?,?,?)",
                    p.maKhachHang(),
                    p.maPhong(),
                    p.giaPhongChot(),
                    ts(p.ngayDat()),
                    ts(p.ngayNhan()),
                    ts(p.ngayTra()),
                    p.trangThai().name(),
                    p.maDatPhong());
        }

        public void themSuDung(SuDungDichVu p) {
            write(
                    "INSERT dbo.SuDungDichVu(MaSuDung,MaDatPhong,MaDichVu,SoLuong,DonGia,ThoiGianSuDung) VALUES(?,?,?,?,?,?)",
                    p.maSuDung(),
                    p.maDatPhong(),
                    p.maDichVu(),
                    p.soLuong(),
                    p.donGia(),
                    ts(p.thoiGianSuDung()));
        }
    }
}
