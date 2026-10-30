package com.me.backend_springboot.tenant.utils;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class Base36Generator {

    private static final String BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int len = 4;

    public static String base36Generator(Long id) {
        Random random = new Random(id);

        StringBuilder codigo = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            int indice = random.nextInt(BASE36_CHARS.length());
            codigo.append(BASE36_CHARS.charAt(indice));
        }

        return codigo.toString();
    }

}
