package com.chiroerp.identity.core.infrastructure.web

import jakarta.enterprise.context.RequestScoped

@RequestScoped
class RequestTraceContext {
    var correlationId: String? = null
    var errorId: String? = null
}
