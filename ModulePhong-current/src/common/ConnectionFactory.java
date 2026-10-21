package common;
import java.sql.*;
@FunctionalInterface
public interface ConnectionFactory { Connection open() throws SQLException; }
