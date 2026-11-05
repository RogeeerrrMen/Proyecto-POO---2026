package edu.uvg.gestionpedidos.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.uvg.gestionpedidos.entity.Pago;
import edu.uvg.gestionpedidos.service.PagoService;

@Controller
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {

        this.pagoService = pagoService;
    }

    @GetMapping("/pedidos/{pedidoId}/pago")
    public String mostrarPago(

            @PathVariable Long pedidoId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            Pago pago = pagoService.obtenerOCrearPago(pedidoId);

            model.addAttribute("pedidoId", pedidoId);
            model.addAttribute("totalPedido", pago.getTotalPedido());
            model.addAttribute("totalAbonado", pago.getTotalAbonado());
            model.addAttribute("saldoPendiente", pago.getSaldoPendiente());
            model.addAttribute("estadoPago", pago.getEstado());

            return "pago-pedido";

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute("error", ex.getMessage());

            return "redirect:/pedidos";
        }
    }

    @PostMapping("/pedidos/{pedidoId}/pago/abonar")
    public String registrarAbono(

            @PathVariable Long pedidoId,
            @RequestParam BigDecimal monto,
            RedirectAttributes redirectAttributes) {

        try {

            pagoService.registrarAbono(pedidoId, monto);

            redirectAttributes.addFlashAttribute("mensaje", "Abono registrado correctamente.");

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/pedidos/" + pedidoId + "/pago";
    }
}
