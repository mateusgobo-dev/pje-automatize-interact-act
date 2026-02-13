package br.com.infox.pje.manager;

import java.sql.Time;
import java.util.Date;
import java.util.List;


import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.dao.ProcessoPericiaDAO;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.ProcessoPericia;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.PericiaStatusEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("processoPericiaManager")
@AutoCreate
public class ProcessoPericiaManager extends BaseManager<ProcessoPericia> {

	@In
	private ProcessoPericiaDAO processoPericiaDAO;
	
	@Override
	protected BaseDAO<ProcessoPericia> getDAO() {
		return processoPericiaDAO;
	}
	
	/**
	 * @deprecated Utilize a classe br.jus.cnj.pje.webservice.PericiaServiceImpl
	 */
	@Deprecated
	public List<ProcessoPericia> recuperarPericias(PessoaPerito perito, List<PericiaStatusEnum> status, Date data) {
		return processoPericiaDAO.recuperarPericias(perito, status, data);
	}
	
	/**
	 * @deprecated Utilize a classe br.jus.cnj.pje.webservice.PericiaServiceImpl
	 */
	@Deprecated
	public ProcessoPericia designarPericia(ProcessoTrf processoTrf, Especialidade especialidade, 
			PessoaPerito perito, Double valor, Date dataMarcacao) throws PJeBusinessException {
		
		if (processoTrf == null || especialidade == null || perito == null || dataMarcacao == null) {
			throw new IllegalArgumentException("Argumentos inválidos.");
		}
		
 		ProcessoPericia pericia = new ProcessoPericia();
 		pericia.setProcessoTrf(processoTrf);
 		pericia.setEspecialidade(especialidade);
 		pericia.setPessoaPerito(perito);
 		pericia.setPessoaMarcador(Authenticator.getPessoaLogada());
 		pericia.setStatus(PericiaStatusEnum.M);
 		pericia.setHoraMarcada(new Time(dataMarcacao.getTime()));
 		pericia.setDataMarcacao(DateUtil.getDataSemHora(dataMarcacao));
 		pericia.setValorPericia(valor);
 		
 		processoTrf.getProcessoPericiaList().add(pericia);
 		
 		this.persistAndFlush(pericia);
 		
 		return pericia;
	}

}
