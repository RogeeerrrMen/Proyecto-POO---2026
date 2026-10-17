package edu.uvg.gestionpedidos.dto;
import edu.uvg.gestionpedidos.entity.Gasto;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

//Mueve los datos entre el formulario y el servicio.
public class GastoDTO {

    private String descripcion;
    private BigDecimal monto;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    public GastoDTO() {
    }

    //Copia los datos de un gasto para llenar el formulario de edición.
    public static GastoDTO desde(Gasto gasto) {
        GastoDTO dto = new GastoDTO();
        dto.setDescripcion(gasto.getDescripcion());
        dto.setMonto(gasto.getMonto());
        dto.setFecha(gasto.getFecha());
        return dto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}