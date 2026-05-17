package net.kozachok.postmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.dto.request.CommentRequest;
import net.kozachok.postmanager.dto.response.CommentResponse;
import net.kozachok.postmanager.security.SecurityUtils;
import net.kozachok.postmanager.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/articles/{articleId}/comments")
    public List<CommentResponse> findByArticleId(@PathVariable UUID articleId) {
        return commentService.findByArticleId(articleId);
    }

    @PostMapping("/articles/{articleId}/comments")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@PathVariable UUID articleId,
                                  @Valid @RequestBody CommentRequest request) {
        return commentService.create(request.content(), articleId, SecurityUtils.getCurrentUser());
    }

    @PatchMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public CommentResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody CommentRequest request) {
        return commentService.update(id, request.content(), SecurityUtils.getCurrentUser());
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        commentService.delete(id, SecurityUtils.getCurrentUser());
    }
}