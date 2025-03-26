package com.blog.service.impl;

import com.blog.model.Tag;
import com.blog.repository.TagRepository;
import com.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

  @Autowired
  private TagRepository tagRepository;

  @Override
  public List<Tag> getAllTags() {
    return tagRepository.findAll();
  }

  @Override
  public Tag createTag(Tag tag) {
    return tagRepository.save(tag);
  }

  @Override
  public Tag updateTag(Long id, Tag tag) {
    Tag existingTag = tagRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Tag not found"));

    existingTag.setName(tag.getName());
    existingTag.setDescription(tag.getDescription());

    return tagRepository.save(existingTag);
  }

  @Override
  public void deleteTag(Long id) {
    tagRepository.deleteById(id);
  }
}