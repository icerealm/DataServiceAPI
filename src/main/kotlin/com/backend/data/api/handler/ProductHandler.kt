package com.backend.data.api.handler

import com.backend.data.api.domain.Product
import com.backend.data.api.repository.ProductRepository
import com.mongodb.connection.Server
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

/**
 * Created by jakkritsittiwerapong on 9/13/2017 AD.
 */
@Component
class ProductHandler(@Autowired private val productRepo: ProductRepository) {


    fun addProduct(req: ServerRequest): Mono<ServerResponse> {
        var productToBeCreated = req.bodyToMono(Product::class.java)
        return productToBeCreated.flatMap{ product -> ServerResponse.status(HttpStatus.CREATED)
                                                        .body(productRepo.save(product)) }
    }

    fun updateProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id");
        val updatedProduct = req.bodyToMono(Product::class.java);
        return productRepo.findById(id).flatMap { existingProduct ->
            updatedProduct.flatMap { product -> ServerResponse.status(HttpStatus.OK).body(
                    productRepo.update(existingProduct, product))
            }
        }.switchIfEmpty(notFound)
    }

    fun getProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id");
        return productRepo.findById(id).flatMap { product -> ServerResponse.ok().body(Mono.just(product)) }
                                       .switchIfEmpty(notFound)
    }

    fun getAllProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        return productRepo.findAll().collectList().flatMap { products -> ServerResponse.ok().body(Mono.just(products)) }
                                                  .switchIfEmpty(notFound)
    }

    fun getAllActiveProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        return productRepo.findAllProductWhereEnableFlg(true)
                          .collectList().flatMap { products -> ServerResponse.ok().body(Mono.just(products)) }
                                        .switchIfEmpty(notFound)
    }

    fun deleteProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id");
        return productRepo.findById(id).flatMap { product -> ServerResponse.status(HttpStatus.ACCEPTED)
                                                                            .body(productRepo.logicalDelete(product)) }
                                       .switchIfEmpty(notFound)
    }
}