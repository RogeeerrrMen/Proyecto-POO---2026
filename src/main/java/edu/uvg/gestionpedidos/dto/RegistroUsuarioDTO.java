package edu.uvg.gestionpedidos.dto;

public class RegistroUsuarioDTO {

    private String nombre;
    private String apellido;
    private String correo;
    private String password;
    private String confirmarPassword;
    private String emprendimiento;

    public RegistroUsuarioDTO() {

    }
    public String getNombre() {

        return nombre;

    }

    public void setNombre(String nombre) {

        this.nombre = nombre;

    }

    public String getApellido() {

        return apellido;

    }

    public void setApellido(String apellido) {

        this.apellido = apellido;

    }

    public String getCorreo() {

        return correo;

    }

    public void setCorreo(String correo) {

        this.correo = correo;

    }

    public String getPassword() {

        return password;

    }

    public void setPassword(String password) {

        this.password = password;

    }

    public String getConfirmarPassword() {

        return confirmarPassword;

    }

    public void setConfirmarPassword(String confirmarPassword) {

        this.confirmarPassword = confirmarPassword;

    }

    public String getEmprendimiento() {

        return emprendimiento;

    }

    public void setEmprendimiento(String emprendimiento) {

        this.emprendimiento = emprendimiento;

    }



    
}