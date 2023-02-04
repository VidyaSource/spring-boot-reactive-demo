package com.vidyasource.springbootreactivedemo;

import com.vidyasource.springbootreactivedemo.applicant.AdoptionApplicant;
import com.vidyasource.springbootreactivedemo.applicant.ApplicantDatabase;
import com.vidyasource.springbootreactivedemo.pets.Adoption;
import com.vidyasource.springbootreactivedemo.pets.Pet;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DemoController {
    @GetMapping(value = "/demo/{name}")
    public Mono<String> getGreeting(@PathVariable String name) {
        return Mono.just(String.format("Hello, %s! Welcome to the Pet Demo!", name));
    }

    @GetMapping(value = "/demo/pets")
    public Flux<Pet> getPets() {
        WebClient client = WebClient.builder()
                .baseUrl("https://petstore.swagger.io")
                .build();

        return client
                .get()
                .uri(uriBuilder -> uriBuilder.path("/v2/pet/findByStatus")
                        .queryParam("status", "available")
                        .build())
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {});
    }

    @GetMapping(value = "/demo/adoptions")
    public Flux<Adoption> getAdoptions() {
        ApplicantDatabase db = new ApplicantDatabase();

        return db.findAll().zipWith(getPets(), (applicant, pet) -> new Adoption(applicant.name(), pet));
    }

    @GetMapping(value = "/demo/adoptions/dogs")
    public Flux<Adoption> getDogAdoptions() {
        ApplicantDatabase db = new ApplicantDatabase();

        // In real life if you are pulling from a DB, you would use your DB to filter, but this filters the REST response
        return db.findAll()
                .zipWith(getPets()
                .filter(p -> p.name().equals("doggie")), (applicant, pet) -> new Adoption(applicant.name(), pet));
    }


    @GetMapping(value = "/demo/errors/return")
    public Flux<AdoptionApplicant> getApplicantsWithErrorReturn() {
        ApplicantDatabase db = new ApplicantDatabase();

        return db.findApplicantsButError()
                .doOnError(e -> System.out.printf("Error: %s%n", e.getMessage()))
                .retry(1)
                .onErrorReturn(new AdoptionApplicant(-1, ""));
    }

    @GetMapping(value = "/demo/errors/resume")
    public Mono<ResponseEntity<List<AdoptionApplicant>>> getApplicantsWithErrorResume() {
        ApplicantDatabase db = new ApplicantDatabase();

        return db
                .findApplicantsButError()
                .collectList()
                .map(applicants -> new ResponseEntity<>(applicants, HttpStatus.OK))
                .doOnError(e -> System.out.printf("Error: %s%n", e.getMessage()))
                .onErrorResume(e -> Mono.just(new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR)));
    }
}
