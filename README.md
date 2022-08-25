### Task outcome report using Gradle Enterprise API
Utility to generate reports based in the task outcome using the [Gradle Enterprise API](https://docs.gradle.com/enterprise/api-manual/)
It generates a report grouping by task outcome

#### Installation

After cloning the repository, execute:
```
./gradlew install
```

Test the binary with:
```
geapi/build/install/app/bin/geapi --help
```

#### Access Key and Permission
To access the Gradle Enterprise API you need to generate a new API token and the permission Access build data via the API:
1. Sign in to Gradle Enterprise.
2. Access "My settings" from the user menu in the top right-hand corner of the page.
3. Access "Access keys" from the left-hand menu.
4. Click "Generate" on the right-hand side and copy the generated access key.
5. Store the token in a file to be used later

To view the permissions assigned to you:
1. Sign in to Gradle Enterprise.
2. Access "My settings" from the user menu in the top right-hand corner of the page.
3. Access "Permissions" from the left-hand menu.
4. Check if you have the required permissions.

#### Parameters

| Name                  | Description                                 | Default   | Required | Example                                  |
|-----------------------|---------------------------------------------|-----------|----------|------------------------------------------|
| api-key               | File with the generated token               |           | Yes      | --api-key=TOKEN_FILE                     |
| url                   | Gradle Enteprise instance                   |           | Yes      | --url=https://ge.acme.dev                |
| max-builds            | Max builds to be processed                  | 10        | No       | --max-builds=50                          |
| project               | Root project in Gradle Enteprise            |           | No       | --project=acme                           |
| inlcude-failed-builds | Include failed builds in the report         | false     | No       | --include-failed-builds                  |
| task                  | Requested task                              |           | No       | --task=assemble                          |
| type                  | Type of task                                |           | No       | --type=org.gradle.api.tasks.testing.Test |
| tags                  | Tags                                        | CI, LOCAL | No       | --tags=CI=develop                        |
| range                 | Initial date for requesting builds          | week      | No       | --range=month                            |
| since-build-id        | Initial build scan id for requesting builds |           | No       | --since-build-id=xxxxxx                  |


#### Examples

##### Getting the report of one build
```
 geapi/build/install/app/bin/geapi --api-key=FILE_TOKEN \
        --url=https://ge.acme.dev \
        --max-builds=1
```

##### Report filtering by requested task and tag
```
 geapi/build/install/app/bin/geapi --api-key=FILE_TOKEN \
        --url=https://ge.acme.dev \
        --task=assembleDebug
        --tags=local
```

##### Report including failed builds
```
 geapi/build/install/app/bin/geapi --api-key=FILE_TOKEN \
        --url=https://ge.acme.dev \
        --task=assembleDebug \
        --include-failed-builds
```

##### Report filtering by type of task
```
 geapi/build/install/app/bin/geapi --api-key=FILE_TOKEN \
        --url=https://ge.acme.dev \
        --type=com.android.build.gradle.internal.tasks.DexMergingTask
```

##### Filtering since build id
```
 geapi/build/install/app/bin/geapi --api-key=FILE_TOKEN \
        --url=https://ge.acme.dev \
        --type=com.android.build.gradle.internal.tasks.DexMergingTask \
        --max-builds=50 \
        --task=assemble \
        --since-build-id=xxxxxx
```

#### Example Output
The execution will generate reports like:

```
┌───────────────────────────────────────────────┐
│             Gradle Enterprise API             │
├───────────────────────┬───────────────────────┤
│        Server         │https://ge.acme.dev    │
├───────────────────────┼───────────────────────┤
│      Report type      │         xxxx          │
├───────────────────────┼───────────────────────┤
│       MaxBuilds       │          10           │
├───────────────────────┼───────────────────────┤
│Including Failed builds│         false         │
├───────────────────────┼───────────────────────┤
│         Tags          │       ci, local       │
├───────────────────────┼───────────────────────┤
│Total builds processed │          10           │
├───────────────────────┼───────────────────────┤
│ Total build filtered  │           7           │
├───────────────────────┼───────────────────────┤
│       Duration        │      18 seconds       │
└───────────────────────┴───────────────────────┘
┌─────────────────────────────────────────────────────────┐
│  Executions by outcome                                  │
├────────────────────────────┬────────────────────────────┤
│  no-source                 │  633                       │
├────────────────────────────┼────────────────────────────┤
│  avoided_up_to_date        │  485                       │
├────────────────────────────┼────────────────────────────┤
│  lifecycle                 │  697                       │
├────────────────────────────┼────────────────────────────┤
│  skipped                   │  23                        │
├────────────────────────────┼────────────────────────────┤
│  executed_cacheable        │  5                         │
├────────────────────────────┼────────────────────────────┤
│  executed_not_cacheable    │  1240                      │
├────────────────────────────┼────────────────────────────┤
│  avoided_from_local_cache  │  1406                      │
├────────────────────────────┴────────────────────────────┤
│  Duration by outcome(ms)                                │
├────────────────────────────┬────────────────────────────┤
│  no-source                 │  2 seconds                 │
├────────────────────────────┼────────────────────────────┤
│  avoided_up_to_date        │  1 seconds                 │
├────────────────────────────┼────────────────────────────┤
│  lifecycle                 │  72 milliseconds           │
├────────────────────────────┼────────────────────────────┤
│  skipped                   │  1 milliseconds            │
├────────────────────────────┼────────────────────────────┤
│  executed_cacheable        │  3 seconds                 │
├────────────────────────────┼────────────────────────────┤
│  executed_not_cacheable    │  1 minutes and 37 seconds  │
├────────────────────────────┼────────────────────────────┤
│  avoided_from_local_cache  │  1 minutes and 21 seconds  │
├────────────────────────────┴────────────────────────────┤
│  Mean time by outcome(ms)                               │
├────────────────────────────┬────────────────────────────┤
│  no-source                 │  3 milliseconds            │
├────────────────────────────┼────────────────────────────┤
│  avoided_up_to_date        │  3 milliseconds            │
├────────────────────────────┼────────────────────────────┤
│  lifecycle                 │  0 milliseconds            │
├────────────────────────────┼────────────────────────────┤
│  skipped                   │  0 milliseconds            │
├────────────────────────────┼────────────────────────────┤
│  executed_cacheable        │  669 milliseconds          │
├────────────────────────────┼────────────────────────────┤
│  executed_not_cacheable    │  78 milliseconds           │
├────────────────────────────┼────────────────────────────┤
│  avoided_from_local_cache  │  57 milliseconds           │
└────────────────────────────┴────────────────────────────┘
```

