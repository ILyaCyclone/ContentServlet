package ru.unisuite.contentservlet.service;

import ru.unisuite.contentservlet.model.Content;
import ru.unisuite.contentservlet.model.HashAndLastModified;

public interface ContentService {
    Content getContent(ContentRequest contentRequest);

    HashAndLastModified getHashAndLastModified(ContentRequest contentRequest);
}
