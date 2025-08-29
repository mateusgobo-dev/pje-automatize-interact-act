package br.com.carta.precatoria.remessa.model;

import java.io.Serializable;

public record InstanciaTribunalDTO(String siglaTribunal,
                                   String JTR,
                                   String codigoSegmentoJustica,
                                   Integer identificadorGrauJurisdicao,
                                   String outrosIdentificadores)  {
}
