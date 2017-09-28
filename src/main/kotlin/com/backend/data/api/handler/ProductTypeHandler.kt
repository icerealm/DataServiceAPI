package com.backend.data.api.handler

import com.backend.data.api.domain.ProductType
import com.backend.data.api.domain.ProductTypeDTO
import com.backend.data.api.repository.ProductRepository
import com.backend.data.api.repository.ProductTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class ProductTypeHandler(@Autowired private val productTypeRepository: ProductTypeRepository,
                         @Autowired private val productRepository: ProductRepository) {

    fun getProductType(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id");
        return productTypeRepository.findById(id).flatMap { productType ->
            ServerResponse.ok().body(Mono.just(productType.toDto()))
        }.switchIfEmpty(notFound);
    }

    fun getActiveProductType(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id");
        return productTypeRepository.findByIdAndFlg(id, true).flatMap { productType ->
            ServerResponse.ok().body(Mono.just(productType.toDto()))
        }.switchIfEmpty(notFound);
    }

    fun getAllActiveProductType(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        return productTypeRepository.findAllProductTypeWhereEnableFlg(true).collectList().flatMap {
            productTypes ->
            var dtoList = productTypes.map { aProductType -> aProductType.toDto() }
            ServerResponse.ok().body(Mono.just(dtoList))
        }.switchIfEmpty(notFound)
    }

    fun addProductType(req: ServerRequest): Mono<ServerResponse> {
        var productTypeTobeCreated = req.bodyToMono(ProductTypeDTO::class.java)
        return productTypeTobeCreated.flatMap { dto -> ServerResponse.status(HttpStatus.CREATED)
                                                                .body(productTypeRepository.save(ProductType.fromDto(dto))) }
    }
    fun updateProductType(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id");
        val productTypeMono = req.bodyToMono(ProductTypeDTO::class.java)

        return productTypeRepository.findById(id).flatMap { existingProductType ->
            productTypeMono.flatMap { dto ->
                ServerResponse.status(HttpStatus.OK).body(
                        productTypeRepository.update(existingProductType, ProductType.fromDto(dto)))
            }
        }.switchIfEmpty(notFound)
    }

    fun deleteProductType(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id");
        val logicalDelete = fun(productType: ProductType):Mono<ProductType> {
                return productRepository.findByProductType(productType.name)
                             .collectList().flatMap { list ->
                                                if (list.isEmpty())
                                                    productTypeRepository.logicalDelete(productType)
                                                else
                                                    Mono.just(productType)
                                                }
            }

        return productTypeRepository.findById(id).flatMap { existingProductType ->
                                                        ServerResponse.status(HttpStatus.ACCEPTED)
                                                                      .body(logicalDelete(existingProductType))
                                                            }.switchIfEmpty(notFound)
    }
}