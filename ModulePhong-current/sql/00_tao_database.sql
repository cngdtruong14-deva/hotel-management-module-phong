-- Buoc 1: Tao database moi de hoc va test local.
-- Neu da ton tai thi giu nguyen, khong xoa du lieu.
IF DB_ID(N'HotelManagementDB_LocalSimple') IS NULL
    CREATE DATABASE HotelManagementDB_LocalSimple;
GO
USE HotelManagementDB_LocalSimple;
GO
