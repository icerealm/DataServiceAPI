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
    fun findByIdAndFlg(id: String, enableFlg: Boolean): Mono<Product>
    fun findByProductType(productType: String?): Flux<Product>
    fun findByName(name: String?): Flux<Product>
    fun findByNameAndType(name: String?, type: String?): Flux<Product>
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

    override fun findByProductType(productType: String?): Flux<Product> =
            mongoOpr.find(Query.query(Criteria.where("type").`is`(productType)), Product::class.java)

    override fun save(product: Product): Mono<Product> {
        if (!product.name.isNullOrEmpty()) {
            product.revisedDate = LocalDateTime.now()
            return mongoOpr.save(product)
        }
        else {
            return Mono.empty()
        }
    }

    override fun findById(id: String): Mono<Product> = mongoOpr.findById(id, Product::class.java)

    override fun findByIdAndFlg(id: String, enableFlg: Boolean): Mono<Product> {
        val result = mongoOpr.find(Query.query(Criteria.where("id").`is`(id)
                                    .and("enableFlag").`is`(enableFlg)), Product::class.java)
        return result.next();
    }

    override fun findByName(name: String?): Flux<Product> {
        return mongoOpr.find(Query.query(Criteria.where("name").`is`(name)), Product::class.java)
    }

    override fun findByNameAndType(name: String?, type: String?): Flux<Product> {
        return mongoOpr.find(Query.query(Criteria.where("name").`is`(name)
                                    .and("type").`is`(type)), Product::class.java)
    }

    override fun update(existingProduct: Product, productTobeUpdated: Product): Mono<Product> {
        existingProduct.apply {
            productTobeUpdated.name?.let { name = productTobeUpdated.name }
            productTobeUpdated.description?.let{ description = productTobeUpdated.description }
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