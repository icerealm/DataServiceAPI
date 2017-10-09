package com.backend.data.api.handler

import com.backend.data.api.domain.*
import com.backend.data.api.repository.BinaryDataRepository
import com.backend.data.api.repository.ProductRepository
import com.fasterxml.jackson.databind.ObjectMapper

import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.FormFieldPart

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import java.io.File


/**
 * Created by jakkritsittiwerapong on 9/13/2017 AD.
 */
@Component
class ProductHandler(@Autowired private val productRepo: ProductRepository,
                     @Autowired private val binaryDataRepo: BinaryDataRepository) {

    @Value("\${file.dir}")
    private val dir: String? = null

    private fun convertToSystemFileName(part: FilePart): String {
        var tmpFile = File(part.filename())
        return tmpFile.nameWithoutExtension + "." +
                System.currentTimeMillis() + "." +
                tmpFile.extension
    }

    private fun createDestinationFile(product: Product, filename: String): File {
        val path = File("${dir}/${product.id}")
        path.mkdirs()
        var destFile = File("${dir}/${product.id}/${filename}")
        destFile.createNewFile()
        return destFile
    }

    fun addProduct(req: ServerRequest): Mono<ServerResponse> {
        var productToBeCreated = req.bodyToMono(ProductDTO::class.java)
        return productToBeCreated.flatMap{ dto -> ServerResponse.status(HttpStatus.CREATED)
                                                        .body(productRepo.save(Product.fromDto(dto))) }
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
                if (item.key == "name") {
                    product.name = data.value()
                }
                else if ( item.key == "description") {
                    product.description = data.value()
                }
                else if ( item.key == "type") {
                    product.type = mapper.readValue(data.value(), ProductTypeDTO::class.java)
                }
                else if ( item.key == "price") {
                    product.price = mapper.readValue(data.value(), Price::class.java)
                }
            }
            productRepo.findByName(product.name).collectList().flatMap { list ->
                if (list.isEmpty()){
                    ServerResponse.ok().body(
                        productRepo.save(product).flatMap { savedProduct ->
                            images.forEach { image ->
                                var part: FilePart = image.value as FilePart
                                var tmpFile = File(part.filename())
                                var destFile = createDestinationFile(product, convertToSystemFileName(part))
                                part.transferTo(destFile)
                                var binData = BinaryData()
                                binData.fileName = tmpFile.nameWithoutExtension
                                binData.fileType = tmpFile.extension
                                binData.path = destFile.path
                                binData.reference = ReferenceObject(savedProduct.id, Product::class.java.simpleName)
                                binaryDataRepo.save(binData).subscribe() //triggering to save
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
        val updatedProduct = req.bodyToMono(ProductDTO::class.java);
        return productRepo.findById(id).flatMap { existingProduct ->
            updatedProduct.flatMap { dto -> ServerResponse.status(HttpStatus.OK).body(
                    productRepo.update(existingProduct, Product.fromDto(dto)))
            }
        }.switchIfEmpty(notFound)
    }

    fun getProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id");
        return productRepo.findById(id).flatMap { product -> ServerResponse.ok().body(Mono.just(product.toDto())) }
                                       .switchIfEmpty(notFound)
    }

    fun getActiveProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id");
        return productRepo.findByIdAndFlg(id, true).flatMap { product ->
            ServerResponse.ok().body(Mono.just(product.toDto()))
        }.switchIfEmpty(notFound);
    }

    fun getAllProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        return productRepo.findAll().collectList().flatMap { products ->
            var dtoList = products.map { product -> product.toDto() }
            ServerResponse.ok().body(Mono.just(dtoList))
        }.switchIfEmpty(notFound)
    }

    fun getAllActiveProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        return productRepo.findAllProductWhereEnableFlg(true)
                          .collectList().flatMap { products ->
            var dtoList = products.map { product -> product.toDto() }
            ServerResponse.ok().body(Mono.just(dtoList))
        }.switchIfEmpty(notFound)
    }

    fun deleteProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id")
        return productRepo.findById(id).flatMap { product -> ServerResponse.status(HttpStatus.ACCEPTED)
                                                                            .body(productRepo.logicalDelete(product)) }
                                       .switchIfEmpty(notFound)
    }

    fun getImagesInProduct(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id")
        return binaryDataRepo.findByReferenceObjectId(id, Product::class.java.simpleName)
                                .collectList().flatMap { binariesData ->
            ServerResponse.ok().body(Mono.just(binariesData))
        }.switchIfEmpty(notFound)
    }
}