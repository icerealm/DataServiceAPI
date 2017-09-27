package com.backend.data.api.routes

import com.backend.data.api.handler.ProductHandler
import com.backend.data.api.handler.ProductTypeHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

/**
 * Created by jakkritsittiwerapong on 9/8/2017 AD.
 */

@Configuration
class AppRoutes{

    @Autowired lateinit var productHandler: ProductHandler
    @Autowired lateinit var productTypeHandler: ProductTypeHandler

    @Bean
    fun productApi() = router {
        (accept(MediaType.MULTIPART_FORM_DATA) and "/products").nest {
            POST("/", productHandler::addProductWithFiles)
        }
        (accept(MediaType.APPLICATION_JSON) and "/products").nest{
            GET("/", productHandler::getAllActiveProduct)
            GET("/{id}", productHandler::getProduct)
            PUT("/{id}", productHandler::updateProduct)
            DELETE("/{id}", productHandler::deleteProduct)
        }
    }

    @Bean
    fun productTypeApi() = router {
        (accept(MediaType.APPLICATION_JSON) and "/productTypes").nest {
            GET("/", productTypeHandler::getAllActiveProductType)
            POST("/", productTypeHandler::addProductType)
            GET("/{id}", productTypeHandler::getActiveProductType)
            PUT("/{id}", productTypeHandler::updateProductType)
            DELETE("/{id}", productTypeHandler::deleteProductType)
        }
    }
}