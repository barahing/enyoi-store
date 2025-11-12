package com.store.carts_microservice.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendRequest {
    private String to;
    private String subject;
    private String html;
    private String attachmentPath;
}
