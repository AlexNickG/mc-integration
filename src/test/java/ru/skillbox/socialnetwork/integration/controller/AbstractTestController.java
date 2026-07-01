package ru.skillbox.socialnetwork.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest //аннотация говорит о том, что тест является интеграционным и должен загружать и инициализировать весь контекст
@AutoConfigureMockMvc //аннотация является частью фреймворка Spring Boot Test. Используется для автоматической конфигурации и внедрения MockMvc в контекст
public abstract class AbstractTestController {

    @Autowired
    protected MockMvc mockMvc; //Основная задача MockMvc - симуляция HTTP запросов и проверка ответов от контролера

    @Autowired
    protected ObjectMapper objectMapper; //Является частью библиотеки Jackson и нужна для преобразования объектов Java в JSON и обратно

    //protected
}
