package br.com.carta.precatoria.remessa.model;

import java.io.Serializable;

public record MensagemDTO(String protocolo, String url, String callback, Integer notificacaoId)  {
}
