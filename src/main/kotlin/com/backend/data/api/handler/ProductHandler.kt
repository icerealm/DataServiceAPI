package com.backend.data.api.handler

import com.backend.data.api.domain.*
import com.backend.data.api.repository.BinaryDataRepository
import com.backend.data.api.repository.ProductRepository
import com.fasterxml.jackson.databind.ObjectMapper

import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.FormFieldPart
import org.springframework.http.codec.multipart.Part

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.File
import java.io.IOException


/**
 * Created by jakkritsittiwerapong on 9/13/2017 AD.
 */
@Component
class ProductHandler(@Autowired private val productRepo: ProductRepository,
                     @Autowired private val binaryDataRepo: BinaryDataRepository) {

    @Value("\${file.dir}")
    private val dir: String? = null

    fun addProduct(req: ServerRequest): Mono<ServerResponse> {
        var productToBeCreated = req.bodyToMono(Product::class.java)
        return productToBeCreated.flatMap{ product -> ServerResponse.status(HttpStatus.CREATED)
                                                        .body(productRepo.save(product)) }
    }

    fun addProductWithFiles(req: ServerRequest):Mono<ServerResponse> {
        return req.body(BodyExtractors.toMultipartData()).flatMap { parts ->
            val map = parts.toSingleValueMap()
            val images = map.entries.filter { item -> item.key.startsWith("image") }
            val fields = map.entries.filter { item -> !item.key.startsWith("image")}
            val mapper = ObjectMapper()
            var product = Product(null,"")
            fields.forEach { item ->
                var data = item.value as FormFieldPart
                if (item.key == "name")
                    product.name = data.value()
                else if ( item.key == "description")
                    product.description = data.value()
                else if ( item.key == "type")
                    product.type = data.value()
                else if ( item.key == "price")
                    product.price = mapper.readValue(data.value(), Price::class.java)
            }

            productRepo.findByNameAndType(product.name, product.type).collectList().flatMap { list ->
                if (list.isEmpty()){
                    ServerResponse.ok().body(
                            productRepo.save(product).flatMap { savedProduct ->
                                images.forEach { image ->
                                    try {
                                        var part: FilePart = image.value as FilePart
                                        var destFile = File(dir + "/" + part.filename())
                                        destFile.createNewFile()
                                        part.transferTo(destFile)
                                        var binData = BinaryData()
                                        binData.fileName = destFile.nameWithoutExtension
                                        binData.fileType = destFile.extension
                                        binData.path = destFile.path
                                        binData.reference = ReferenceObject(savedProduct.id, Product::class.java.simpleName)
                                        binaryDataRepo.save(binData).subscribe()
                                    }
                                    catch(e: Exception){
                                        e.printStackTrace()
                                    }
                                }
                                Mono.just(savedProduct)
                            }
                    )
                }
                else {
                    ServerResponse.status(HttpStatus.BAD_REQUEST).build()
                }
            }
        }

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

    fun getActiveProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id");
        return productRepo.findByIdAndFlg(id, true).flatMap { product ->
            ServerResponse.ok().body(Mono.just(product))
        }.switchIfEmpty(notFound);
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