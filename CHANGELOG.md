# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Generic mapper interface support: implementation class now declares type parameters from the mapper interface
- Interface inheritance support: methods from parent interfaces are now processed with type parameter substitution
- `DefaultImplClassPackageGenerator` supports default package (empty package name) - generates implementation in `impl` package

### Changed
- **BREAKING**: `@MappingTarget` annotation can now only be applied to method parameters (removed `ElementType.METHOD` from `@Target`). Method-level usage was previously silently ignored.
- `copyProperties`: source/target array-iterable mismatch now reports ERROR instead of WARNING
- `converter.format()` / `elementConverter.format()`: `%` characters no longer cause `IllegalFormatException` (internal concatenation changed from `String.format` to `replace`)

### Fixed
- Counter variable name collision in generated array/iterable conversion loops (`int i` now uses unique names)
- Typo fixes: `PUBLIC_FIEld` → `PUBLIC_FIELD`, `propertyNam` → `propertyName`, `Cant' find` → `Can't find`
- Error message consistency: PropertyMapping validation messages now start with uppercase "The method..."
- Corrected error message for methods with return type: "with return type must have at least 1 parameter"
- TYPEVAR element type error messages now provide actionable guidance

### Security
- No security changes

## [1.0.0] - 2025-xx-xx
- Initial release of bean-mapper annotation processor
- Basic bean-to-bean mapping with `@PropertyMapping`
- Collection/array conversion support
- Map conversion support
- Custom converter support via `@Converter` / `@ElementConverter`
- Spring component model support
- Jakarta CDI component model support