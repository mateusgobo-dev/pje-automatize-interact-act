package br.com.infox.trf.webservice;

import java.util.HashMap;
import java.util.Map;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoa;

/**
 * Classe responsável por abstrair o tipo de conexão que será utilizada. No momento da criação desta classe existem dois tipos de conexões possíveis:
 * através do proxy provido pelo CNJ (configurado através do valor 'cnj' do parâmetro "tipoConexaoWebService") através do proxy do CJF (configurado
 * através do valor 'cjf' do parâmetro "tipoConexaoWebService")
 * 
 * @author wesleysilva
 * 
 */
@Name(ConsultaClienteWebService.NAME)
@Scope(value = ScopeType.EVENT)
@BypassInterceptors
public class ConsultaClienteWebService{

	private static final String MSG_EXCECAO_WEBSERVICE = "Parâmetro 'tipoConexaoWebService' não configurado corretamente."
		+ " Valores possíveis: cjf ou cnj. Deve-se atentar também para o valor do parâmetro 'urlWsdlReceita'";
	public static final String NAME = "consultaClienteWebService";
	public Map<TipoPessoaEnum, Object> mapClassesConexaoReceita;
	public String tipoConexao;

	public String getTipoConexao(){
		return tipoConexao;
	}

	public void setTipoConexao(String tipoConexao){
		this.tipoConexao = tipoConexao;
	}

	public static ConsultaClienteWebService instance(){
		return ComponentUtil.getComponent(ConsultaClienteWebService.NAME);
	}

	/**
	 * Inicializa as variáveis do componente
	 */
	@Create
	public void init(){
		tipoConexao = ParametroUtil.instance().getTipoConexaoWebService();

		mapClassesConexaoReceita = new HashMap<TipoPessoaEnum, Object>();
		mapClassesConexaoReceita.put(TipoPessoaEnum.F,
				Component.getInstance("consultaClienteReceitaPF" + this.getTipoConexao().toUpperCase()));
		mapClassesConexaoReceita.put(TipoPessoaEnum.J,
				Component.getInstance("consultaClienteReceitaPJ" + this.getTipoConexao().toUpperCase()));
	}

	public DadosReceitaPessoa consultaDados(TipoPessoaEnum tipoPessoa, String nrDocumento, boolean forceUpdate)
			throws Exception{

		validaParametroTipoConexao();

		// No momento da execução caso, por exemplo, o parâmetro
		// "tipoConexaoWebService" esteja configurado com o valor
		// 'cjf' e a consulta for para pessoa física, será chamada a classe
		// ConsultaClienteReceitaPFCJF
		IConsultaClienteReceita consultaClienteReceita = (IConsultaClienteReceita) mapClassesConexaoReceita.get(tipoPessoa);

		if (consultaClienteReceita == null){
			throw new Exception(MSG_EXCECAO_WEBSERVICE);
		}

		return consultaClienteReceita.consultaDados(nrDocumento, forceUpdate);
	}

	private void validaParametroTipoConexao() throws Exception{

		if (tipoConexao == null || tipoConexao.trim().isEmpty()){
			throw new Exception(MSG_EXCECAO_WEBSERVICE);
		}
	}

}
