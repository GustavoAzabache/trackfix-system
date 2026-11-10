package com.me.backend_springboot.tenant.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class Base36GeneratorTest {

    @Test
    void shouldReturnAFourCharacterCode() {
        String resultado = Base36Generator.generate();

        assertNotNull(resultado);
        assertEquals(4, resultado.length());
    }

    @Test
    void shouldGenerateADifferentCodeOnEachCall() {
        String primerResultado = Base36Generator.generate();
        String segundoResultado = Base36Generator.generate();

        assertNotEquals(primerResultado, segundoResultado);
    }

    @Test
    void shouldContainOnlyBase36Characters() {
        String regex = "^[0-9A-Z]{4}$";

        String resultado = Base36Generator.generate();

        assertTrue(resultado.matches(regex));
    }

}
