package com.liceo.agenda.service;

import org.springframework.stereotype.Service;

import com.liceo.agenda.repository.UsuarioRepository;
import com.liceo.agenda.model.Usuario;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");

        // 1. Buscamos al usuario en la base de datos por email
        Usuario usuarioDB = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    OAuth2Error oauth2Error = new OAuth2Error("not_in_whitelist");
                    return new OAuth2AuthenticationException(oauth2Error, "No autorizado");
                });

        // 2. Creamos un nuevo objeto de usuario que mezcle los datos de Google
        // con la identidad de nuestra base de datos (ID, Rol, etc.)
        return new CustomUserDetails(usuarioDB, oAuth2User.getAttributes());
    }
}
