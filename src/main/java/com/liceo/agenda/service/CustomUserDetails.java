package com.liceo.agenda.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.liceo.agenda.model.Usuario;

public class CustomUserDetails implements OAuth2User, Serializable {
    private Usuario usuario; // Tu entidad de la BD
    private Map<String, Object> attributes;

    public CustomUserDetails(Usuario usuario, Map<String, Object> attributes) {
        this.usuario = usuario;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()));
    }

    // ESTO ES LO QUE ARREGLA EL NOMBRE EN EL INDEX
    @Override
    public String getName() { return usuario.getEmail(); }

    // ESTO ES LO QUE ARREGLA EL BORRADO DE EVENTOS
    public Long getId() { return usuario.getId(); }
}
