package com.backend.data.api.repository

import com.backend.data.api.domain.BinaryData
import com.mongodb.client.result.DeleteResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * Created by jakkritsittiwerapong on 9/13/2017 AD.
 */
interface BinaryDataRepository {
    fun findAll(): Flux<BinaryData>
    fun findById(id: String): Mono<BinaryData>
    fun findByReferenceObjectId(productId: String, referenceObjType: String): Flux<BinaryData>
    fun save(binData: BinaryData): Mono<BinaryData>
    fun delete(binData: BinaryData): Mono<DeleteResult>
}

@Repository
class BinaryDataRepositoryImpl
    constructor(@Autowired val mongoOpr: ReactiveMongoOperations) : BinaryDataRepository {

    override fun findAll(): Flux<BinaryData> = mongoOpr.findAll(BinaryData::class.java);

    override fun save(binData: BinaryData): Mono<BinaryData> {
        binData.revisedDate = LocalDateTime.now()
        return mongoOpr.save(binData)
    }

    override fun findById(id: String): Mono<BinaryData> = mongoOpr.findById(id, BinaryData::class.java)

    override fun delete(binData: BinaryData): Mono<DeleteResult> =  mongoOpr.remove(binData)

    override fun findByReferenceObjectId(productId: String, referenceObjectType: String): Flux<BinaryData> {
        return mongoOpr.find(Query.query(Criteria.where("reference.referenceId").`is`(productId)
                                        .and("reference.referenceObjType").`is`(referenceObjectType)), BinaryData::class.java)
    }
}

