# ClickHouse JDBC Catalog Array Type Support

This document describes the enhanced support for ClickHouse array types in StarRocks JDBC catalog.

## Overview

Starting with this enhancement, StarRocks JDBC catalog now supports ClickHouse array types, including:
- `Array(String)` -> `ARRAY<VARCHAR(65533)>`
- `Array(Int32)` -> `ARRAY<INT>`
- `Array(Int64)` -> `ARRAY<BIGINT>`
- `Array(Float32)` -> `ARRAY<FLOAT>`
- `Array(Float64)` -> `ARRAY<DOUBLE>`
- `Array(Bool)` -> `ARRAY<BOOLEAN>`
- `Array(Date)` -> `ARRAY<DATE>`
- `Array(DateTime)` -> `ARRAY<DATETIME>`
- `Array(Decimal(p,s))` -> `ARRAY<DECIMAL(p,s)>`
- Unsigned integer types (UInt8, UInt16, UInt32, UInt64)
- Nullable array elements (e.g., `Array(Nullable(String))`)

## Implementation Details

### Frontend (FE) Changes

1. **ClickhouseSchemaResolver Enhancement**
   - Added support for `Types.ARRAY` in `convertColumnType()` method
   - Implemented `convertClickHouseArrayType()` to parse ClickHouse array type names
   - Added `convertClickHouseElementType()` to handle various element types
   - Added `parseDecimalType()` for decimal array elements

2. **Type Mapping**
   - ClickHouse `Array(ElementType)` maps to StarRocks `ARRAY<ElementType>`
   - Proper handling of nullable elements
   - Support for unsigned integer types with appropriate upcasting

### JDBC Bridge Changes

1. **JDBCScanner Enhancement**
   - Added `convertArrayToJson()` method to handle `java.sql.Array` objects
   - Converts JDBC arrays to JSON string representation
   - Uses Jackson ObjectMapper for reliable JSON serialization
   - Proper resource management with `sqlArray.free()`

### Backend (BE) Integration

- Leverages existing ArrayColumn implementation
- Uses StarRocks' built-in JSON parsing capabilities
- Array data flows through existing `parse_json()` functionality

## Usage Example

```sql
-- Create ClickHouse JDBC catalog
CREATE EXTERNAL CATALOG clickhouse_catalog
PROPERTIES (
    "type" = "jdbc",
    "user" = "default",
    "password" = "",
    "jdbc_uri" = "jdbc:clickhouse://localhost:8123/default",
    "driver_url" = "file:///path/to/clickhouse-jdbc-0.4.6.jar",
    "driver_class" = "com.clickhouse.jdbc.ClickHouseDriver"
);

-- Query tables with array columns
SELECT * FROM clickhouse_catalog.default.table_with_arrays;

-- Use array functions
SELECT 
    array_length(string_array_col) as array_size,
    array_contains(int_array_col, 42) as contains_42
FROM clickhouse_catalog.default.table_with_arrays;
```

## Supported ClickHouse Array Types

| ClickHouse Type | StarRocks Type | Notes |
|-----------------|----------------|---------|
| Array(String) | ARRAY<VARCHAR(65533)> | |
| Array(Int8) | ARRAY<TINYINT> | |
| Array(Int16) | ARRAY<SMALLINT> | |
| Array(Int32) | ARRAY<INT> | |
| Array(Int64) | ARRAY<BIGINT> | |
| Array(UInt8) | ARRAY<SMALLINT> | Upcasted for safety |
| Array(UInt16) | ARRAY<INT> | Upcasted for safety |
| Array(UInt32) | ARRAY<BIGINT> | Upcasted for safety |
| Array(UInt64) | ARRAY<LARGEINT> | |
| Array(Float32) | ARRAY<FLOAT> | |
| Array(Float64) | ARRAY<DOUBLE> | |
| Array(Bool) | ARRAY<BOOLEAN> | |
| Array(Date) | ARRAY<DATE> | |
| Array(DateTime) | ARRAY<DATETIME> | |
| Array(Decimal(p,s)) | ARRAY<DECIMAL(p,s)> | |
| Array(Nullable(T)) | ARRAY<T> | Nullable elements supported |

## Testing

Comprehensive test coverage includes:
- Unit tests for type conversion logic
- Integration tests for end-to-end functionality
- Error handling tests for invalid array formats
- Tests for all supported element types

Run tests with:
```bash
# Run ClickHouse schema resolver tests
mvn test -Dtest=ClickhouseSchemaResolverTest

# Run array type integration tests
mvn test -Dtest=ClickHouseArrayTypeIntegrationTest
```

## Limitations

1. Nested arrays (e.g., `Array(Array(String))`) are not currently supported
2. Complex types like `Array(Tuple(...))` are not supported
3. Array size limits follow StarRocks' general array limitations

## Error Handling

The implementation includes robust error handling for:
- Invalid array type formats
- Unsupported element types
- Malformed decimal specifications
- JDBC array conversion failures

Errors are reported with descriptive messages to aid in troubleshooting.

## Performance Considerations

- Array data is converted to JSON format for transmission
- Large arrays may impact query performance
- Consider using appropriate filters to limit array data transfer
- Array functions are executed in StarRocks BE for optimal performance

## Future Enhancements

Potential future improvements:
- Support for nested arrays
- Support for tuple arrays
- Optimized binary array transmission
- Additional ClickHouse-specific array functions