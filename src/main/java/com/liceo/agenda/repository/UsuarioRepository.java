package com.liceo.agenda.repository;

import com.liceo.agenda.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Este método es el que usará la seguridad para buscar al docente por su email
    Optional<Usuario> findByEmail(String email);
}