package net.kozachok.postmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleRequest(@NotBlank @Size(max = 255) String title, @NotBlank String content, Integer categoryId) {

}
