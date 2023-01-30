package com.vidyasource.springbootreactivedemo.pets;

import java.util.List;

public record Pet(long id, Category category, String name, List<String> photoUrls, List<Tag> tags, String status) {}
record Category(int id, String name) { }
record Tag (String name, String address) {}
