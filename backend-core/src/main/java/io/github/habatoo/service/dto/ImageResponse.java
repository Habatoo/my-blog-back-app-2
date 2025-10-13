package io.github.habatoo.service.dto;

import org.springframework.http.MediaType;

public record ImageResponse(byte[] data, MediaType mediaType) {}
