package ru.unisuite.contentservlet.service;

import ru.unisuite.contentservlet.model.Content;

public interface ContentService {
    Content getContent(ContentRequest contentRequest);
}
