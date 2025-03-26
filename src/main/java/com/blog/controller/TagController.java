package com.blog.controller;

import com.blog.model.Tag;
import com.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "http://localhost:8081")
public class TagController {

  @Autowired
  private TagService tagService;

  @GetMapping
  public ResponseEntity<List<Tag>> getAllTags() {
    return ResponseEntity.ok(tagService.getAllTags());
  }

  @PostMapping
  public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
    return ResponseEntity.ok(tagService.createTag(tag));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Tag> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
    return ResponseEntity.ok(tagService.updateTag(id, tag));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
    tagService.deleteTag(id);
    return ResponseEntity.ok().build();
  }
}