package edu.uvg.gestionpedidos.model;

/**
 * Estados permitidos para un pedido.
 */
public enum EstadoPedido {
    REGISTRADO,
    CONFIRMADO,
    EN_PREPARACION,
    LISTO,
    ENVIADO,
    ENTREGADO,
    FALLIDO,
    CANCELADO
}
