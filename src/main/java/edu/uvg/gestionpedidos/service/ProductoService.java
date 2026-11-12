package edu.uvg.gestionpedidos.service;

import edu.uvg.gestionpedidos.dto.ProductoDTO;
import edu.uvg.gestionpedidos.entity.Producto;
import edu.uvg.gestionpedidos.repository.ProductoRepository;
import edu.uvg.gestionpedidos.entity.Emprendimiento;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

//Coordina los casos de uso de productos: registrar, modificar, consultar,buscar 
//y activar/desactivar.

//Errores que puede lanzar:
//- IllegalArgumentException: los datos recibidos no son válidos.
//- NoSuchElementException: el producto que se indica no existe.
 
@Service
public class ProductoService {

    // Deben coincidir con el largo de las columnas definido en la entidad Producto.
    static final int NOMBRE_MAX = 120;
    static final int DESCRIPCION_MAX = 500;

    private final ProductoRepository productoRepository;
    private final EmprendimientoService emprendimientoService;

    public ProductoService(ProductoRepository productoRepository,EmprendimientoService emprendimientoService) {

        this.productoRepository = productoRepository;
        this.emprendimientoService = emprendimientoService;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {

        Long emprendimientoId = obtenerEmprendimientoActual().getId();

        return productoRepository.findByEmprendimientoIdOrderByIdAsc(emprendimientoId);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductosActivos() {

        Long emprendimientoId =
                obtenerEmprendimientoActual().getId();

        return productoRepository
                .findByEmprendimientoIdAndActivoTrueOrderByIdAsc(
                    emprendimientoId
                );
    }

    
    @Transactional(readOnly = true)
    public Optional<Producto> buscarProductoPorId(Long id) {

        Long emprendimientoId =
                obtenerEmprendimientoActual().getId();

        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return productoRepository.findByIdAndEmprendimientoId(
                id,
                emprendimientoId
        );
    }


    // Busca por coincidencia parcial del nombre
   @Transactional(readOnly = true)
    public List<Producto> buscarProductosPorNombre(String texto) {

        if (texto == null || texto.isBlank()) {
            return listarProductos();
        }

        Long emprendimientoId =
                obtenerEmprendimientoActual().getId();

        return productoRepository
                .findByEmprendimientoIdAndNombreContainingIgnoreCaseOrderByIdAsc(
                    emprendimientoId,
                    texto.trim()
                );
    }

    //Registra un producto nuevo. Todo producto nuevo queda activo.
    @Transactional
    public Producto registrarProducto(ProductoDTO datos) {
        validar(datos);

        Emprendimiento emprendimiento =
                obtenerEmprendimientoActual();

        Producto producto = new Producto();
        producto.asignarEmprendimiento(emprendimiento);
        producto.actualizarPrecio(datos.getPrecio());
        aplicarDatos(producto, datos);
        producto.activar();

        return productoRepository.save(producto);
    }

    //Modifica nombre, descripción y precio de un producto existente. No cambia su estado.
    @Transactional
    public Producto modificarProducto(Long id, ProductoDTO datos) {
        Producto producto = obtener(id);
        validar(datos);

        // El precio va primero: si no es válido, falla antes de modificar cualquier otro dato.
        producto.actualizarPrecio(datos.getPrecio());
        aplicarDatos(producto, datos);
        return productoRepository.save(producto);
    }

    //Vuelve a habilitar un producto desactivado.
    @Transactional
    public Producto activarProducto(Long id) {
        Producto producto = obtener(id);
        producto.activar();
        return productoRepository.save(producto);
    }

    // Deshabilita un producto, pero se elimina
    @Transactional
    public Producto desactivarProducto(Long id) {
        Producto producto = obtener(id);
        producto.desactivar();
        return productoRepository.save(producto);
    }

    
    private Producto obtener(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                "El identificador del producto no es válido."
            );
        }

        Long emprendimientoId =
                obtenerEmprendimientoActual().getId();

        return productoRepository
                .findByIdAndEmprendimientoId(id, emprendimientoId)
                .orElseThrow(() -> new NoSuchElementException(
                    "El producto no existe o no está disponible."
                ));
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


    private String obtenerCorreoAutenticado() {

        var autenticacion = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || "anonymousUser".equals(
                    autenticacion.getPrincipal())) {

            throw new AccessDeniedException(
                "Debes iniciar sesión."
            );
        }

        return autenticacion.getName();
    }

    private Emprendimiento obtenerEmprendimientoActual() {

        return emprendimientoService
                .buscarMiEmprendimiento(
                    obtenerCorreoAutenticado()
                )
                .orElseThrow(() -> new AccessDeniedException(
                    "Debes registrar tu emprendimiento."
                ));
    }

}
