package com.backend.data.api.domain

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceConstructor
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Created by jakkritsittiwerapong on 9/10/2017 AD.
 */

const val BAHT:String = "baht"

@Document(collection="Stock")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Stock @PersistenceConstructor constructor(val id: String? = null, val name: String);

@Document(collection="BinaryData")
data class BinaryData @PersistenceConstructor
          constructor(@Id val id: String? = null,
                      var path: String? = null,
                      var fileName: String? = null,
                      var fileType: String? = null,
                      var revisedDate: LocalDateTime? = null,
                      var reference: ReferenceObject? = null);

@Document(collection = "ProductType")
data class ProductType @PersistenceConstructor
          constructor(@Id var id: String? = null,
                      var name: String? = null,
                      var description: String? = null,
                      var enableFlag: Boolean? = true,
                      var revisedDate: LocalDateTime? = null) {

    fun toDto():ProductTypeDTO = ProductTypeDTO(
            id = this.id,
            name = this.name,
            description = this.description
    )

    companion object {
        fun fromDto(dto: ProductTypeDTO) = ProductType(
                id = dto.id,
                name = dto.name,
                description = dto.description
        )
    }
}

@Document(collection="Product")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Product @PersistenceConstructor
        constructor(
        @Id var id: String? = null,
        var name: String? = "",
        var description: String? = null,
        var enableFlag: Boolean? = true,
        var revisedDate: LocalDateTime? = null,
        var type: ProductTypeDTO? = null,
        var price: Price? = Price(0, 0, BAHT)
    )
{
    fun toDto(): ProductDTO = ProductDTO(
            id = this.id,
            name = this.name,
            description = this.description,
            type = this.type,
            price = this.price
    )

    companion object {
        fun fromDto(dto: ProductDTO) = Product(
                id = dto.id,
                name = dto.name,
                description = dto.description,
                type = dto.type,
                price = dto.price
        )
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ReferenceObject constructor( var referenceId: String? = null,
                                        var referenceObjType: String? = null)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Price
        constructor(var major: Int? = null,
                    var minor: Int? = null,
                    var type: String? = null )
