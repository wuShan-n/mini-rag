package com.example.ragchatbot.typehandler;

import com.pgvector.PGvector;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 自定义 MyBatis TypeHandler，用于处理 PGvector 类型
 * 【最终健壮版本】
 */
@MappedTypes(PGvector.class)
public class PGvectorTypeHandler extends BaseTypeHandler<PGvector> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PGvector parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    private PGvector toPGvector(Object obj) throws SQLException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof PGvector) {
            return (PGvector) obj;
        }
        if (obj instanceof PGobject) {
            String value = ((PGobject) obj).getValue();
            if (value != null) {
                return new PGvector(value);
            }
        }
        return new PGvector(obj.toString());
    }

    @Override
    public PGvector getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object obj = rs.getObject(columnName);
        return toPGvector(obj);
    }

    @Override
    public PGvector getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        return toPGvector(obj);
    }

    @Override
    public PGvector getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object obj = cs.getObject(columnIndex);
        return toPGvector(obj);
    }
}
