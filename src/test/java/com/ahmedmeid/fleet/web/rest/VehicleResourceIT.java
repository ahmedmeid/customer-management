package com.ahmedmeid.fleet.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.ahmedmeid.fleet.IntegrationTest;
import com.ahmedmeid.fleet.domain.Customer;
import com.ahmedmeid.fleet.domain.Vehicle;
import com.ahmedmeid.fleet.repository.EntityManager;
import com.ahmedmeid.fleet.repository.VehicleRepository;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link VehicleResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class VehicleResourceIT {

    private static final String DEFAULT_VEHICLE_ID = "AAAAAAAAAA";
    private static final String UPDATED_VEHICLE_ID = "BBBBBBBBBB";

    private static final String DEFAULT_VEHICLE_REG_NO = "AAAAAAAAAA";
    private static final String UPDATED_VEHICLE_REG_NO = "BBBBBBBBBB";

    private static final String DEFAULT_DEVICE_ID = "AAAAAAAAAA";
    private static final String UPDATED_DEVICE_ID = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/vehicles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleRepository vehicleRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Vehicle vehicle;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Vehicle createEntity(EntityManager em) {
        Vehicle vehicle = new Vehicle().vehicleId(DEFAULT_VEHICLE_ID).vehicleRegNo(DEFAULT_VEHICLE_REG_NO).deviceId(DEFAULT_DEVICE_ID);
        // Add required entity
        Customer customer;
        customer = em.insert(CustomerResourceIT.createEntity(em)).block();
        vehicle.setOwner(customer);
        return vehicle;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Vehicle createUpdatedEntity(EntityManager em) {
        Vehicle vehicle = new Vehicle().vehicleId(UPDATED_VEHICLE_ID).vehicleRegNo(UPDATED_VEHICLE_REG_NO).deviceId(UPDATED_DEVICE_ID);
        // Add required entity
        Customer customer;
        customer = em.insert(CustomerResourceIT.createUpdatedEntity(em)).block();
        vehicle.setOwner(customer);
        return vehicle;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Vehicle.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        CustomerResourceIT.deleteEntities(em);
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        vehicle = createEntity(em);
    }

    @Test
    void createVehicle() throws Exception {
        int databaseSizeBeforeCreate = vehicleRepository.findAll().collectList().block().size();
        // Create the Vehicle
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(vehicle))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeCreate + 1);
        Vehicle testVehicle = vehicleList.get(vehicleList.size() - 1);
        assertThat(testVehicle.getVehicleId()).isEqualTo(DEFAULT_VEHICLE_ID);
        assertThat(testVehicle.getVehicleRegNo()).isEqualTo(DEFAULT_VEHICLE_REG_NO);
        assertThat(testVehicle.getDeviceId()).isEqualTo(DEFAULT_DEVICE_ID);
    }

    @Test
    void createVehicleWithExistingId() throws Exception {
        // Create the Vehicle with an existing ID
        vehicle.setId(1L);

        int databaseSizeBeforeCreate = vehicleRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(vehicle))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkVehicleIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = vehicleRepository.findAll().collectList().block().size();
        // set the field null
        vehicle.setVehicleId(null);

        // Create the Vehicle, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(vehicle))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkVehicleRegNoIsRequired() throws Exception {
        int databaseSizeBeforeTest = vehicleRepository.findAll().collectList().block().size();
        // set the field null
        vehicle.setVehicleRegNo(null);

        // Create the Vehicle, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(vehicle))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkDeviceIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = vehicleRepository.findAll().collectList().block().size();
        // set the field null
        vehicle.setDeviceId(null);

        // Create the Vehicle, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(vehicle))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllVehiclesAsStream() {
        // Initialize the database
        vehicleRepository.save(vehicle).block();

        List<Vehicle> vehicleList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Vehicle.class)
            .getResponseBody()
            .filter(vehicle::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(vehicleList).isNotNull();
        assertThat(vehicleList).hasSize(1);
        Vehicle testVehicle = vehicleList.get(0);
        assertThat(testVehicle.getVehicleId()).isEqualTo(DEFAULT_VEHICLE_ID);
        assertThat(testVehicle.getVehicleRegNo()).isEqualTo(DEFAULT_VEHICLE_REG_NO);
        assertThat(testVehicle.getDeviceId()).isEqualTo(DEFAULT_DEVICE_ID);
    }

    @Test
    void getAllVehicles() {
        // Initialize the database
        vehicleRepository.save(vehicle).block();

        // Get all the vehicleList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(vehicle.getId().intValue()))
            .jsonPath("$.[*].vehicleId")
            .value(hasItem(DEFAULT_VEHICLE_ID))
            .jsonPath("$.[*].vehicleRegNo")
            .value(hasItem(DEFAULT_VEHICLE_REG_NO))
            .jsonPath("$.[*].deviceId")
            .value(hasItem(DEFAULT_DEVICE_ID));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVehiclesWithEagerRelationshipsIsEnabled() {
        when(vehicleRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(vehicleRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVehiclesWithEagerRelationshipsIsNotEnabled() {
        when(vehicleRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(vehicleRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getVehicle() {
        // Initialize the database
        vehicleRepository.save(vehicle).block();

        // Get the vehicle
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, vehicle.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(vehicle.getId().intValue()))
            .jsonPath("$.vehicleId")
            .value(is(DEFAULT_VEHICLE_ID))
            .jsonPath("$.vehicleRegNo")
            .value(is(DEFAULT_VEHICLE_REG_NO))
            .jsonPath("$.deviceId")
            .value(is(DEFAULT_DEVICE_ID));
    }

    @Test
    void getNonExistingVehicle() {
        // Get the vehicle
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingVehicle() throws Exception {
        // Initialize the database
        vehicleRepository.save(vehicle).block();

        int databaseSizeBeforeUpdate = vehicleRepository.findAll().collectList().block().size();

        // Update the vehicle
        Vehicle updatedVehicle = vehicleRepository.findById(vehicle.getId()).block();
        updatedVehicle.vehicleId(UPDATED_VEHICLE_ID).vehicleRegNo(UPDATED_VEHICLE_REG_NO).deviceId(UPDATED_DEVICE_ID);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedVehicle.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedVehicle))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeUpdate);
        Vehicle testVehicle = vehicleList.get(vehicleList.size() - 1);
        assertThat(testVehicle.getVehicleId()).isEqualTo(UPDATED_VEHICLE_ID);
        assertThat(testVehicle.getVehicleRegNo()).isEqualTo(UPDATED_VEHICLE_REG_NO);
        assertThat(testVehicle.getDeviceId()).isEqualTo(UPDATED_DEVICE_ID);
    }

    @Test
    void putNonExistingVehicle() throws Exception {
        int databaseSizeBeforeUpdate = vehicleRepository.findAll().collectList().block().size();
        vehicle.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, vehicle.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(vehicle))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchVehicle() throws Exception {
        int databaseSizeBeforeUpdate = vehicleRepository.findAll().collectList().block().size();
        vehicle.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(vehicle))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamVehicle() throws Exception {
        int databaseSizeBeforeUpdate = vehicleRepository.findAll().collectList().block().size();
        vehicle.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(vehicle))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateVehicleWithPatch() throws Exception {
        // Initialize the database
        vehicleRepository.save(vehicle).block();

        int databaseSizeBeforeUpdate = vehicleRepository.findAll().collectList().block().size();

        // Update the vehicle using partial update
        Vehicle partialUpdatedVehicle = new Vehicle();
        partialUpdatedVehicle.setId(vehicle.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedVehicle.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedVehicle))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeUpdate);
        Vehicle testVehicle = vehicleList.get(vehicleList.size() - 1);
        assertThat(testVehicle.getVehicleId()).isEqualTo(DEFAULT_VEHICLE_ID);
        assertThat(testVehicle.getVehicleRegNo()).isEqualTo(DEFAULT_VEHICLE_REG_NO);
        assertThat(testVehicle.getDeviceId()).isEqualTo(DEFAULT_DEVICE_ID);
    }

    @Test
    void fullUpdateVehicleWithPatch() throws Exception {
        // Initialize the database
        vehicleRepository.save(vehicle).block();

        int databaseSizeBeforeUpdate = vehicleRepository.findAll().collectList().block().size();

        // Update the vehicle using partial update
        Vehicle partialUpdatedVehicle = new Vehicle();
        partialUpdatedVehicle.setId(vehicle.getId());

        partialUpdatedVehicle.vehicleId(UPDATED_VEHICLE_ID).vehicleRegNo(UPDATED_VEHICLE_REG_NO).deviceId(UPDATED_DEVICE_ID);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedVehicle.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedVehicle))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeUpdate);
        Vehicle testVehicle = vehicleList.get(vehicleList.size() - 1);
        assertThat(testVehicle.getVehicleId()).isEqualTo(UPDATED_VEHICLE_ID);
        assertThat(testVehicle.getVehicleRegNo()).isEqualTo(UPDATED_VEHICLE_REG_NO);
        assertThat(testVehicle.getDeviceId()).isEqualTo(UPDATED_DEVICE_ID);
    }

    @Test
    void patchNonExistingVehicle() throws Exception {
        int databaseSizeBeforeUpdate = vehicleRepository.findAll().collectList().block().size();
        vehicle.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, vehicle.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(vehicle))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchVehicle() throws Exception {
        int databaseSizeBeforeUpdate = vehicleRepository.findAll().collectList().block().size();
        vehicle.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(vehicle))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamVehicle() throws Exception {
        int databaseSizeBeforeUpdate = vehicleRepository.findAll().collectList().block().size();
        vehicle.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(vehicle))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteVehicle() {
        // Initialize the database
        vehicleRepository.save(vehicle).block();

        int databaseSizeBeforeDelete = vehicleRepository.findAll().collectList().block().size();

        // Delete the vehicle
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, vehicle.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Vehicle> vehicleList = vehicleRepository.findAll().collectList().block();
        assertThat(vehicleList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
