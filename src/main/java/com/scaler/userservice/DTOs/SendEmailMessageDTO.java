package com.scaler.userservice.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailMessageDTO {
    private String to;
    private String from;
    private String body;
    private String subject;
}
