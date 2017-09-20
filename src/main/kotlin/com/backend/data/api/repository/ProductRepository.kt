package com.backend.data.api.repository

import com.backend.data.api.domain.Product
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
interface ProductRepository {
    fun findAll(): Flux<Product>
    fun findAllProductWhereEnableFlg(flg: Boolean): Flux<Product>
    fun findById(id: String): Mono<Product>
    fun findByProductType(productType: String): Flux<Product>
    fun save(product: Product): Mono<Product>
    fun update(existingProduct: Product, product: Product): Mono<Product>
    fun logicalDelete(existingProduct: Product): Mono<Product>
}

@Repository
class ProductRepositoryImpl
    constructor(@Autowired val mongoOpr: ReactiveMongoOperations) : ProductRepository {

    override fun findAll(): Flux<Product> = mongoOpr.findAll(Product::class.java)

    override fun findAllProductWhereEnableFlg(flg: Boolean): Flux<Product> =
            mongoOpr.find(Query.query(Criteria.where("enableFlag").`is`(flg)), Product::class.java)

    override fun findByProductType(productType: String): Flux<Product> =
            mongoOpr.find(Query.query(Criteria.where("type").`is`(productType)), Product::class.java)

    override fun save(product: Product): Mono<Product> {
        product.revisedDate = LocalDateTime.now()
        return mongoOpr.save(product)
    }

    override fun findById(id: String): Mono<Product> = mongoOpr.findById(id, Product::class.java)

    override fun update(existingProduct: Product, productTobeUpdated: Product): Mono<Product> {
        existingProduct.apply {
            productTobeUpdated.name?.let { name = productTobeUpdated.name }
            productTobeUpdated.description?.let{ description = productTobeUpdated.description }
            productTobeUpdated.binaryDataList?.let{ binaryDataList = productTobeUpdated.binaryDataList }
            productTobeUpdated.enableFlag?.let{ enableFlag = productTobeUpdated.enableFlag }
            productTobeUpdated.type?.let{ type = productTobeUpdated.type }
            revisedDate = LocalDateTime.now()
        }
        return mongoOpr.save(existingProduct)
    }

    override fun logicalDelete(existingProduct: Product): Mono<Product> {
        existingProduct.enableFlag = false
        existingProduct.revisedDate = LocalDateTime.now()
        return mongoOpr.save(existingProduct)
    }
}