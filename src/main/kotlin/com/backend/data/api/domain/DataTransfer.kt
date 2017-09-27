package com.backend.data.api.domain

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductDTO constructor(
        var name: String,
        var description: String? = null,
        var images: List<MultipartFile>? = null,
        var type: String? = null,
        var price: Price? = Price(0, 0, BAHT)
)