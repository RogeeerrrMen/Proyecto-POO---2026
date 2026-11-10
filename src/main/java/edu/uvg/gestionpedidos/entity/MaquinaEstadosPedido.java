package edu.uvg.gestionpedidos.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class MaquinaEstadosPedido {

    private MaquinaEstadosPedido() {
    }

    public static List<EstadoPedido> flujoPredeterminado() {
        return List.of(
                EstadoPedido.REGISTRADO,
                EstadoPedido.CONFIRMADO,
                EstadoPedido.EN_PREPARACION,
                EstadoPedido.LISTO,
                EstadoPedido.ENVIADO,
                EstadoPedido.ENTREGADO
        );
    }

    public static List<EstadoPedido> validarFlujo(
            List<EstadoPedido> estados) {

        if (estados == null || estados.size() < 3) {
            throw new IllegalArgumentException("El flujo debe incluir al menos " + "REGISTRADO, ENVIADO y ENTREGADO.");
        }

        EnumSet<EstadoPedido> encontrados =
                EnumSet.noneOf(EstadoPedido.class);

        for (EstadoPedido estado : estados) {
            if (estado == null) {
                throw new IllegalArgumentException("El flujo contiene un estado vacío.");
            }

            if (!encontrados.add(estado)) {
                throw new IllegalArgumentException("No puedes repetir el estado " + estado + ".");
            }

            if (estado == EstadoPedido.CANCELADO
                    || estado == EstadoPedido.FALLIDO) {
                throw new IllegalArgumentException("CANCELADO y FALLIDO son salidas alternativas " + "y no forman parte de la secuencia.");
            }
        }

        if (estados.get(0) != EstadoPedido.REGISTRADO) {
            throw new IllegalArgumentException("El primer estado debe ser REGISTRADO.");
        }

        if (estados.get(estados.size() - 1)
                != EstadoPedido.ENTREGADO) {
            throw new IllegalArgumentException("El último estado debe ser ENTREGADO.");
        }

        if (estados.get(estados.size() - 2)
                != EstadoPedido.ENVIADO) {
            throw new IllegalArgumentException("ENVIADO debe estar justo antes de ENTREGADO.");
        }

        return List.copyOf(estados);
    }

    public static List<EstadoPedido> siguientes(EstadoPedido actual, ModalidadEntrega modalidad, List<EstadoPedido> flujo) {

        if (actual == null) {
            throw new IllegalArgumentException("El estado actual es obligatorio.");
        }

        if (modalidad == null) {
            throw new IllegalArgumentException("La modalidad de entrega es obligatoria.");
        }

        List<EstadoPedido> flujoValidado = validarFlujo(flujo);

        if (esFinal(actual)) {
            return List.of();
        }

        List<EstadoPedido> secuencia = new ArrayList<>();

        for (EstadoPedido estado : flujoValidado) {
            if (modalidad == ModalidadEntrega.RECOGER_NEGOCIO && estado == EstadoPedido.ENVIADO) {
                continue;
            }

            secuencia.add(estado);
        }

        int posicion = secuencia.indexOf(actual);

        if (posicion == -1) {
            throw new IllegalArgumentException("El estado actual no pertenece al flujo del pedido.");
        }

        List<EstadoPedido> permitidos = new ArrayList<>();

        if (posicion + 1 < secuencia.size()) {
            permitidos.add(secuencia.get(posicion + 1));
        }

        if (actual == EstadoPedido.ENVIADO) {
            permitidos.add(EstadoPedido.FALLIDO);
        } else {
            permitidos.add(EstadoPedido.CANCELADO);
        }

        return List.copyOf(permitidos);
    }

    public static boolean esFinal(EstadoPedido estado) {
        return estado == EstadoPedido.ENTREGADO
                || estado == EstadoPedido.CANCELADO
                || estado == EstadoPedido.FALLIDO;
    }
}