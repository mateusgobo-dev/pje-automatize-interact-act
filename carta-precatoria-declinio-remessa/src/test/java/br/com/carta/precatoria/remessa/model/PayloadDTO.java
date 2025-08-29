package br.com.carta.precatoria.remessa.model;

import java.io.Serializable;

public record PayloadDTO(String formato,
        String nomeEntidadePayload,
        Object conteudo,
        String algoritmoHash,
        String hash,
        String algoritmoAssinatura,
        String hashAssinado) implements Serializable {
    private static final long serialVersionUID = 1L;
}
