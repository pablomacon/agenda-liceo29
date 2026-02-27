package com.liceo.agenda.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("start")
    @Column(name = "inicio") // Aseguramos que apunte a 'inicio'
    private LocalDateTime fechaInicio;

    @JsonProperty("end")
    @Column(name = "fin") // Aseguramos que apunte a 'fin'
    private LocalDateTime fechaFin;

    @JsonProperty("title")
    private String titulo;

    private String ambito; // Antes 'tipo' (GRUPO, TALLER, etc.)
    private String tipo; // Duplicamos o usamos para color en FullCalendar
    private String destinatarios; // Aquí guardamos los grupos o "General"
    private String turno;
    private Integer cantidadEstudiantes;
    private boolean participanTodos;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario creador;

    // Constructores
    public Evento() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // Para fechaInicio
    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    // Para fechaFin
    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        this.ambito = ambito;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDestinatarios() {
        return destinatarios;
    }

    public void setDestinatarios(String destinatarios) {
        this.destinatarios = destinatarios;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public Integer getCantidadEstudiantes() {
        return cantidadEstudiantes;
    }

    public void setCantidadEstudiantes(Integer cantidadEstudiantes) {
        this.cantidadEstudiantes = cantidadEstudiantes;
    }

    public boolean isParticipanTodos() {
        return participanTodos;
    }

    public void setParticipanTodos(boolean participanTodos) {
        this.participanTodos = participanTodos;
    }

    public Usuario getCreador() {
        return creador;
    }

    public void setCreador(Usuario creador) {
        this.creador = creador;
    }
}