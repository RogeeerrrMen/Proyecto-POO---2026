package edu.uvg.gestionpedidos.controller;

import java.math.BigDecimal;

import edu.uvg.gestionpedidos.dto.ResumenPagoDTO;
import edu.uvg.gestionpedidos.entity.MetodoPago;
import edu.uvg.gestionpedidos.service.PagoService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping("/pedidos/{pedidoId}/pago")
    public String mostrarPago(@PathVariable Long pedidoId, Model model, RedirectAttributes mensajes) {

        try {
            ResumenPagoDTO resumen = pagoService.consultarResumen(pedidoId);

            model.addAttribute("pedidoId", resumen.getPedidoId());
            model.addAttribute("totalPedido", resumen.getTotalPedido());
            model.addAttribute("totalAbonado", resumen.getTotalAbonado());
            model.addAttribute("saldoPendiente", resumen.getSaldoPendiente());
            model.addAttribute("estadoPago", resumen.getEstado());

            model.addAttribute("metodosPago", MetodoPago.values());

            model.addAttribute("historialAbonos", pagoService.consultarHistorial(pedidoId));

            model.addAttribute("montoSinDetalle", pagoService.consultarMontoSinDetalle(pedidoId));

            return "pago-pedido";

        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());

            return "redirect:/pedidos";
        }
    }

    @PostMapping("/pedidos/{pedidoId}/pago/abonar")
    public String registrarAbono(
            @PathVariable Long pedidoId,
            @RequestParam(required = false) String monto,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) String referencia,
            RedirectAttributes mensajes) {

        try {
            BigDecimal montoValidado = convertirMonto(monto);
            MetodoPago metodoValidado = convertirMetodo(metodoPago);

            pagoService.registrarAbono(pedidoId, montoValidado, metodoValidado, referencia);

            mensajes.addFlashAttribute("mensaje", "Abono registrado correctamente.");

        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());

            mensajes.addFlashAttribute("montoIngresado", monto);

            mensajes.addFlashAttribute("metodoPagoSeleccionado", metodoPago);

            mensajes.addFlashAttribute("referenciaIngresada", referencia);
        }

        return "redirect:/pedidos/" + pedidoId + "/pago";
    }

    private BigDecimal convertirMonto(String monto) {
        if (monto == null || monto.isBlank()) {
            throw new IllegalArgumentException("Debes ingresar el monto del abono.");
        }

        try {
            return new BigDecimal(monto.trim());

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El monto debe ser un número válido.");
        }
    }

    private MetodoPago convertirMetodo(String metodoPago) {
        if (metodoPago == null || metodoPago.isBlank()) {
            throw new IllegalArgumentException("Debes seleccionar un método de pago.");
        }

        try {
            return MetodoPago.valueOf(metodoPago.trim());

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El método de pago seleccionado no es válido.");
        }
    }
}