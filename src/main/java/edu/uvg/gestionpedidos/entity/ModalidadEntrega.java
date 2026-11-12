package edu.uvg.gestionpedidos.entity;

public enum ModalidadEntrega {

    ENVIO_DOMICILIO("Envío a domicilio"),
    RECOGER_NEGOCIO("Pasar a recoger en el negocio");

    private final String descripcion;

    ModalidadEntrega(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}