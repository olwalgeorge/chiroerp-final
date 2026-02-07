package com.chiroerp.finance.ap.infrastructure.persistence

import com.chiroerp.finance.ap.application.service.PaymentRepository
import com.chiroerp.finance.ap.domain.model.Payment
import com.chiroerp.finance.shared.identifiers.PaymentId
import io.quarkus.hibernate.orm.panache.PanacheQuery
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class PaymentRepositoryImpl : PanacheRepositoryBase<PaymentEntity, UUID>, PaymentRepository {

    override fun save(payment: Payment): Payment {
        val entity = PaymentEntity.fromDomain(payment)
        persist(entity)
        return entity.toDomain()
    }

    override fun findById(tenantId: UUID, paymentId: PaymentId): Payment? {
        return list("id = ?1 and tenantId = ?2", paymentId.value, tenantId).firstOrNull()?.toDomain()
    }
}