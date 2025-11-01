package io.github.habatoo.service.dto;

import org.springframework.http.MediaType;

public record ImageResponseDto(byte[] data, MediaType mediaType) {}
