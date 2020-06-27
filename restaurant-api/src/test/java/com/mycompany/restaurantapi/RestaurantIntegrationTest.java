package com.mycompany.restaurantapi;

import com.mycompany.restaurantapi.model.City;
import com.mycompany.restaurantapi.model.Restaurant;
import com.mycompany.restaurantapi.repository.CityRepository;
import com.mycompany.restaurantapi.repository.RestaurantRepository;
import com.mycompany.restaurantapi.rest.dto.CreateRestaurantDto;
import com.mycompany.restaurantapi.rest.dto.RestaurantDto;
import com.mycompany.restaurantapi.rest.dto.UpdateRestaurantDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RestaurantIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private City city;
    private City city2;

    @BeforeEach
    void setUp() {
        city = saveDefaultCity("Porto");
        city2 = saveDefaultCity("Berlin");
    }

    @Test
    void testGetRestaurant() {
        Restaurant restaurant = saveDefaultRestaurant();

        String url = String.format(API_RESTAURANTS_RESTAURANT_ID_URL, restaurant.getId());
        ResponseEntity<RestaurantDto> responseEntity = testRestTemplate.getForEntity(url, RestaurantDto.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(restaurant.getId(), responseEntity.getBody().getId());
        assertEquals(restaurant.getName(), responseEntity.getBody().getName());
        assertEquals(restaurant.getCity().getId(), responseEntity.getBody().getCity().getId());
        assertEquals(restaurant.getCity().getName(), responseEntity.getBody().getCity().getName());
        assertEquals(0, responseEntity.getBody().getDishes().size());
    }

    @Test
    void testCreateRestaurant() {
        CreateRestaurantDto createRestaurantDto = getDefaultCreateRestaurantDto();

        ResponseEntity<RestaurantDto> responseEntity = testRestTemplate.postForEntity(API_RESTAURANTS_URL, createRestaurantDto, RestaurantDto.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertNotNull(responseEntity.getBody().getId());
        assertEquals(createRestaurantDto.getName(), responseEntity.getBody().getName());
        assertEquals(createRestaurantDto.getCityId(), responseEntity.getBody().getCity().getId());
        assertEquals(0, responseEntity.getBody().getDishes().size());

        Optional<City> optionalCity = cityRepository.findById(city.getId());
        assertTrue(optionalCity.isPresent());
        optionalCity.ifPresent(c -> assertEquals(1, c.getRestaurants().size()));
    }

    @Disabled("There is a problem on performing the cities swap. In the end, the restaurant ends up in both cities")
    @Test
    void testUpdateRestaurant() {
        Restaurant restaurant = saveDefaultRestaurant();

        UpdateRestaurantDto updateRestaurantDto = getDefaultUpdateRestaurantDto();

        String url = String.format(API_RESTAURANTS_RESTAURANT_ID_URL, restaurant.getId());
        HttpEntity<UpdateRestaurantDto> requestUpdate = new HttpEntity<>(updateRestaurantDto);
        ResponseEntity<RestaurantDto> responseEntity = testRestTemplate.exchange(url, HttpMethod.PUT, requestUpdate, RestaurantDto.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(restaurant.getId(), responseEntity.getBody().getId());
        assertEquals(updateRestaurantDto.getName(), responseEntity.getBody().getName());
        assertEquals(updateRestaurantDto.getCityId(), responseEntity.getBody().getCity().getId());
        assertEquals(0, responseEntity.getBody().getDishes().size());

        Optional<City> optionalCity = cityRepository.findById(city.getId());
        assertTrue(optionalCity.isPresent());
        optionalCity.ifPresent(c -> assertEquals(0, c.getRestaurants().size()));

        Optional<City> optionalCity2 = cityRepository.findById(city2.getId());
        assertTrue(optionalCity2.isPresent());
        optionalCity2.ifPresent(c -> assertEquals(1, c.getRestaurants().size()));
    }

    @Test
    void testDeleteRestaurant() {
        Restaurant restaurant = saveDefaultRestaurant();

        String url = String.format(API_RESTAURANTS_RESTAURANT_ID_URL, restaurant.getId());
        ResponseEntity<RestaurantDto> responseEntity = testRestTemplate.exchange(url, HttpMethod.DELETE, null, RestaurantDto.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(restaurant.getId(), responseEntity.getBody().getId());
        assertEquals(restaurant.getName(), responseEntity.getBody().getName());
        assertEquals(restaurant.getCity().getId(), responseEntity.getBody().getCity().getId());
        assertEquals(restaurant.getCity().getName(), responseEntity.getBody().getCity().getName());
        assertEquals(0, responseEntity.getBody().getDishes().size());

        Optional<City> optionalCity = cityRepository.findById(city.getId());
        assertTrue(optionalCity.isPresent());
        optionalCity.ifPresent(c -> assertEquals(0, c.getRestaurants().size()));
    }

    private CreateRestaurantDto getDefaultCreateRestaurantDto() {
        CreateRestaurantDto createRestaurantDto = new CreateRestaurantDto();
        createRestaurantDto.setName("Happy Pizza");
        createRestaurantDto.setCityId(city.getId());
        return createRestaurantDto;
    }

    private UpdateRestaurantDto getDefaultUpdateRestaurantDto() {
        UpdateRestaurantDto updateRestaurantDto = new UpdateRestaurantDto();
        updateRestaurantDto.setName("Happy Burger");
        updateRestaurantDto.setCityId(city2.getId());
        return updateRestaurantDto;
    }

    private City saveDefaultCity(String name) {
        City c = new City();
        c.setName(name);
        return cityRepository.save(c);
    }

    private Restaurant saveDefaultRestaurant() {
        Restaurant defaultRestaurant = new Restaurant();
        defaultRestaurant.setName("Happy Pizza");
        defaultRestaurant.setCity(city);

        city.getRestaurants().add(defaultRestaurant);
        cityRepository.save(city);

        return restaurantRepository.save(defaultRestaurant);
    }

    private static final String API_RESTAURANTS_URL = "/api/restaurants";
    private static final String API_RESTAURANTS_RESTAURANT_ID_URL = "/api/restaurants/%s";

}