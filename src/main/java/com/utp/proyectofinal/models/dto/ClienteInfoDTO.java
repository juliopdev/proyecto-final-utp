package com.utp.proyectofinal.models.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.utp.proyectofinal.models.documents.ClienteInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteInfoDTO {
    
    private String id;
    private String idCliente;
    private List<ComentarioGeneralDTO> comentariosGenerales;
    private PreferenciasDTO preferencias;
    private LocalDateTime fechaCreacion;
    
    // Constructor desde Document
    public ClienteInfoDTO(ClienteInfo clienteInfo) {
        this.id = clienteInfo.getId() != null ? clienteInfo.getId().toString() : null;
        this.idCliente = clienteInfo.getIdCliente();
        this.fechaCreacion = clienteInfo.getFechaCreacion();
        
        if (clienteInfo.getComentariosGenerales() != null) {
            this.comentariosGenerales = clienteInfo.getComentariosGenerales()
                .stream()
                .map(ComentarioGeneralDTO::new)
                .collect(Collectors.toList());
        }
        
        if (clienteInfo.getPreferencias() != null) {
            this.preferencias = new PreferenciasDTO(clienteInfo.getPreferencias());
        }
    }
    
    public ClienteInfo toDocument() {
        ClienteInfo doc = new ClienteInfo();
        doc.setIdCliente(this.idCliente);
        
        if (this.comentariosGenerales != null) {
            doc.setComentariosGenerales(
                this.comentariosGenerales.stream()
                    .map(ComentarioGeneralDTO::toDocument)
                    .collect(Collectors.toList())
            );
        }
        
        if (this.preferencias != null) {
            doc.setPreferencias(this.preferencias.toDocument());
        }
        
        return doc;
    }
    
    // DTOs internos
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComentarioGeneralDTO {
        private String texto;
        private LocalDateTime fecha;
        private int puntuacion;
        
        public ComentarioGeneralDTO(ClienteInfo.ComentarioGeneral comentario) {
            this.texto = comentario.getTexto();
            this.fecha = comentario.getFecha();
            this.puntuacion = comentario.getPuntuacion();
        }
        
        public ClienteInfo.ComentarioGeneral toDocument() {
            return new ClienteInfo.ComentarioGeneral(texto, fecha, puntuacion);
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferenciasDTO {
        private String idioma;
        private String metodoPagoPreferido;
        private String tipoEntregaPreferido;
        private boolean notificaciones;
        
        public PreferenciasDTO(ClienteInfo.Preferencias preferencias) {
            this.idioma = preferencias.getIdioma();
            this.metodoPagoPreferido = preferencias.getMetodoPagoPreferido();
            this.tipoEntregaPreferido = preferencias.getTipoEntregaPreferido();
            this.notificaciones = preferencias.isNotificaciones();
        }
        
        public ClienteInfo.Preferencias toDocument() {
            return new ClienteInfo.Preferencias(idioma, metodoPagoPreferido, tipoEntregaPreferido, notificaciones);
        }
    }
}