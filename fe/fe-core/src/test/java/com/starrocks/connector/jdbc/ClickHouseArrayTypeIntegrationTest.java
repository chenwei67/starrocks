// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.starrocks.connector.jdbc;

import com.starrocks.catalog.ArrayType;
import com.starrocks.catalog.PrimitiveType;
import com.starrocks.catalog.ScalarType;
import com.starrocks.catalog.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Types;

/**
 * Integration test for ClickHouse JDBC catalog array type support
 */
public class ClickHouseArrayTypeIntegrationTest {

    @Test
    public void testClickHouseArrayTypeMapping() {
        ClickhouseSchemaResolver resolver = new ClickhouseSchemaResolver(null);
        
        // Test Array(String) -> ARRAY<VARCHAR>
        Type arrayStringType = resolver.convertColumnType(Types.ARRAY, "Array(String)", 0, 0);
        Assertions.assertTrue(arrayStringType instanceof ArrayType);
        ArrayType arrayString = (ArrayType) arrayStringType;
        Assertions.assertTrue(arrayString.getItemType().isStringType());
        Assertions.assertEquals("ARRAY<VARCHAR(65533)>", arrayString.toSql());
        
        // Test Array(Int32) -> ARRAY<INT>
        Type arrayIntType = resolver.convertColumnType(Types.ARRAY, "Array(Int32)", 0, 0);
        Assertions.assertTrue(arrayIntType instanceof ArrayType);
        ArrayType arrayInt = (ArrayType) arrayIntType;
        Assertions.assertTrue(arrayInt.getItemType().isIntegerType());
        Assertions.assertEquals("ARRAY<INT>", arrayInt.toSql());
        
        // Test Array(Int64) -> ARRAY<BIGINT>
        Type arrayBigintType = resolver.convertColumnType(Types.ARRAY, "Array(Int64)", 0, 0);
        Assertions.assertTrue(arrayBigintType instanceof ArrayType);
        ArrayType arrayBigint = (ArrayType) arrayBigintType;
        Assertions.assertTrue(arrayBigint.getItemType().isBigintType());
        Assertions.assertEquals("ARRAY<BIGINT>", arrayBigint.toSql());
        
        // Test Array(Float32) -> ARRAY<FLOAT>
        Type arrayFloatType = resolver.convertColumnType(Types.ARRAY, "Array(Float32)", 0, 0);
        Assertions.assertTrue(arrayFloatType instanceof ArrayType);
        ArrayType arrayFloat = (ArrayType) arrayFloatType;
        Assertions.assertTrue(arrayFloat.getItemType().isFloatType());
        Assertions.assertEquals("ARRAY<FLOAT>", arrayFloat.toSql());
        
        // Test Array(Float64) -> ARRAY<DOUBLE>
        Type arrayDoubleType = resolver.convertColumnType(Types.ARRAY, "Array(Float64)", 0, 0);
        Assertions.assertTrue(arrayDoubleType instanceof ArrayType);
        ArrayType arrayDouble = (ArrayType) arrayDoubleType;
        Assertions.assertTrue(arrayDouble.getItemType().isDoubleType());
        Assertions.assertEquals("ARRAY<DOUBLE>", arrayDouble.toSql());
    }
    
    @Test
    public void testClickHouseNestedArrayTypes() {
        ClickhouseSchemaResolver resolver = new ClickhouseSchemaResolver(null);
        
        // Test Array(Nullable(String)) -> ARRAY<VARCHAR>
        Type arrayNullableStringType = resolver.convertColumnType(Types.ARRAY, "Array(Nullable(String))", 0, 0);
        Assertions.assertTrue(arrayNullableStringType instanceof ArrayType);
        ArrayType arrayNullableString = (ArrayType) arrayNullableStringType;
        Assertions.assertTrue(arrayNullableString.getItemType().isStringType());
        
        // Test Array(Nullable(Int32)) -> ARRAY<INT>
        Type arrayNullableIntType = resolver.convertColumnType(Types.ARRAY, "Array(Nullable(Int32))", 0, 0);
        Assertions.assertTrue(arrayNullableIntType instanceof ArrayType);
        ArrayType arrayNullableInt = (ArrayType) arrayNullableIntType;
        Assertions.assertTrue(arrayNullableInt.getItemType().isIntegerType());
    }
    
    @Test
    public void testClickHouseDecimalArrayTypes() {
        ClickhouseSchemaResolver resolver = new ClickhouseSchemaResolver(null);
        
        // Test Array(Decimal(10,2)) -> ARRAY<DECIMAL>
        Type arrayDecimalType = resolver.convertColumnType(Types.ARRAY, "Array(Decimal(10,2))", 0, 0);
        Assertions.assertTrue(arrayDecimalType instanceof ArrayType);
        ArrayType arrayDecimal = (ArrayType) arrayDecimalType;
        Assertions.assertTrue(arrayDecimal.getItemType().isDecimalV3());
        
        ScalarType decimalType = (ScalarType) arrayDecimal.getItemType();
        Assertions.assertEquals(10, decimalType.getPrecision());
        Assertions.assertEquals(2, decimalType.getScalarScale());
    }
    
    @Test
    public void testClickHouseUnsignedArrayTypes() {
        ClickhouseSchemaResolver resolver = new ClickhouseSchemaResolver(null);
        
        // Test Array(UInt8) -> ARRAY<SMALLINT>
        Type arrayUInt8Type = resolver.convertColumnType(Types.ARRAY, "Array(UInt8)", 0, 0);
        Assertions.assertTrue(arrayUInt8Type instanceof ArrayType);
        ArrayType arrayUInt8 = (ArrayType) arrayUInt8Type;
        Assertions.assertTrue(arrayUInt8.getItemType().isSmallintType());
        
        // Test Array(UInt16) -> ARRAY<INT>
        Type arrayUInt16Type = resolver.convertColumnType(Types.ARRAY, "Array(UInt16)", 0, 0);
        Assertions.assertTrue(arrayUInt16Type instanceof ArrayType);
        ArrayType arrayUInt16 = (ArrayType) arrayUInt16Type;
        Assertions.assertTrue(arrayUInt16.getItemType().isIntegerType());
        
        // Test Array(UInt32) -> ARRAY<BIGINT>
        Type arrayUInt32Type = resolver.convertColumnType(Types.ARRAY, "Array(UInt32)", 0, 0);
        Assertions.assertTrue(arrayUInt32Type instanceof ArrayType);
        ArrayType arrayUInt32 = (ArrayType) arrayUInt32Type;
        Assertions.assertTrue(arrayUInt32.getItemType().isBigintType());
        
        // Test Array(UInt64) -> ARRAY<LARGEINT>
        Type arrayUInt64Type = resolver.convertColumnType(Types.ARRAY, "Array(UInt64)", 0, 0);
        Assertions.assertTrue(arrayUInt64Type instanceof ArrayType);
        ArrayType arrayUInt64 = (ArrayType) arrayUInt64Type;
        Assertions.assertEquals(PrimitiveType.LARGEINT, arrayUInt64.getItemType().getPrimitiveType());
    }
    
    @Test
    public void testClickHouseArrayTypeErrorHandling() {
        ClickhouseSchemaResolver resolver = new ClickhouseSchemaResolver(null);
        
        // Test invalid array format
        Assertions.assertThrows(Exception.class, () -> {
            resolver.convertColumnType(Types.ARRAY, "InvalidArrayFormat", 0, 0);
        });
        
        // Test missing closing parenthesis
        Assertions.assertThrows(Exception.class, () -> {
            resolver.convertColumnType(Types.ARRAY, "Array(String", 0, 0);
        });
        
        // Test unsupported element type
        Assertions.assertThrows(Exception.class, () -> {
            resolver.convertColumnType(Types.ARRAY, "Array(UnsupportedType)", 0, 0);
        });
        
        // Test invalid decimal format
        Assertions.assertThrows(Exception.class, () -> {
            resolver.convertColumnType(Types.ARRAY, "Array(Decimal(invalid))", 0, 0);
        });
    }
    
    @Test
    public void testClickHouseBooleanAndDateArrayTypes() {
        ClickhouseSchemaResolver resolver = new ClickhouseSchemaResolver(null);
        
        // Test Array(Bool) -> ARRAY<BOOLEAN>
        Type arrayBoolType = resolver.convertColumnType(Types.ARRAY, "Array(Bool)", 0, 0);
        Assertions.assertTrue(arrayBoolType instanceof ArrayType);
        ArrayType arrayBool = (ArrayType) arrayBoolType;
        Assertions.assertTrue(arrayBool.getItemType().isBoolean());
        
        // Test Array(Date) -> ARRAY<DATE>
        Type arrayDateType = resolver.convertColumnType(Types.ARRAY, "Array(Date)", 0, 0);
        Assertions.assertTrue(arrayDateType instanceof ArrayType);
        ArrayType arrayDate = (ArrayType) arrayDateType;
        Assertions.assertTrue(arrayDate.getItemType().isDateType());
        
        // Test Array(DateTime) -> ARRAY<DATETIME>
        Type arrayDateTimeType = resolver.convertColumnType(Types.ARRAY, "Array(DateTime)", 0, 0);
        Assertions.assertTrue(arrayDateTimeType instanceof ArrayType);
        ArrayType arrayDateTime = (ArrayType) arrayDateTimeType;
        Assertions.assertTrue(arrayDateTime.getItemType().isDatetimeType());
    }
}