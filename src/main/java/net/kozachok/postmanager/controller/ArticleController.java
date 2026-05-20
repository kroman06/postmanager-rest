package net.kozachok.postmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(
            summary = "Get published articles",
            description = "Returns a paginated list of published articles. Optionally filters articles by category."
    )
    @GetMapping
    public PageResponse<ArticleResponse> findPublished(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)    Integer categoryId) {
        return articleService.findPublished(
                PageRequest.of(page, size, Sort.by("publishedAt").descending()),
                categoryId);
    }

    @Operation(
            summary = "Get all articles for admin",
            description = "Returns a paginated list of all articles for administrators. Optionally filters articles by status."
    )
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

    @Operation(
            summary = "Get published article by ID",
            description = "Returns a published article by its unique identifier."
    )
    @GetMapping("/{id}")
    public ArticleResponse findById(@PathVariable UUID id) {
        return articleService.findById(id);
    }

    @Operation(
            summary = "Get own article by ID",
            description = "Returns an article owned by the currently authenticated author."
    )
    @GetMapping("/my/{id}")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public ArticleResponse findMyById(@PathVariable UUID id) {
        return articleService.findMyById(id, SecurityUtils.getCurrentUser());
    }

    @Operation(
            summary = "Get current author's articles",
            description = "Returns a paginated list of articles created by the currently authenticated author."
    )
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public PageResponse<ArticleResponse> findMy(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        var currentUser = SecurityUtils.getCurrentUser();
        return articleService.findByAuthor(
                currentUser.id(), PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Operation(
            summary = "Get published articles by author",
            description = "Returns a paginated list of published articles created by the specified author."
    )
    @GetMapping("/author/{authorId}")
    public PageResponse<ArticleResponse> findByAuthor(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return articleService.findByAuthor(
                authorId, PageRequest.of(page, size, Sort.by("publishedAt").descending()));
    }

    @Operation(
            summary = "Create article",
            description = "Creates a new draft article for the currently authenticated author."
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse create(@Valid @RequestBody ArticleRequest request) {
        return articleService.create(request, SecurityUtils.getCurrentUser());
    }

    @Operation(
            summary = "Update article",
            description = "Updates an article owned by the currently authenticated author."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public ArticleResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody ArticleRequest request) {
        return articleService.update(id, request, SecurityUtils.getCurrentUser());
    }

    @Operation(
            summary = "Publish article",
            description = "Publishes a draft article owned by the currently authenticated author."
    )
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public ArticleResponse publish(@PathVariable UUID id) {
        return articleService.publish(id, SecurityUtils.getCurrentUser());
    }

    @Operation(
            summary = "Archive article",
            description = "Archives a published article. Authors can archive their own articles, administrators can archive any article."
    )
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public ArticleResponse archive(@PathVariable UUID id) {
        return articleService.archive(id, SecurityUtils.getCurrentUser());
    }

    @Operation(
            summary = "Restore article",
            description = "Restores an archived article back to published status. Authors can restore their own articles, administrators can restore any article."
    )
    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public ArticleResponse restore(@PathVariable UUID id) {
        return articleService.restore(id, SecurityUtils.getCurrentUser());
    }

    @Operation(
            summary = "Delete article",
            description = "Deletes an article. Administrators can delete any article, authors can delete only their own draft articles."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        articleService.delete(id, SecurityUtils.getCurrentUser());
    }
}