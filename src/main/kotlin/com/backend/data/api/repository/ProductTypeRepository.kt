package com.backend.data.api.repository

import com.backend.data.api.domain.ProductType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface ProductTypeRepository {
    fun findAll(): Flux<ProductType>
    fun findAllProductWhereEnableFlg(flg: Boolean): Flux<ProductType>
    fun findById(id: String): Mono<ProductType>
    fun save(productType: ProductType): Mono<ProductType>
    fun update(existingProductType: ProductType, productTypeTobeUpdated: ProductType): Mono<ProductType>
    fun logicalDelete(existingProductType: ProductType): Mono<ProductType>
}

@Repository
class ProductTypeRepositoryImpl
        constructor(@Autowired val mongoOpr: ReactiveMongoOperations): ProductTypeRepository {

    override fun findAll(): Flux<ProductType> = mongoOpr.findAll(ProductType::class.java)

    override fun findAllProductWhereEnableFlg(flg: Boolean): Flux<ProductType> =
            mongoOpr.find(Query.query(Criteria.where("enableFlag").`is`(flg)), ProductType::class.java)

    override fun findById(id: String): Mono<ProductType> = mongoOpr.findById(id, ProductType::class.java)

    override fun update(existingProductType: ProductType, productTypeTobeUpdated: ProductType): Mono<ProductType> {
        existingProductType.apply {
            productTypeTobeUpdated.name?.let{ name = productTypeTobeUpdated.name }
            productTypeTobeUpdated.description?.let { description = productTypeTobeUpdated.description }
            productTypeTobeUpdated.revisedDate = LocalDateTime.now()
        }
        return mongoOpr.save(existingProductType)
    }

    override fun logicalDelete(existingProductType: ProductType): Mono<ProductType> {
        existingProductType.enableFlag = false
        return mongoOpr.save(existingProductType)
    }

    override fun save(productType: ProductType): Mono<ProductType> {
        productType.revisedDate = LocalDateTime.now()
        return mongoOpr.save(productType)
    }
}
