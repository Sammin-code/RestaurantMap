package com.blog.service;

import com.blog.model.Tag;
import java.util.List;

public interface TagService {
  List<Tag> getAllTags();

  Tag createTag(Tag tag);

  Tag updateTag(Long id, Tag tag);

  void deleteTag(Long id);
}