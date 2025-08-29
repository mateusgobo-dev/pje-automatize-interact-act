package br.com.carta.precatoria.remessa;

import br.com.carta.precatoria.remessa.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.UUID;

public class CartaPrecatoriaRemessaTest {

    @Test
    public void buildMensagemDTO() throws JsonProcessingException {
        MensagemDTO mensagemDTO = new MensagemDTO(//Alterar protocolo, url e callback para outras cartas
                "20251000000000163",
                "https://portal-interno-api-tribunais.stg.pdpj.jus.br/api/v1/peticoes/por-protocolo/20251000000000163",
                "https://portal-interno-api-tribunais.stg.pdpj.jus.br/api/v1/protocolos/20251000000000163/callback",
                0
        );
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
        String json = writer.writeValueAsString(mensagemDTO);
        System.out.println(json);

        PayloadDTO payloadDTO = new PayloadDTO("RAW",
                "MensagemDTO",
                mensagemDTO,
                "",
                "",
                "",
                "");
        WebhookWrapperMessage webhookWrapperMessage = WebhookWrapperMessage.builder()
                .objeto("carta_precatoria")
                .formato("RAW")
                .criadoEm(new Timestamp(System.currentTimeMillis()))
                .numeroUnicoProcesso("0804867-31.2025.8.19.0031")
                .autorAcao(new PessoaSimplesDTO(UUID.randomUUID(),"32454725878",null,null,"N/D"))
                .origemAcao(new InstanciaTribunalDTO("TJRJ", "819", "JCE", 1, null))
                .payload(payloadDTO).build();
        ObjectMapper mapperWrapper = new ObjectMapper();
        ObjectWriter writerWrapper = mapperWrapper.writerWithDefaultPrettyPrinter();
        String webhookWrapperMessageAsText = writerWrapper.writeValueAsString(webhookWrapperMessage);
        System.out.println(webhookWrapperMessageAsText);
    }
}
