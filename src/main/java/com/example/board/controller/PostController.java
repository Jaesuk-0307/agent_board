package com.example.board.controller;

import com.example.board.entity.Post;
import com.example.board.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Controller
public class PostController {

    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Post> posts = service.list(page);
        model.addAttribute("posts", posts);
        return "list";
    }

    @GetMapping("/posts/new")
    public String createForm(Model model) {
        model.addAttribute("post", new Post());
        return "form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute Post post, BindingResult result) {
        if (result.hasErrors()) {
            return "form";
        }
        service.create(post);
        return "redirect:/";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("post", service.get(id));
        model.addAttribute("page", page);
        return "detail";
    }

    @GetMapping("/posts/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("post", service.get(id));
        model.addAttribute("postId", id);
        return "form";
    }

    @PostMapping("/posts/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Post post, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("postId", id);
            return "form";
        }
        service.update(id, post);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/";
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String notFound() {
        return "redirect:/";
    }
}
