package br.com.carta.precatoria.remessa.model;

import java.io.Serializable;

public record ServicoDTO(String id,
                         String nome,
                         String identificadorGrupo,
                         String url,
                         String versao) {}
