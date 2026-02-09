package com.chiroerp.tenancy.core.infrastructure.health

import io.agroal.api.AgroalDataSource
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Liveness
import org.eclipse.microprofile.health.Readiness

@Readiness
@ApplicationScoped
class TenancyReadinessCheck(
    private val dataSource: AgroalDataSource,
) : HealthCheck {
    override fun call(): HealthCheckResponse {
        return try {
            dataSource.connection.use { connection ->
                if (connection.isValid(2)) {
                    HealthCheckResponse.up("tenancy-core-readiness")
                } else {
                    HealthCheckResponse.down("tenancy-core-readiness")
                }
            }
        } catch (ex: Exception) {
            HealthCheckResponse.named("tenancy-core-readiness")
                .down()
                .withData("error", ex.message ?: "unknown")
                .build()
        }
    }
}

@Liveness
@ApplicationScoped
class TenancyLivenessCheck : HealthCheck {
    override fun call(): HealthCheckResponse =
        HealthCheckResponse.up("tenancy-core-liveness")
}
