package com.chiroerp.identity.core.infrastructure.web

import jakarta.ws.rs.core.Application
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme

/**
 * OpenAPI security scheme definition for Identity API.
 * ADR-007: JWT bearer authentication strategy.
 */
@SecurityScheme(
    securitySchemeName = "jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT bearer token issued by identity-core",
)
class OpenApiConfig : Application()
