package com.example.board.service;

import com.example.board.entity.Post;
import com.example.board.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PostService {

    private static final int PAGE_SIZE = 10;

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public Page<Post> list(int page) {
        return repository.findAll(PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
    }

    public Post get(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("게시글이 없습니다."));
    }

    public void create(Post post) {
        repository.save(post);
    }

    public void update(Long id, Post form) {
        Post post = get(id);
        post.setTitle(form.getTitle());
        post.setName(form.getName());
        post.setContent(form.getContent());
        repository.save(post);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Optional<Post> findLatest() {
        return repository.findFirstByOrderByIdDesc();
    }

    /** 이미 삭제된 글이면 false */
    public boolean deleteIfExists(Long id) {
        return repository.deleteByPostId(id) > 0;
    }

    /** 최신 글 삭제. 글이 없거나 이미 삭제되었으면 false */
    public boolean deleteLatest() {
        return findLatest().map(post -> deleteIfExists(post.getId())).orElse(false);
    }
}
