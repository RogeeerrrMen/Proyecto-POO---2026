package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.ProductoDTO;
import edu.uvg.gestionpedidos.entity.Producto;
import edu.uvg.gestionpedidos.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Coordina los casos de uso de productos: registrar, modificar, consultar,
 * buscar y activar/desactivar. Las reglas propias del producto (como el precio
 * válido o el estado activo) viven en la entidad {@link Producto}.
 *
 * Errores que puede lanzar:
 * - IllegalArgumentException: los datos recibidos no son válidos.
 * - NoSuchElementException: el producto indicado no existe.
 */
@Service
public class ProductoService {

    /** Deben coincidir con el largo de las columnas definido en la entidad Producto. */
    static final int NOMBRE_MAX = 120;
    static final int DESCRIPCION_MAX = 500;

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductosActivos() {
        return productoRepository.findByActivoTrueOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    /** Busca por coincidencia parcial del nombre; sin texto devuelve todos los productos. */
    @Transactional(readOnly = true)
    public List<Producto> buscarProductosPorNombre(String texto) {
        if (texto == null || texto.isBlank()) {
            return listarProductos();
        }
        return productoRepository.findByNombreContainingIgnoreCaseOrderByIdAsc(texto.trim());
    }

    /** Registra un producto nuevo. Todo producto nuevo queda activo. */
    @Transactional
    public Producto registrarProducto(ProductoDTO datos) {
        validar(datos);

        Producto producto = new Producto();
        producto.actualizarPrecio(datos.getPrecio());
        aplicarDatos(producto, datos);
        producto.activar();
        return productoRepository.save(producto);
    }

    /** Modifica nombre, descripción y precio de un producto existente. No cambia su estado. */
    @Transactional
    public Producto modificarProducto(Long id, ProductoDTO datos) {
        Producto producto = obtener(id);
        validar(datos);

        // El precio va primero: si no es válido, falla antes de modificar cualquier otro dato.
        producto.actualizarPrecio(datos.getPrecio());
        aplicarDatos(producto, datos);
        return productoRepository.save(producto);
    }

    /** Vuelve a habilitar un producto desactivado. */
    @Transactional
    public Producto activarProducto(Long id) {
        Producto producto = obtener(id);
        producto.activar();
        return productoRepository.save(producto);
    }

    /** Deshabilita un producto. No se elimina: sigue guardado y se puede reactivar. */
    @Transactional
    public Producto desactivarProducto(Long id) {
        Producto producto = obtener(id);
        producto.desactivar();
        return productoRepository.save(producto);
    }

    private Producto obtener(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El producto no existe."));
    }

    private void aplicarDatos(Producto producto, ProductoDTO datos) {
        producto.setNombre(datos.getNombre().trim());

        String descripcion = datos.getDescripcion();
        producto.setDescripcion(descripcion == null || descripcion.isBlank() ? null : descripcion.trim());
    }

    private void validar(ProductoDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del producto son obligatorios.");
        }
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (datos.getNombre().trim().length() > NOMBRE_MAX) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los " + NOMBRE_MAX + " caracteres.");
        }
        if (datos.getDescripcion() != null && datos.getDescripcion().trim().length() > DESCRIPCION_MAX) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar los " + DESCRIPCION_MAX + " caracteres.");
        }
        if (datos.getPrecio() == null) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }
    }
}
