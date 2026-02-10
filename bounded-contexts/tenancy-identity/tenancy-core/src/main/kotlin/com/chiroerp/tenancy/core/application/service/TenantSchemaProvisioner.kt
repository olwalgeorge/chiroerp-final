package com.chiroerp.tenancy.core.application.service

import com.chiroerp.tenancy.shared.TenantId

/**
 * Port for executing database-level provisioning actions for a tenant.
 *
 * Implementations perform DDL/DML operations such as creating schemas,
 * applying grants, and seeding baseline reference data.
 */
interface TenantSchemaProvisioner {
    fun verifySharedSchema()

    fun createSchema(schemaName: String)

    fun grantSchemaUsage(schemaName: String)

    fun createBootstrapObjects(schemaName: String)

    fun seedSchemaReferenceData(schemaName: String, tenantId: TenantId)

    fun dropSchema(schemaName: String)

    fun createDatabase(databaseName: String)

    fun grantDatabaseAccess(databaseName: String)

    fun runDatabaseMigrations(databaseName: String, tenantId: TenantId)

    fun dropDatabase(databaseName: String)
}
