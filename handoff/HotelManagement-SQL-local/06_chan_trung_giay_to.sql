-- Database đã tạo bằng room-simple-1: chỉ chạy file này để bổ sung chống trùng.
-- Nếu dùng tên database khác, sửa USE trước khi chạy. Không sửa/xóa hồ sơ.
USE HotelManagementDB_LocalSimple;
GO
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;
    -- Khóa trong lúc kiểm tra và tạo index để tránh phát sinh bản ghi trùng mới.
    IF EXISTS (
        SELECT 1 FROM dbo.KhachHang WITH (TABLOCKX, HOLDLOCK)
        WHERE SoGiayTo IS NOT NULL
        GROUP BY LoaiGiayTo, QuocGiaCap, SoGiayTo
        HAVING COUNT(*) > 1
    )
    BEGIN
        SELECT MaKhachHang, HoTen, LoaiGiayTo, QuocGiaCap, SoGiayTo
        FROM (
            SELECT *, COUNT(*) OVER (PARTITION BY LoaiGiayTo, QuocGiaCap, SoGiayTo) AS SoBanTrung
            FROM dbo.KhachHang WHERE SoGiayTo IS NOT NULL
        ) AS Trung WHERE SoBanTrung > 1;
        THROW 51001, N'Có giấy tờ trùng. Chưa thay đổi database; đối chiếu hồ sơ trước khi chạy lại.', 1;
    END;
    -- Nếu index đã có, không tạo lại. File kiểm chứng bên dưới xác nhận cấu trúc.
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.KhachHang') AND name=N'UX_KhachHang_GiayTo')
    BEGIN
        CREATE UNIQUE INDEX UX_KhachHang_GiayTo
ON dbo.KhachHang(LoaiGiayTo, QuocGiaCap, SoGiayTo)
WHERE SoGiayTo IS NOT NULL;
    END;
    -- Tên trùng nhưng cấu trúc khác: dừng để tránh báo thành công sai.
    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes i
        WHERE i.object_id=OBJECT_ID(N'dbo.KhachHang') AND i.name=N'UX_KhachHang_GiayTo'
          AND i.is_unique=1 AND i.is_disabled=0 AND i.has_filter=1
          AND REPLACE(REPLACE(REPLACE(REPLACE(i.filter_definition,'[',''),']',''),'(',''),')','') = N'SoGiayTo IS NOT NULL'
          AND INDEX_COL(N'dbo.KhachHang',i.index_id,1)=N'LoaiGiayTo'
          AND INDEX_COL(N'dbo.KhachHang',i.index_id,2)=N'QuocGiaCap'
          AND INDEX_COL(N'dbo.KhachHang',i.index_id,3)=N'SoGiayTo'
          AND INDEX_COL(N'dbo.KhachHang',i.index_id,4) IS NULL
    ) THROW 51002, N'Index cùng tên có cấu trúc khác. Cần kiểm tra thủ công.', 1;
    COMMIT;
    PRINT N'Đã có chống trùng giấy tờ; dữ liệu khách hàng giữ nguyên.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO
