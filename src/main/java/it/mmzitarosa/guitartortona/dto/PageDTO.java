package it.mmzitarosa.guitartortona.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageDTO<T>(List<T> content, long totalElements, int page, int size) {
	public static <T> PageDTO<T> of(Page<T> page) {
		return new PageDTO<>(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
	}
}
