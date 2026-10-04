package edu.uvg.gestionpedidos.model;

/**
 * Relaciona un producto con un pedido y guarda su cantidad, precio y subtotal.
 */
public class DetallePedido {

    private Long id;
    private Producto producto;
    private int cantidad;
    private double precioUnitario;

    public DetallePedido() {
    }

    /**
     * El precio unitario se congela al momento de crear el detalle,
     * para que cambios futuros en el precio del producto no afecten pedidos ya hechos.
     */
    public DetallePedido(Producto producto, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    /**
     * Multiplica la cantidad por el precio unitario.
     */
    public double calcularSubtotal() {
        return precioUnitario * cantidad;
    }

    /**
     * Modifica la cantidad solicitada.
     */
    public void cambiarCantidad(int nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        this.cantidad = nuevaCantidad;
    }
}
