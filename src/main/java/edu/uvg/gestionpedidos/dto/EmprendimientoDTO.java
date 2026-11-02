package edu.uvg.gestionpedidos.dto;

import edu.uvg.gestionpedidos.entity.Emprendimiento;

public class EmprendimientoDTO {

    private String nombre;

    public EmprendimientoDTO() {
    }

    //Reune los datos para el formulario de edición
    public static EmprendimientoDTO desde(
            Emprendimiento emprendimiento
    ) {
        EmprendimientoDTO dto = new EmprendimientoDTO();
        dto.setNombre(emprendimiento.getNombre());
        return dto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}