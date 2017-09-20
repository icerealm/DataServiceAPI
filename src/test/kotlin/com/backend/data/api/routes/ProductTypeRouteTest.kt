package com.backend.data.api.routes

import com.backend.data.api.domain.ProductType
import com.backend.data.api.handler.ProductTypeHandler
import com.backend.data.api.repository.ProductRepository
import com.backend.data.api.repository.ProductTypeRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
class ProductTypeRouteTest : IntegrateTest() {

    @Mock
    lateinit var productRepo: ProductRepository
    @Mock
    lateinit var productTypeRepo: ProductTypeRepository
    lateinit var productTypeHandler: ProductTypeHandler
    lateinit var sut: AppRoutes //system under test
    lateinit var webClient: WebTestClient

    @Before
    fun setup() {
        productTypeHandler = ProductTypeHandler(productTypeRepo, productRepo)
        sut = AppRoutes()
        sut.productTypeHandler = productTypeHandler
        webClient = WebTestClient.bindToRouterFunction(sut.productTypeApi()).configureClient()
                .baseUrl("http://localhost/productTypes")
                .build();
    }

    @Test
    fun `should return 404 eror when delete with no id`() {
        //Given
        val id = "001"
        `when`(productTypeRepo.findById(id)).thenReturn(Mono.empty())

        //When
        val res = webClient.delete().uri("/${id}").accept(MediaType.APPLICATION_JSON).exchange()

        res.expectStatus().is4xxClientError
    }

    @Test
    fun `should logical delete success when id exist and no product under the type`() {
        //Given
        val id = "001"
        val name = "type1"
        val type = ProductType(id, name, "desc")
        val expect = ProductType(id, name, "desc", false)
        `when`(productTypeRepo.findById(id)).thenReturn(Mono.just(type))
        `when`(productRepo.findByProductType(name)).thenReturn(Flux.empty())
        `when`(productTypeRepo.logicalDelete(type)).thenReturn(Mono.just(expect))


        //When
        val res = webClient.delete().uri("/${id}").accept(MediaType.APPLICATION_JSON).exchange()

        val expectedJson = Gson.toJson(expect)
        res.expectStatus().is2xxSuccessful.expectBody().json(expectedJson)
    }

    @Test
    fun `should save if there is product type`() {
        //Given
        val type = ProductType(null, "type1", "desc")
        val expected = ProductType("001", "type1", "desc")
        `when`(productTypeRepo.save(type)).thenReturn(Mono.just(expected))

        //When
        var res = webClient.post().uri("/")
                .contentType(MediaType.APPLICATION_JSON).syncBody(type)
                .exchange()
        //Then
        val expectedJson = Gson.toJson(expected)
        res.expectStatus().is2xxSuccessful.expectBody().json(expectedJson)

    }

    fun `should update product type detail if id exist in the system`() {
        //Given
        val id = "001"
        val type = ProductType("001", "type1", "desc")
        val expected = ProductType("001", "typeUpdated", "desc", revisedDate = LocalDateTime.now())
        `when`(productTypeRepo.findById(id)).thenReturn(Mono.just(type))
        `when`(productTypeRepo.save(type)).thenReturn(Mono.just(expected))


        //When
        var res = webClient.put().uri("/${id}")
                .contentType(MediaType.APPLICATION_JSON).syncBody(type)
                .exchange()

        //Then
        val expectedJson = Gson.toJson(expected)
        res.expectStatus().is2xxSuccessful.expectBody().json(expectedJson)
    }

}