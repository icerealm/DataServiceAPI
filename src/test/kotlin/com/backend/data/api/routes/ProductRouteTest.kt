package com.backend.data.api.routes

import com.backend.data.api.domain.Product
import com.backend.data.api.handler.ProductHandler
import com.backend.data.api.repository.ProductRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import org.junit.Before
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime


@RunWith(SpringRunner::class)

class ProductRouteTest: IntegrateTest() {
    @Mock
    lateinit var productRepo: ProductRepository
    lateinit var productHandler: ProductHandler
    lateinit var sut: AppRoutes //system under test
    lateinit var webClient: WebTestClient


    @Before
    fun setup() {
        productHandler = ProductHandler(productRepo)
        sut = AppRoutes()
        sut.productHandler = productHandler
        webClient = WebTestClient.bindToRouterFunction(sut.productApi()).configureClient()
                .baseUrl("http://localhost/products")
                .build();
    }

    @Test
    fun `should return empty with success when no product`() {
        //Given
        `when`(productRepo.findAllProductWhereEnableFlg(true)).thenReturn(Flux.empty())

        //When
        var res = webClient.get().uri("/")
                        .accept(MediaType.APPLICATION_JSON).exchange()

        //then
        res.expectStatus().is2xxSuccessful
                .expectBody().json("[]")
    }

    @Test
    fun `ควรจะคืนค่า 404 error เมื่อ path ผิด`() {
        //Given
        var param = "bxx"
        `when`(productRepo.findById(param)).thenReturn(Mono.empty())

        //When
        var res = webClient.get().uri("/${param}")
                .accept(MediaType.APPLICATION_JSON).exchange()

        //then
        res.expectStatus().is4xxClientError
    }

    @Test
    fun `should return active product if there is active product`() {
        //Given
        var productList = listOf(Product("0001", "ProductA", "DescOfProductA"),
                Product("0002", "ProductB", "DescOfProductB"))
        `when`(productRepo.findAllProductWhereEnableFlg(true)).thenReturn(Flux.fromIterable(productList))

        //When
        var res = webClient.get().uri("/").accept(MediaType.APPLICATION_JSON).exchange()

        //Then
        val expectedJson = Gson.toJson(productList)

        res.expectStatus().is2xxSuccessful.expectBody().json(expectedJson)

    }

    @Test
    fun `should save if there is product`() {
        //Given
        val product = Product("ProductA", "DescOfProductA")
        val expectedProduct = Product("0001", "ProductA", "DescOfProductA")
        `when`(productRepo.save(product)).thenReturn(Mono.just(expectedProduct))

        //When
        var res = webClient.post().uri("/")
                                    .contentType(MediaType.APPLICATION_JSON).syncBody(product)
                                    .exchange()
        //Then
        val expectedJson = Gson.toJson(expectedProduct)
        res.expectStatus().is2xxSuccessful.expectBody().json(expectedJson)
    }

    fun `should update product detail if id exist in the system`() {
        //Given
        val id = "001"
        val existing = Product("0001", "ProductA", "DescOfProductA")
        val expected = Product("0001", "ProductA", "DescOfProductA", revisedDate = LocalDateTime.now())
        `when`(productRepo.findById(id)).thenReturn(Mono.just(existing))
        `when`(productRepo.save(existing)).thenReturn(Mono.just(expected))


        //When
        var res = webClient.put().uri("/${id}")
                .contentType(MediaType.APPLICATION_JSON).syncBody(existing)
                .exchange()

        //Then
        val expectedJson = Gson.toJson(expected)
        res.expectStatus().is2xxSuccessful.expectBody().json(expectedJson)
    }
}