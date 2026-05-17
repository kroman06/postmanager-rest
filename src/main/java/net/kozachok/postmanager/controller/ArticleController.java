package net.kozachok.postmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.domain.ArticleStatus;
import net.kozachok.postmanager.dto.request.ArticleRequest;
import net.kozachok.postmanager.dto.response.ArticleResponse;
import net.kozachok.postmanager.dto.response.PageResponse;
import net.kozachok.postmanager.security.SecurityUtils;
import net.kozachok.postmanager.service.ArticleService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public PageResponse<ArticleResponse> findPublished(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)    Integer categoryId) {
        return articleService.findPublished(
                PageRequest.of(page, size, Sort.by("publishedAt").descending()),
                categoryId);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<ArticleResponse> findAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)    ArticleStatus status) {
        return articleService.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()),
                status);
    }

    @GetMapping("/{id}")
    public ArticleResponse findById(@PathVariable UUID id) {
        return articleService.findById(id);
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ArticleResponse findMyById(@PathVariable UUID id) {
        return articleService.findMyById(id, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('AUTHOR')")
    public PageResponse<ArticleResponse> findMy(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        var currentUser = SecurityUtils.getCurrentUser();
        return articleService.findByAuthor(
                currentUser.id(), PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @GetMapping("/author/{authorId}")
    public PageResponse<ArticleResponse> findByAuthor(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return articleService.findByAuthor(
                authorId, PageRequest.of(page, size, Sort.by("publishedAt").descending()));
    }

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse create(@Valid @RequestBody ArticleRequest request) {
        return articleService.create(request, SecurityUtils.getCurrentUser());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ArticleResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody ArticleRequest request) {
        return articleService.update(id, request, SecurityUtils.getCurrentUser());
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('AUTHOR')")
    public ArticleResponse publish(@PathVariable UUID id) {
        return articleService.publish(id, SecurityUtils.getCurrentUser());
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public ArticleResponse archive(@PathVariable UUID id) {
        return articleService.archive(id, SecurityUtils.getCurrentUser());
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public ArticleResponse restore(@PathVariable UUID id) {
        return articleService.restore(id, SecurityUtils.getCurrentUser());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        articleService.delete(id, SecurityUtils.getCurrentUser());
    }
}