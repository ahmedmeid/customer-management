package com.ahmedmeid.fleet.repository;

import com.ahmedmeid.fleet.domain.Vehicle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Vehicle entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VehicleRepository extends ReactiveCrudRepository<Vehicle, Long>, VehicleRepositoryInternal {
    @Override
    Mono<Vehicle> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Vehicle> findAllWithEagerRelationships();

    @Override
    Flux<Vehicle> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM vehicle entity WHERE entity.owner_id = :id")
    Flux<Vehicle> findByOwner(Long id);

    @Query("SELECT * FROM vehicle entity WHERE entity.owner_id IS NULL")
    Flux<Vehicle> findAllWhereOwnerIsNull();

    @Override
    <S extends Vehicle> Mono<S> save(S entity);

    @Override
    Flux<Vehicle> findAll();

    @Override
    Mono<Vehicle> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface VehicleRepositoryInternal {
    <S extends Vehicle> Mono<S> save(S entity);

    Flux<Vehicle> findAllBy(Pageable pageable);

    Flux<Vehicle> findAll();

    Mono<Vehicle> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Vehicle> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Vehicle> findOneWithEagerRelationships(Long id);

    Flux<Vehicle> findAllWithEagerRelationships();

    Flux<Vehicle> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
