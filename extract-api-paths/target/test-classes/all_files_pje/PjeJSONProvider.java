package br.jus.cnj.pje.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.infox.cliente.util.JSONUtil;

@Provider
@Consumes(MediaType.WILDCARD) // NOTE: required to support "non-standard" JSON variants
@Produces(MediaType.WILDCARD)
public class PjeJSONProvider implements ContextResolver<ObjectMapper>{
	
    private final ObjectMapper objectMapper;

    public PjeJSONProvider()
    {
        objectMapper = this.getObjectMapper();
    }

    @Override
    public ObjectMapper getContext(Class<?> objectType)

    {
        return objectMapper;
    }	

    public ObjectMapper getObjectMapper(){
    	return JSONUtil.novoObjectMapper();
    }
}
