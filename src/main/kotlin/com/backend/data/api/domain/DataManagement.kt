package com.backend.data.api.domain

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceConstructor
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Created by jakkritsittiwerapong on 9/10/2017 AD.
 */


@Document(collection="Stock")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Stock @PersistenceConstructor constructor(val id: String? = null, val name: String);

@Document(collection="BinaryData")
data class BinaryData @PersistenceConstructor
          constructor(@Id val id: String? = null,
                      var path: String,
                      var fileName: String,
                      var fileType: String);

@Document(collection = "ProductType")
data class ProductType @PersistenceConstructor
          constructor(@Id var id: String? = null,
                      var name: String,
                      var description: String? = null,
                      var enableFlag: Boolean? = null,
                      var revisedDate: LocalDateTime? = null)

@Document(collection="Product")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Product @PersistenceConstructor
        constructor(
        @Id var id: String? = null,
        var name: String,
        var description: String? = null,
        var binaryDataList: List<BinaryData>? = null,
        var enableFlag: Boolean? = true,
        var revisedDate: LocalDateTime? = null,
        var type: String? = null
);

