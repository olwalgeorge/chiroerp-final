package com.chiroerp.tenancy.core.application.service

import com.chiroerp.tenancy.shared.IsolationLevel

/**
 * Publishes provisioning telemetry to the observability layer (metrics, traces, etc).
 */
interface ProvisioningTelemetry {
    fun recordStep(stepName: String, status: ProvisioningStepStatus)

    fun recordOutcome(isolationLevel: IsolationLevel, readyForActivation: Boolean)
}
