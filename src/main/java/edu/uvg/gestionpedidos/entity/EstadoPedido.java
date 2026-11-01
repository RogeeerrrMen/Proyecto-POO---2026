package edu.uvg.gestionpedidos.entity;

import java.util.List;

public enum EstadoPedido {
    REGISTRADO,
    CONFIRMADO,
    EN_PREPARACION,
    LISTO,
    ENVIADO,
    ENTREGADO,
    FALLIDO,
    CANCELADO;

    public List<EstadoPedido> getEstadosPermitidos() {
        return switch (this) {
            case REGISTRADO -> List.of(CONFIRMADO, CANCELADO);
            case CONFIRMADO -> List.of(EN_PREPARACION, CANCELADO);
            case EN_PREPARACION -> List.of(LISTO);
            case LISTO -> List.of(ENVIADO);
            case ENVIADO -> List.of(ENTREGADO, FALLIDO);
            case ENTREGADO, FALLIDO, CANCELADO -> List.of();
        };
    }

    public boolean puedeCambiarA(EstadoPedido nuevoEstado) {
        return nuevoEstado != null && getEstadosPermitidos().contains(nuevoEstado);
    }
}