package com.me.backend_springboot.tenant.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class Base36GeneratorTest {

    @Test
    void shouldReturnAFourCharacterCode() {
        Long id = 30L;

        String resultado = Base36Generator.base36Generator(id);

        assertNotNull(resultado);
        assertEquals(4, resultado.length());
    }

    @Test
    void shouldBeDeterministic() {
        Long id = 567L;

        String primerResultado = Base36Generator.base36Generator(id);
        String segundoResultado = Base36Generator.base36Generator(id);

        assertEquals(primerResultado, segundoResultado);
    }

    @Test
    void shouldContainOnlyBase36Characters() {
        Long id = 45L;
        String regex = "^[0-9A-Z]{4}$";

        String resultado = Base36Generator.base36Generator(id);

        assertTrue(resultado.matches(regex));
    }

}