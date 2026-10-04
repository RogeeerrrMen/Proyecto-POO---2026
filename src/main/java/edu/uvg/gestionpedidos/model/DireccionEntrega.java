package edu.uvg.gestionpedidos.model;

/**
 * Almacena la información que se compartirá para realizar la entrega.
 */
public class DireccionEntrega {

    private Long id;
    private String nombreReceptor;
    private String telefonoReceptor;
    private String direccion;
    private String referencias;
    private String enlaceMapa;

    public DireccionEntrega() {
    }

    public DireccionEntrega(String nombreReceptor, String telefonoReceptor, String direccion,
                             String referencias, String enlaceMapa) {
        this.nombreReceptor = nombreReceptor;
        this.telefonoReceptor = telefonoReceptor;
        this.direccion = direccion;
        this.referencias = referencias;
        this.enlaceMapa = enlaceMapa;
        validarDatos();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreReceptor() {
        return nombreReceptor;
    }

    public String getTelefonoReceptor() {
        return telefonoReceptor;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getReferencias() {
        return referencias;
    }

    public String getEnlaceMapa() {
        return enlaceMapa;
    }

    /**
     * Modifica los datos de entrega y vuelve a validar los campos obligatorios.
     */
    public void actualizarDatos(String nombreReceptor, String telefonoReceptor, String direccion,
                                 String referencias, String enlaceMapa) {
        this.nombreReceptor = nombreReceptor;
        this.telefonoReceptor = telefonoReceptor;
        this.direccion = direccion;
        this.referencias = referencias;
        this.enlaceMapa = enlaceMapa;
        validarDatos();
    }

    /**
     * Verifica que los campos obligatorios (receptor, teléfono y dirección) estén presentes.
     */
    public boolean validarDatos() {
        if (nombreReceptor == null || nombreReceptor.isBlank()
                || telefonoReceptor == null || telefonoReceptor.isBlank()
                || direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("Nombre, teléfono y dirección del receptor son obligatorios.");
        }
        return true;
    }

    /**
     * Devuelve el enlace de Google Maps registrado.
     */
    public String obtenerEnlaceMapa() {
        return enlaceMapa;
    }
}
