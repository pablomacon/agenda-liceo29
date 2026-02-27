package com.liceo.agenda.service;

import com.liceo.agenda.model.Usuario;
import com.liceo.agenda.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${ADMIN_USER}")
    private String adminUser;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Validación del Administrador (Variables de entorno)
        if (username.equals(adminUser)) {
            return User.builder()
                    .username(adminUser)
                    .password(adminPassword)
                    .roles("ADMIN")
                    .build();
        }

        // 2. Validación de Usuarios de la Base de Datos
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no habilitado: " + username));

        String hashBD = usuario.getPassword();

        // 3. Ajuste de prefijo para el Hash
        // Si el hash no tiene el prefijo de algoritmo, le añadimos {bcrypt}
        // para que el DelegatingPasswordEncoder sepa que debe usar ese validador.
        if (hashBD != null && !hashBD.startsWith("{")) {
            hashBD = "{bcrypt}" + hashBD;
        }

        return User.builder()
                .username(usuario.getEmail())
                .password(hashBD)
                .roles(usuario.getRol() != null ? usuario.getRol() : "DOCENTE")
                .disabled(!usuario.isActivo())
                .build();
    }
}