package br.com.carta.precatoria.remessa.model;

import java.io.Serializable;

public record ModeloEventoDTO(String id,
                              String nome,
                              String descricao,
                              String servicoId,
                              String grupoServicoId,
                              String modelo) {
}
