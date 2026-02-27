package com.liceo.agenda.controller;

import com.liceo.agenda.model.Evento;
import com.liceo.agenda.model.Usuario;
import com.liceo.agenda.repository.EventoRepository;
import com.liceo.agenda.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * VISTA PRINCIPAL (Calendario)
     * Mapeado a "/" para que sea lo primero que veas al entrar o loguearte.
     */
    @GetMapping("/")
    public String index(Model model) {
        // Cargamos los eventos para que Thymeleaf los tenga disponibles
        model.addAttribute("eventos", eventoRepository.findAll());
        return "index";
    }

    /**
     * FORMULARIO DE NUEVO EVENTO
     */
    @GetMapping("/eventos/nuevo")
    public String mostrarFormulario(Model model) {
        Evento evento = new Evento();
        evento.setAmbito("GRUPO");
        model.addAttribute("evento", evento);
        return "nuevo";
    }

    /**
     * GUARDAR EVENTO
     */
    @PostMapping("/eventos/guardar")
    public String guardarEvento(
            @ModelAttribute("evento") Evento evento,
            @RequestParam(value = "gruposArr", required = false) List<String> gruposArr,
            @RequestParam(value = "inputOtros", required = false) String inputOtros,
            Principal principal) {

        // 1. Asignar el creador desde el login
        if (principal != null) {
            String emailLogueado = principal.getName();
            Usuario docente = usuarioRepository.findByEmail(emailLogueado)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            evento.setCreador(docente);
        }

        // 2. Lógica de destinatarios
        if ("GRUPO".equals(evento.getAmbito())) {
            if (gruposArr != null && !gruposArr.isEmpty()) {
                evento.setDestinatarios(String.join(", ", gruposArr));
            } else {
                evento.setDestinatarios("Sin grupos seleccionados");
            }
        } else {
            evento.setDestinatarios(inputOtros != null && !inputOtros.isEmpty() ? inputOtros : "General");
        }

        // 3. Sincronizar tipo y turno
        evento.setTipo(evento.getAmbito());
        if (evento.getDestinatarios() != null) {
            String dest = evento.getDestinatarios();
            if (dest.matches(".*(7°[1-3]|8°[1-3]|9°[1-4]).*")) {
                evento.setTurno("MATUTINO");
            } else {
                evento.setTurno("VESPERTINO");
            }
        }

        eventoRepository.save(evento);
        return "redirect:/"; // Redirige a la raíz donde está el calendario
    }

    /**
     * API JSON para FullCalendar
     */
    /**
     * API JSON para FullCalendar
     * Ajustado a la estructura real de Evento.java
     */
    @GetMapping("/api/eventos")
    @ResponseBody
    public List<Map<String, Object>> listarEventos() {
        return eventoRepository.findAll().stream().map(evento -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", evento.getId());
            map.put("title", evento.getTitulo());
            map.put("start", evento.getFechaInicio());
            map.put("end", evento.getFechaFin());

            Map<String, Object> props = new HashMap<>();
            props.put("destinatarios", evento.getDestinatarios());
            props.put("ambito", evento.getAmbito());

            if (evento.getCreador() != null) {
                props.put("usuarioId", evento.getCreador().getId());

                // --- CONCATENAMOS NOMBRE Y APELLIDO ---
                String nombre = evento.getCreador().getNombre() != null ? evento.getCreador().getNombre() : "";
                String apellido = evento.getCreador().getApellido() != null ? evento.getCreador().getApellido() : "";

                String nombreCompleto = (nombre + " " + apellido).trim();

                // Si por alguna razón ambos están vacíos, usamos el email como respaldo
                if (nombreCompleto.isEmpty()) {
                    nombreCompleto = evento.getCreador().getEmail();
                }

                props.put("usuarioNombre", nombreCompleto);
            } else {
                props.put("usuarioId", null);
                props.put("usuarioNombre", "No disponible");
            }

            map.put("extendedProps", props);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * ELIMINAR EVENTO
     * Solo permitido para el creador del evento o un administrador.
     */
    @PostMapping("/eventos/eliminar/{id}")
    public String eliminarEvento(@PathVariable Long id, Principal principal) {
        if (principal == null)
            return "redirect:/login";

        try {
            Evento evento = eventoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

            Usuario usuarioLogueado = usuarioRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            boolean esAdmin = "ADMIN".equalsIgnoreCase(usuarioLogueado.getRol());
            boolean esDuenio = evento.getCreador().getId().equals(usuarioLogueado.getId());

            if (esAdmin || esDuenio) {
                eventoRepository.delete(evento);
                System.out.println("Eliminado con éxito ID: " + id);
            }
        } catch (Exception e) {
            System.err.println("Error al eliminar: " + e.getMessage());
        }
        return "redirect:/";
    }

    /**
     * API para verificar colisiones antes de guardar.
     * Devuelve una lista de mensajes si encuentra conflictos.
     */
    @GetMapping("/api/eventos/check-conflictos")
    @ResponseBody
    public List<String> checkConflictos(
            @RequestParam("grupos") String gruposRaw, // Recibimos como String para manejarlo mejor
            @RequestParam("inicio") String inicioStr,
            @RequestParam("fin") String finStr) {

        List<String> mensajes = new ArrayList<>();
        try {
            // Manejamos la posible "T" que envía el input datetime-local
            LocalDateTime inicio = LocalDateTime
                    .parse(inicioStr.contains("T") ? inicioStr : inicioStr.replace(" ", "T"));
            LocalDateTime fin = LocalDateTime.parse(finStr.contains("T") ? finStr : finStr.replace(" ", "T"));

            String[] grupos = gruposRaw.split(",");

            for (String grupo : grupos) {
                String patron = "%" + grupo.trim() + "%";
                // Ahora sí, busca en las columnas correctas unificadas
                List<Evento> conflictos = eventoRepository.buscarConflictos(patron, inicio, fin);

                if (!conflictos.isEmpty()) {
                    mensajes.add("El grupo " + grupo + " ya tiene: " + conflictos.get(0).getTitulo());
                }
            }
        } catch (Exception e) {
            System.err.println("Error validando: " + e.getMessage());
        }
        return mensajes;
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Esto busca templates/login.html
    }
}