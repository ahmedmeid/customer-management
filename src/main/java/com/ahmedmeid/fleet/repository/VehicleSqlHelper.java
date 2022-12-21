package com.ahmedmeid.fleet.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class VehicleSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("vehicle_id", table, columnPrefix + "_vehicle_id"));
        columns.add(Column.aliased("vehicle_reg_no", table, columnPrefix + "_vehicle_reg_no"));
        columns.add(Column.aliased("device_id", table, columnPrefix + "_device_id"));

        columns.add(Column.aliased("owner_id", table, columnPrefix + "_owner_id"));
        return columns;
    }
}
