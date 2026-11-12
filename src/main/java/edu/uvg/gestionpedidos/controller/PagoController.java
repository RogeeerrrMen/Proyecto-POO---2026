package edu.uvg.gestionpedidos.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import edu.uvg.gestionpedidos.dto.ResumenPagoDTO;
import edu.uvg.gestionpedidos.entity.MetodoPago;
import edu.uvg.gestionpedidos.service.PagoService;
import edu.uvg.gestionpedidos.service.PlanPagoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PagoController {
    private final PagoService pagoService;
    private final PlanPagoService planPagoService;

    public PagoController(PagoService pagoService, PlanPagoService planPagoService) {
        this.pagoService = pagoService;
        this.planPagoService = planPagoService;
    }

    @GetMapping("/pedidos/{pedidoId}/pago")
    public String mostrarPago(@PathVariable Long pedidoId, Principal principal,
            Model model, RedirectAttributes mensajes) {
        try {
            // Resuelve el plan y comprueba la propiedad antes de mostrar los importes.
            model.addAttribute("planPago", planPagoService.consultar(pedidoId, principal.getName()));
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
    public String registrarAbono(@PathVariable Long pedidoId,
            @RequestParam(required = false) String monto,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) String partePagoId,
            Principal principal, RedirectAttributes mensajes) {
        try {
            pagoService.registrarAbono(pedidoId, convertirMonto(monto), convertirMetodo(metodoPago),
                    referencia, convertirParteId(partePagoId), principal.getName());
            mensajes.addFlashAttribute("mensaje", "Abono registrado correctamente.");
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            mensajes.addFlashAttribute("montoIngresado", monto);
            mensajes.addFlashAttribute("metodoPagoSeleccionado", metodoPago);
            mensajes.addFlashAttribute("referenciaIngresada", referencia);
            mensajes.addFlashAttribute("partePagoSeleccionada", partePagoId);
        }
        return volver(pedidoId);
    }

    @PostMapping("/pedidos/{pedidoId}/pago/plan/cuotas")
    public String crearCuotas(@PathVariable Long pedidoId, @RequestParam(required = false) String cantidadCuotas, @RequestParam(required = false) String primerVencimiento,
            Principal principal, RedirectAttributes mensajes) {
        try {
            Integer cantidad;
            try { cantidad = Integer.valueOf(cantidadCuotas == null ? "" : cantidadCuotas.trim()); }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Indica una cantidad válida de cuotas.");
            }
            LocalDate fecha;
            try {
                if (primerVencimiento == null || !primerVencimiento.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                    throw new IllegalArgumentException("Indica una fecha válida para la primera cuota.");
                }
                fecha = LocalDate.parse(primerVencimiento);
                if (fecha.getYear() < 1) {
                    throw new IllegalArgumentException("Indica una fecha válida para la primera cuota.");
                }
            } catch (DateTimeException e) {
                throw new IllegalArgumentException("Indica una fecha válida para la primera cuota.");
            }
            planPagoService.crearCuotas(pedidoId, cantidad, fecha, principal.getName());
            mensajes.addFlashAttribute("mensaje", "Plan de cuotas guardado. Los cobros se registran por separado.");
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            mensajes.addFlashAttribute("cantidadCuotasIngresada", cantidadCuotas);
            mensajes.addFlashAttribute("primerVencimientoIngresado", primerVencimiento);
        }
        return volver(pedidoId);
    }

    @PostMapping("/pedidos/{pedidoId}/pago/plan/dividido")
    public String crearDividido(@PathVariable Long pedidoId, @RequestParam MultiValueMap<String, String> formulario,
            Principal principal, RedirectAttributes mensajes) {
        List<String> personas = formulario.getOrDefault("personas", List.of());
        List<String> valores = formulario.getOrDefault("montos", List.of());
        try {
            if (personas.size() > 60 || valores.size() > 60) {
                throw new IllegalArgumentException("Puedes asignar como máximo 60 personas.");
            }
            List<BigDecimal> montos = valores.stream().map(this::convertirMonto).toList();
            planPagoService.crearDividido(pedidoId, personas, montos, principal.getName());
            mensajes.addFlashAttribute("mensaje", "Distribución guardada. Los cobros se registran por separado.");
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            mensajes.addFlashAttribute("personasIngresadas", personas);
            mensajes.addFlashAttribute("montosAsignados", valores);
        }
        return volver(pedidoId);
    }

    private String volver(Long pedidoId) { return "redirect:/pedidos/" + pedidoId + "/pago"; }

    private BigDecimal convertirMonto(String monto) {
        if (monto == null || monto.isBlank()) {
            throw new IllegalArgumentException("Debes ingresar un monto.");
        }
        try { return new BigDecimal(monto.trim()); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("El monto debe ser un número válido.");
        }
    }

    private Long convertirParteId(String texto) {
        if (texto == null || texto.isBlank()) { return null; }
        try {
            Long id = Long.valueOf(texto.trim());
            if (id <= 0) { throw new IllegalArgumentException("Selecciona una cuota o persona válida."); }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Selecciona una cuota o persona válida.");
        }
    }

    private MetodoPago convertirMetodo(String metodo) {
        if (metodo == null || metodo.isBlank()) {
            throw new IllegalArgumentException("Debes seleccionar un método de pago.");
        }
        try { return MetodoPago.valueOf(metodo.trim()); }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El método de pago seleccionado no es válido.");
        }
    }
}
