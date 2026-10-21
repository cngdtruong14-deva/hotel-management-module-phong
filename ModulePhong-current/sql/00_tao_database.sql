-- Bước 1: Tạo database mới để học và test local.
-- Nếu đã tồn tại thì giữ nguyên, không xóa dữ liệu.
IF DB_ID(N'HotelManagementDB_LocalSimple') IS NULL
    CREATE DATABASE HotelManagementDB_LocalSimple;
GO
USE HotelManagementDB_LocalSimple;
GO
