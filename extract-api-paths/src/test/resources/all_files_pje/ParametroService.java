package br.jus.cnj.pje.nucleo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.ApplicationContext;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ParametroDAO;
import br.jus.pje.nucleo.entidades.Parametro;

@Name(ParametroService.NAME)
public class ParametroService extends BaseService implements br.jus.cnj.pje.extensao.servico.ParametroService{
	
	public static final String NAME = "parametroService"; 
	
	private static final String PJENULL = "pje:null";

	@In
	private ParametroDAO parametroDAO;

	@In
	private ApplicationContext applicationContext;
	
	/**
     * @return Instância da classe.
     */
    public static ParametroService instance() {
        return ComponentUtil.getComponent(NAME);
    }
    
	public Parametro findByName(String name){
		return this.parametroDAO.findByName(name);
	}

	public List<Parametro> findMultipleByName(String name){
		return this.parametroDAO.findMultipleByName(name);
	}

	@Transactional
	public synchronized void setValue(String name, String value) {
		if (value==null)
			value = "";
		
		Parametro param = parametroDAO.findByName(name);
		if (param==null) {
			param = new Parametro();
			param.setNomeVariavel(name);
			param.setAtivo(Boolean.TRUE);
			param.setSistema(Boolean.TRUE);
			param.setDescricaoVariavel(name + " - Parâmetro criado automaticamente pelo sistema.");
		} else {
			if (Objects.equals(param.getValorVariavel(), value)) 
				param = null;
		}
		
		if (param!=null) {
			param.setValorVariavel(value);
			parametroDAO.persist(param);
			parametroDAO.flush();
		}
				
		applicationContext.set(name, value);
	}

	public String valueOf(String name){
		String value = (String) applicationContext.get(name);
		if (value == null && !applicationContext.isSet(name)){
			value = this.parametroDAO.valueOf(name);
			if(value == null){
				// FIXME: inserido para evitar repetição de chamadas no banco de dados
				// Modificar o tratamento de parâmetros para deixarem de ser componentes
				// autônomos de aplicação e passarem a fazer parte de um mapa no contexto
				// de aplicação.
				// O applicationContext.set(name, ) não grava o valor do componente se o valor
				// for null.
				value = PJENULL;
				applicationContext.set(name, value);
			}else{
				applicationContext.set(name, value);
			}
		}
		if(value.equals(PJENULL)){
			return null;
		}else{
			return value;
		}
	}

	public List<String> multipleValueOf(String name){
		List<String> ret = new ArrayList<String>();
		String valorIntegral = (String) applicationContext.get(name);
		if (valorIntegral == null){
			ret.addAll(this.parametroDAO.multipleValuesByName(name));
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < ret.size() - 1; i++){
				sb.append(ret.get(i));
				sb.append("]*[");
			}
			sb.append(ret.get(ret.size() - 1));
			applicationContext.set(name, sb.toString());
		}
		else{
			String[] valores = valorIntegral.split("\\]*\\[");
			for (String v : valores){
				ret.add(v);
			}
		}
		return ret;
	}

}
