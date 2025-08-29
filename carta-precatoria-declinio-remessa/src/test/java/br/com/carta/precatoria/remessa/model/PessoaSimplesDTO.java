package br.com.carta.precatoria.remessa.model;

import java.io.Serializable;
import java.util.UUID;

public record PessoaSimplesDTO(UUID uuid,
                               String nome,
                               String documentoIdentificacaoPrincipalValor,
                               String documentoIdentificacaoPrincipalTipo,
                               String justificativaAusenciaDocumentoIdentificacao) {
}
