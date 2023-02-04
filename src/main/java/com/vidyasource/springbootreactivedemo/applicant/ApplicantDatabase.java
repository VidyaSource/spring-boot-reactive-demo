package com.vidyasource.springbootreactivedemo.applicant;

import com.github.javafaker.Faker;
import reactor.core.publisher.Flux;

public class ApplicantDatabase {
    private final Faker faker;
    public ApplicantDatabase() {
        faker = new Faker();
    }

    public Flux<AdoptionApplicant> findAll() {
        return Flux
                .range(1, 20)
                .map(i -> new AdoptionApplicant(i, faker.name().fullName()));
    }

    public Flux<AdoptionApplicant> findApplicantsButError() {
        return Flux.concat(
                Flux.just(new AdoptionApplicant(1, faker.name().fullName())),
                Flux.just(new AdoptionApplicant(2, faker.name().fullName())),
                Flux.error(new Exception(("There was an error"))),
                Flux.just(new AdoptionApplicant(4, faker.name().fullName()))
        );
    }
}
