# Master Data Hub Application Layer

> Part of [Master Data Hub](../mdm-hub.md)

## Directory Structure

```
hub-application/
`-- src/main/kotlin/com.erp.mdm.hub.application/
    |-- port/
    |   |-- input/
    |   |   |-- command/
    |   |   `-- query/
    |   `-- output/
    `-- service/
        |-- command/
        `-- query/
```

---

## Commands (Write Operations)

```
port/input/command/
|-- CreateMasterRecordCommand.kt
|   `-- domain, attributes
|
|-- SubmitChangeRequestCommand.kt
|   `-- masterId, changeType, changes
|
|-- ApproveChangeRequestCommand.kt
|   `-- changeRequestId, approverId
|
`-- RetireMasterRecordCommand.kt
    `-- masterId
```

---

## Queries (Read Operations)

```
port/input/query/
|-- GetMasterRecordQuery.kt
|   `-- masterId -> MasterRecordDto
|
|-- SearchMasterRecordsQuery.kt
|   `-- domain, filters -> List<MasterRecordDto>
|
`-- GetChangeRequestQuery.kt
    `-- changeRequestId -> ChangeRequestDto
```

---

## Output Ports (Driven Ports)

```
port/output/
|-- MasterRecordRepository.kt
|-- ChangeRequestRepository.kt
|-- StewardshipPort.kt
|-- DataQualityPort.kt
`-- EventPublisherPort.kt
```

---

## Command & Query Handlers

```
service/
|-- command/
|   |-- MasterRecordCommandHandler.kt
|   `-- ChangeRequestCommandHandler.kt
`-- query/
    |-- MasterRecordQueryHandler.kt
    `-- ChangeRequestQueryHandler.kt
```
