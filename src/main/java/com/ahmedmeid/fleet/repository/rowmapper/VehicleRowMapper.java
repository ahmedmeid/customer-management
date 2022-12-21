package com.ahmedmeid.fleet.repository.rowmapper;

import com.ahmedmeid.fleet.domain.Vehicle;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Vehicle}, with proper type conversions.
 */
@Service
public class VehicleRowMapper implements BiFunction<Row, String, Vehicle> {

    private final ColumnConverter converter;

    public VehicleRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Vehicle} stored in the database.
     */
    @Override
    public Vehicle apply(Row row, String prefix) {
        Vehicle entity = new Vehicle();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setVehicleId(converter.fromRow(row, prefix + "_vehicle_id", String.class));
        entity.setVehicleRegNo(converter.fromRow(row, prefix + "_vehicle_reg_no", String.class));
        entity.setDeviceId(converter.fromRow(row, prefix + "_device_id", String.class));
        entity.setOwnerId(converter.fromRow(row, prefix + "_owner_id", Long.class));
        return entity;
    }
}
