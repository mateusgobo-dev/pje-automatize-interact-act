package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ComplementoProcessoJEDAO;
import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;

@Name(ComplementoProcessoJEManager.NAME)
public class ComplementoProcessoJEManager extends BaseManager<ComplementoProcessoJE> {
	
	public static final String NAME = "complementoProcessoJEManager";
	
	@In
	private ComplementoProcessoJEDAO complementoProcessoJEDAO;
	
	@Override
	protected ComplementoProcessoJEDAO getDAO() {
		return complementoProcessoJEDAO;
	}
	
	public static ComplementoProcessoJEManager instance() {
		return ComponentUtil.getComponent(ComplementoProcessoJEManager.class);
	}
	
	/**
	 * Metodo que verifica se a cadeia possui um processo paradigma
	 * 
	 * @param vinculacao
	 * @return True se existir o processo paradigma para a vinculacao informada via parametro
	 */
	public boolean isParadigmaExistente(VinculacaoDependenciaEleitoral vinculacao) {
		return getDAO().isParadigmaExistente(vinculacao);
	}
	
	/**
	 * Metodo que recupera o primeiro complemento processual de uma cadeia.
	 * 
	 * @param vinculacao
	 * @return ComplementoJE
	 */
	public ComplementoProcessoJE recuperarComplementoParadigma(VinculacaoDependenciaEleitoral vinculacao) {
		return getDAO().recuperarComplementoParadigma(vinculacao);
	}
	
	public ComplementoProcessoJE definirComplementoProcessoJe(ProcessoTrf processoTrf, Eleicao eleicao, Municipio mucipioJurisdicao) {
		ComplementoProcessoJE complementoProcessoJE = processoTrf.getComplementoJE();

		if (complementoProcessoJE == null) {
			complementoProcessoJE = new ComplementoProcessoJE();
		}
		complementoProcessoJE.setProcessoTrf(processoTrf);
		complementoProcessoJE.setDtAtualizacao(new Date());
		complementoProcessoJE.setEstadoEleicao(mucipioJurisdicao.getEstado());
		complementoProcessoJE.setMunicipioEleicao(mucipioJurisdicao);
		complementoProcessoJE.setEleicao(eleicao);
		processoTrf.setComplementoJE(complementoProcessoJE);

		return complementoProcessoJE;
	}
}