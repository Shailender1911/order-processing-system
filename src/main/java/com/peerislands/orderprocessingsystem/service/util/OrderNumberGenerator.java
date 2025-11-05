package com.peerislands.orderprocessingsystem.service.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class OrderNumberGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
        .withZone(ZoneOffset.UTC);
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        String datePart = DATE_FORMATTER.format(Instant.now());
        String randomPart = randomSegment(6);
        return "ORD-" + datePart + "-" + randomPart;
    }

    private String randomSegment(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC.length());
            builder.append(ALPHANUMERIC.charAt(index));
        }
        return builder.toString();
    }
}

