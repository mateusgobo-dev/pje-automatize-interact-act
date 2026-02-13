package br.jus.cnj.pje.amqp;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.pje.nucleo.type.CloudEventType;



public class CloudEventTypeTest {
	
    private String json = "{\"appVersion\":\"6.6.6\",\"payload\":{\"numeroProcesso\":\"numero\",\"idProcessoPje\":666,\"idProcessoPartePje\":2,\"idPessoaPje\":1,\"rji\":\"oi\",\"situacaoParte\":\"A\"},\"routingKey\":\"200.2.pje-legacy.ProcessoParteEventDTO.POST\",\"uuid\":\"f7e8edd2-2f19-4f78-8a1e-45af2bb068f3\",\"appName\":\"pje-legacy\",\"timestamp\":1550870590412,\"payloadHash\":\"56724ba322a72d66774ac5d8ea143d04\",\"links\":null}";

    @Test
    public void descerializaJson() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(any())).thenReturn(json);


        CloudEventType cloudType = new CloudEventType();
        CloudEvent resultado = (CloudEvent) cloudType.nullSafeGet(resultSet, new String[]{"windson"}, null, null);
        Assert.assertEquals("200.2.pje-legacy.ProcessoParteEventDTO.POST",resultado.getRoutingKey().toString());
        Assert.assertEquals("f7e8edd2-2f19-4f78-8a1e-45af2bb068f3",resultado.getUuid());
    }

}
