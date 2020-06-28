# TUID (Time-sequential Unique Identifier)

[![Jitpack](https://jitpack.io/v/toss/tuid.svg)](https://jitpack.io/#toss/tuid)
[![Build Status](https://travis-ci.org/toss/tuid.svg?branch=master)](https://travis-ci.org/toss/tuid)
[![codecov](https://codecov.io/gh/toss/tuid/branch/master/graph/badge.svg)](https://codecov.io/gh/toss/tuid)

## 목표
* 분산된 환경에서 대량으로 동시에 생성하여도 고유성을 가진다.
* 사전순으로 정렬 시 생성 시간으로 순서성을 가진다.
* 사람이 읽을 수 있도록 ASCII TEXT로 표현한다.
* ID의 값만 보고 개체의 유형을 이해 할 수 있다.

## Format
TUID는 62진수 ASCII([0-9A-Za-z])로 표현되며,
총 28개의 문자로된 6개의 그룹을 묶어서 표현한다

| name          | offset | length(bytes) | description                  |
|---------------|--------|---------------|------------------------------|
| epoch_seconds |      0 |             6 | seconds of epoch             |
| nanos         |      6 |             6 | nanoseconds of epoch_seconds |
| runtime       |     12 |             6 | fingerprint of runtime       |
| random        |     18 |             6 | random value                 |
| count         |     24 |             2 | sequential counter           |
| type_id       |     26 |             2 | type of tuid                 |

이를테면 다음과 같다. 

`1jpaGA0mOSnUQPa6j1yG39IW9KTX` => `1jpaGA 0mOSnU QPa6j1 yG39IW 9K TX` 

|         | epoch_seonds | nanos     | runtime     | random      | count | type_id |
|---------|--------------|-----------|-------------|-------------|-------|---------|
| value   | 1jpaGA       | 0mOSnU    | QPa6j1      |      yG39IW |    9K |      TX |
| decimal | 1593362066   | 715094700 | 24197467695 | -1595093560 |   578 |    1831 |


## Maintainers

* [Jinsung Oh](https://github.com/econquer)

## License

    Copyright 2020 Viva Republica, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
