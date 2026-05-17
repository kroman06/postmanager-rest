package net.kozachok.postmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.dto.request.CommentRequest;
import net.kozachok.postmanager.dto.response.CommentResponse;
import net.kozachok.postmanager.dto.response.PageResponse;
import net.kozachok.postmanager.security.SecurityUtils;
import net.kozachok.postmanager.service.CommentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @Operation(
            summary = "Get article comments",
            description = "Returns a paginated list of comments for the specified article."
    )
    @GetMapping("/articles/{articleId}/comments")
    public PageResponse<CommentResponse> findByArticleId(
            @PathVariable UUID articleId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return commentService.findByArticleId(
                articleId, PageRequest.of(page, size, Sort.by("createdAt").ascending()));
    }

    @Operation(
            summary = "Create comment",
            description = "Creates a new comment for the specified published article. Requires authentication."
    )
    @PostMapping("/articles/{articleId}/comments")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@PathVariable UUID articleId,
                                  @Valid @RequestBody CommentRequest request) {
        return commentService.create(request.content(), articleId, SecurityUtils.getCurrentUser());
    }

    @Operation(
            summary = "Delete comment",
            description = "Deletes a comment by its identifier. Requires administrator role."
    )
    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        commentService.delete(id, SecurityUtils.getCurrentUser());
    }
}