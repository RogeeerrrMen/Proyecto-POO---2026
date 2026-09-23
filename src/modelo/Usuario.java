package modelo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Representa la cuenta del emprendedor.
 */
public class Usuario {

    private Long id;
    private String nombre;
    private String correo;
    private String passwordHash;
    private boolean activo;

    public Usuario() {
    }

    /**
     * Crea un usuario nuevo, activo por defecto, guardando únicamente
     * el hash de la contraseña recibida.
     */
    public Usuario(String nombre, String correo, String password) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = generarHash(password);
        this.activo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActivo() {
        return activo;
    }

    /**
     * Cambia la contraseña, guardando solamente su hash.
     */
    public void cambiarPassword(String nuevaPassword) {
        if (nuevaPassword == null || nuevaPassword.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        this.passwordHash = generarHash(nuevaPassword);
    }

    /**
     * Activa la cuenta.
     */
    public void activar() {
        this.activo = true;
    }

    /**
     * Desactiva la cuenta.
     */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * Genera el hash de una contraseña en texto plano.
     * NOTA: se usa SHA-256 como implementación provisional mientras el
     * equipo decide la tecnología de persistencia y sus dependencias.
     * Cuando elijan, se puede reemplazar internamente por BCrypt (u otro)
     * sin cambiar la firma pública de la clase.
     */
    private String generarHash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No se pudo generar el hash de la contraseña.", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        if (id == null || usuario.id == null) return false;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
