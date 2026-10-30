package com.dawn.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateRenderer {

    private final TemplateEngine templateEngine;

    public String render(String templateId, Map<String, Object> variables) {
        Context ctx = new Context();
        if (variables != null) {
            variables.forEach(ctx::setVariable);
        }
        String templatePath = "notification/" + templateId;
        try {
            return templateEngine.process(templatePath, ctx);
        } catch (Exception e) {
            log.warn("Template '{}' not found, falling back to plain text", templatePath);
            return buildFallback(templateId, variables);
        }
    }

    private String buildFallback(String templateId, Map<String, Object> variables) {
        StringBuilder sb = new StringBuilder("<p>Notification: ").append(templateId).append("</p><ul>");
        if (variables != null) {
            variables.forEach((k, v) -> sb.append("<li>").append(k).append(": ").append(v).append("</li>"));
        }
        return sb.append("</ul>").toString();
    }
}
