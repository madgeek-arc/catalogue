## [9.0.0](https://github.com/madgeek-arc/catalogue/compare/v8.0.2...v9.0.0) (2025-12-23)

### ⚠ BREAKING CHANGES

* error messages are returned as json
* renames "transactionId" to "traceId" and adds missing dependencies
* remove log filters for requests and transactions

### Features

* exception handler supports UnsupportedOperationException ([e6d70f9](https://github.com/madgeek-arc/catalogue/commit/e6d70f9d5e1705abdc36f9e175cb2425c8dc326d))
* sets 'value' as Object to enable accepting arrays but keep backward compatibility ([fa761be](https://github.com/madgeek-arc/catalogue/commit/fa761be18298431e36e57fbbcdcf8079d56a112f))
* updates registry core version and creates method returning results with highlights ([d7f1aba](https://github.com/madgeek-arc/catalogue/commit/d7f1aba5aa16a94f3afa51c280096c8fb0325701))
* validation also checks if sections are contained as fields in the data object ([5fa9820](https://github.com/madgeek-arc/catalogue/commit/5fa98202d3cf181de21ece5e9b8d6b538f032e71))

### Bug Fixes

* renames "transactionId" to "traceId" and adds missing dependencies ([3fa97d6](https://github.com/madgeek-arc/catalogue/commit/3fa97d64c885f669cf9ce82c27344b7990bee33b))

### Code Refactoring

* error messages are returned as json ([fdeb251](https://github.com/madgeek-arc/catalogue/commit/fdeb2510bcadefb9909ed4975704fccde2e7adfb))
* remove log filters for requests and transactions ([2b87a88](https://github.com/madgeek-arc/catalogue/commit/2b87a887d42a30b5bb88a9dc288c63fdf584f3b9))

## [8.0.2](https://github.com/madgeek-arc/catalogue/compare/v8.0.1...v8.0.2) (2025-07-09)

### Bug Fixes

* Make service @Primary to fix autowiring issues ([cb93285](https://github.com/madgeek-arc/catalogue/commit/cb93285258032a8846e5e73b16452f20a054b0b2))

## [8.0.1](https://github.com/madgeek-arc/catalogue/compare/v8.0.0...v8.0.1) (2025-05-22)

## [8.0.0](https://github.com/madgeek-arc/catalogue/compare/v7.0.0...v8.0.0) (2025-03-31)

### ⚠ BREAKING CHANGES

* move classes to different packages and remove never-used code

### Code Refactoring

* move classes to different packages and remove never-used code ([a28ef87](https://github.com/madgeek-arc/catalogue/commit/a28ef8732051bb3a72dce03053d4165e596e8b5a))

## [7.0.0](https://github.com/madgeek-arc/catalogue/compare/ea71a64e748000d02a038ceda2eb91fab6be2e79...v7.0.0) (2025-02-14)

### ⚠ BREAKING CHANGES

* change fully qualified package name

### Features

* add exception handler ([c16fcbe](https://github.com/madgeek-arc/catalogue/commit/c16fcbea1562b74f2f7e5446e2f5296f1002df31))

### Bug Fixes

* changed filter name from 'group' to 'form_group' ([ea71a64](https://github.com/madgeek-arc/catalogue/commit/ea71a64e748000d02a038ceda2eb91fab6be2e79))
* check field names of model for duplicates and throw error ([ac5009c](https://github.com/madgeek-arc/catalogue/commit/ac5009cfbe34461981f72c8237b2d448cf6d912e))
* enable compiling with -parameters to support reflection ([2b409f0](https://github.com/madgeek-arc/catalogue/commit/2b409f02f19421ad1f7e379541dcf332e60ac5a1))
* first inserted label was sharing memory with the first item inserted in aliasGroupLabel leading to inconsistent label fields ([8554cd6](https://github.com/madgeek-arc/catalogue/commit/8554cd6f3cbbf1d6f6411bc7093366326c46db58))
* handle ValidationException in GenericExceptionController ([012d46f](https://github.com/madgeek-arc/catalogue/commit/012d46fd9772985ded98172cf5885388e190895e))
* remove feature checking for duplicate names in Model ([ec3a754](https://github.com/madgeek-arc/catalogue/commit/ec3a754cede4abfcd16ba774288d2698ed4172ce))

### Reverts

* Revert "[maven-release-plugin] prepare release catalogue-2.0.0" ([33d131e](https://github.com/madgeek-arc/catalogue/commit/33d131eef87504405ce2eba49219db36960dc505))
* Revert "[maven-release-plugin] prepare release catalogue-2.0.0" ([089c539](https://github.com/madgeek-arc/catalogue/commit/089c539446c32d1b13b5ae5447a367384c22824a))

### Build System

* change fully qualified package name ([2576c52](https://github.com/madgeek-arc/catalogue/commit/2576c52fe895c2691bca9c2e59bd785529082aec))
