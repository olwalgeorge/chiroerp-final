package com.chiroerp.identity.core.application.exception

class InvalidPasswordException(
    val violations: List<String>,
) : RuntimeException(
    "Password does not meet policy: ${violations.joinToString(separator = "; ")}".trim()
)
