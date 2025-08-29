package br.com.carta.precatoria.remessa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebhookWrapperMessage implements Serializable {

    @Builder.Default
    private String objeto = "";

    @Builder.Default
    private String especificacaoObjeto = "";

    @Builder.Default
    private String formato = "";

    @Builder.Default
    private String eventoId = "";

    @Builder.Default
    private String eventoUrl = "";

    @Builder.Default
    private String notificacaoId = "";

    @Builder.Default
    private ModeloEventoDTO modeloEvento = null;

    @Builder.Default
    private ServicoDTO servico = null;

    @Builder.Default
    private PessoaSimplesDTO autorAcao = null;

    @Builder.Default
    private InstanciaTribunalDTO origemAcao = null;

    @Builder.Default
    private String numeroUnicoProcesso = "";

    @Builder.Default
    private PayloadDTO payload = null;

    @Builder.Default
    private String versao = "";

    @Builder.Default
    private Timestamp criadoEm = null;

    @Builder.Default
    private Map<String, String> links = new HashMap<>();

    private static final long serialVersionUID = 1L;
}
