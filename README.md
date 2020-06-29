# TUID (Time-sequential Unique Ientifier)

[![Jitpack](https://jitpack.io/v/toss/tuid.svg)](https://jitpack.io/#toss/tuid)
[![Build Status](https://travis-ci.org/toss/tuid.svg?branch=master)](https://travis-ci.org/toss/tuid)
[![codecov](https://codecov.io/gh/toss/tuid/branch/master/graph/badge.svg)](https://codecov.io/gh/toss/tuid)

## Purpose
* 분산된 환경에서 대량으로 동시에 생성하여도 고유성을 가진다.
* 사전순으로 정렬 시 생성 시간으로 순서성을 가진다.
* 사람이 읽을 수 있도록 ASCII TEXT로 표현한다.
* ID의 값만 보고 개체의 유형을 이해 할 수 있다.

## Usage
[Add dependency](https://jitpack.io/#toss/tuid/v0.0.1) to project and follow this example

```kotlin
// generate a new tuid string
val idValue = tuid()

// generate a new tuid object
val id = TUID()

// generate a tuid object from identifier value
val id = TUID("Uzzzzz15ftgFAbwd5I0hIej11STX") 

id.type // get type of identifier
id.datetime // get datetime of identifier
id.fingerprint // get fingerprint of generator
id.sequence // get sequence in generator
id.random // get random value
```

## Format
TUID는 62진수 ASCII([0-9A-Za-z])로 표현되며,
총 28개의 문자로된 6개의 그룹을 묶어서 표현한다

| name          | offset | length(bytes) | description                  |
|---------------|--------|---------------|------------------------------|
| epoch_seconds |      0 |             6 | seconds since epoch          |
| nanos         |      6 |             6 | nanoseconds since timestamp  |
| fingerprint   |     12 |             6 | fingerprint of generator     |
| random        |     18 |             5 | random value                 |
| sequence      |     23 |             2 | sequential value             |
| type          |     25 |             3 | type of identifier           |

이를테면 다음과 같다. 

`Uzzzzz15ftgFAbwd5I0hIej11STX` => `Uzzzzz 15ftgF Abwd5I 0hIej 11 STX` 

|         | epoch_seconds | nanos     | fingerprint |  random  | sequence | type_id |
|---------|---------------|-----------|-------------|----------|----------|---------|
| value   | Uzzzzz        | 15ftgF    |  Abwd5I     |    0hIej |       11 |     STX |
| decimal | 28400117791   | 999999999 |  9722026020 | 10319821 |       63 |  109463 |

## Limits
### 표현 가능한 최대 시간
2869-12-18T01:36:31.999999999Z

### 같은 ID가 발급될 가능성
아래의 모든 조건이 일치한 경우, 같은 ID가 발급될 수 있다.
* 나노초 단위의 같은 시각에 
* id의 type이 같고 - 238328 case (62^3)
* 같은 fingerprint를 가지는 tuid generator에서 - 56800235584 case (62^6)
* 실행 순서가 같으며 (sequence) - 3844 case (62^2)
* 같은 random값이 생성 - 916132832 case (62^5)


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
