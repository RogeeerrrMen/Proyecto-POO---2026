package edu.uvg.gestionpedidos.controller;

import java.security.Principal;
import java.util.NoSuchElementException;
import edu.uvg.gestionpedidos.dto.CalendarioDTO;
import edu.uvg.gestionpedidos.service.CalendarioService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalendarioControllerTest {
    @Test
    void usarIdentidadAutenticadaYMostrarCalendario() {
        CalendarioService service = mock(CalendarioService.class);
        CalendarioDTO calendario = mock(CalendarioDTO.class);
        when(service.consultar("ana@example.com", "2026-11")).thenReturn(calendario);
        ExtendedModelMap model = new ExtendedModelMap();
        Principal principal = () -> "ana@example.com";
        String vista = new CalendarioController(service).mostrar("2026-11", principal,
                model, new RedirectAttributesModelMap());
        assertEquals("calendario", vista);
        assertSame(calendario, model.get("calendario"));
        verify(service).consultar("ana@example.com", "2026-11");
    }

    @Test
    void redirigirAConfiguracionSiNoExisteNegocio() {
        CalendarioService service = mock(CalendarioService.class);
        when(service.consultar("ana@example.com", null))
                .thenThrow(new NoSuchElementException("Registra tu emprendimiento."));
        RedirectAttributesModelMap mensajes = new RedirectAttributesModelMap();
        String vista = new CalendarioController(service).mostrar(null,
                () -> "ana@example.com", new ExtendedModelMap(), mensajes);
        assertEquals("redirect:/emprendimientos", vista);
        assertEquals("Registra tu emprendimiento.", mensajes.getFlashAttributes().get("error"));
    }

    @Test
    void redirigirAlMesActualConMensajeSiElParametroEsInvalido() {
        CalendarioService service = mock(CalendarioService.class);
        when(service.consultar("ana@example.com", "2026-13"))
                .thenThrow(new IllegalArgumentException("Mes inválido."));
        RedirectAttributesModelMap mensajes = new RedirectAttributesModelMap();
        String vista = new CalendarioController(service).mostrar("2026-13",
                () -> "ana@example.com", new ExtendedModelMap(), mensajes);
        assertEquals("redirect:/calendario", vista);
        assertEquals("Mes inválido.", mensajes.getFlashAttributes().get("error"));
    }
}
