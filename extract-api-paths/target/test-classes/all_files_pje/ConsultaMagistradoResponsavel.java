package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;


public class ConsultaMagistradoResponsavel {
	
	public class InformacoesMagistradoResponsavelDTO {
		public Integer idUsuario;
		public String nome;
		public Boolean magistradoTitular;
		public Integer idOrgaoJulgadorCargo;
		public Boolean recebeDistribuicao;
	}
	
	private final int IDX_FIELD_ID_USUARIO=0, IDX_FIELD_NOME=1, IDX_FIELD_IN_MAGISTRADO_TITULAR=2, IDX_FIELD_ID_ORGAO_JULGADOR_CARGO=3, IDX_FIELD_IN_RECEBE_DISTRIBUICAO=4;
	
	private EntityManager entityManager;
	
	public ConsultaMagistradoResponsavel(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	

	
	
	/**
	 * Método que utiliza query nativa para retornar informações do magistrado responsável 
	 * por um dado orgão julgador, seguindo a seguinte ordem de prioridade:
	 * 
	 * 1- Magistrado que receba distribuição e seja titular.
	 * 2- Magistrado que recebe distribuição
	 * 3- Magistrado que seja titular
	 * 4- Magistrado qualquer com lotação ativa .
	 * 
	 * 
	 * @param idOrgaoJulgador id do órgão julgador a ser pesquisado
	 * 
	 * @param idOrgaoJulgadorColegiado id do órgão julgador colegiado para
	 *            filtragem. Poder ser informado <code>null</code> para não
	 *            realizar a filtragem.
	 * 
	 * @param data
	 *            {@link Date} de referência. Se <code>null</code> será usada a
	 *            data atual.
	 * 
	 * @return InformacoesMagistradoResponsavelDTO dto contendo algumas informações básicas do magistrado responsável, 
	 * ou null caso não encontre nenhuma lotação de magistrado ativa para o OJ dado.
	 */
	@SuppressWarnings({ "unchecked" })
	public InformacoesMagistradoResponsavelDTO obterMagistradoResponsavel(Integer idOrgaoJulgador, Integer idOrgaoJulgadorColegiado, Date data) {
		
		if(idOrgaoJulgador == null) {
			return null;
		}
		
		String sql = obterConsultaSQL(idOrgaoJulgadorColegiado);
		Query query  = entityManager.createNativeQuery(sql);
		
		query.setParameter("idOrgaoJulgador", idOrgaoJulgador);
		query.setParameter("data", (data != null ? data : new Date()));
		
		if(idOrgaoJulgadorColegiado != null) {
			query.setParameter("idOrgaoJulgadorColegiado", idOrgaoJulgadorColegiado);
		}
		
		List<Object[]> resultListMagistradosLotados = query.getResultList();
		return obterMagistradoResponsavel(resultListMagistradosLotados);
	}
	
	
	private InformacoesMagistradoResponsavelDTO obterMagistradoResponsavel(List<Object[]> resultListMagistradosLotados){
		Object[] responsavel = null;
		
		for (Object[] record: resultListMagistradosLotados) {
			if ((Boolean) record[IDX_FIELD_IN_RECEBE_DISTRIBUICAO] && (Boolean) record[IDX_FIELD_IN_MAGISTRADO_TITULAR]) {
				responsavel = record;
				break;
			}
			
			if((Boolean) record[IDX_FIELD_IN_RECEBE_DISTRIBUICAO]){
				responsavel = record;
			}
			
			if (responsavel == null && (Boolean) record[IDX_FIELD_IN_MAGISTRADO_TITULAR]){
				responsavel = record;
			}
		}
		
		if (!resultListMagistradosLotados.isEmpty() && responsavel == null) {
			responsavel = resultListMagistradosLotados.get(0);
		}
		
		return arrayToDTO(responsavel);
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> obterMagistradosResponsaveis(Integer idOrgaoJulgador, Date data) {
		
		if(idOrgaoJulgador == null) {
			return null;
		}
		
		String sql = obterConsultaSQL(null);
		Query query  = entityManager.createNativeQuery(sql);
		
		query.setParameter("idOrgaoJulgador", idOrgaoJulgador);
		query.setParameter("data", (data != null ? data : new Date()));
		
		
		List<Object[]> resultListMagistradosLotados = query.getResultList();
		List<Integer> responsaveis = new ArrayList<>();
		
		for (Object[] record: resultListMagistradosLotados) {
			responsaveis.add(arrayToDTO(record).idUsuario);
		}
		
		return responsaveis;
	}

	
	
	private InformacoesMagistradoResponsavelDTO arrayToDTO(Object[] responsavel) {
		InformacoesMagistradoResponsavelDTO infMagDTO = null;
		
		if (responsavel!=null){
			infMagDTO = new InformacoesMagistradoResponsavelDTO();
			infMagDTO.idUsuario = (Integer)responsavel[IDX_FIELD_ID_USUARIO];
			infMagDTO.nome = (String)responsavel[IDX_FIELD_NOME];
			infMagDTO.magistradoTitular = (Boolean) responsavel[IDX_FIELD_IN_MAGISTRADO_TITULAR];
			infMagDTO.idOrgaoJulgadorCargo = (Integer) responsavel[IDX_FIELD_ID_ORGAO_JULGADOR_CARGO];
			infMagDTO.recebeDistribuicao = (Boolean) responsavel[IDX_FIELD_IN_RECEBE_DISTRIBUICAO];
		}	
		
		return infMagDTO;
	}

	private String obterConsultaSQL(Integer idOrgaoJulgadorColegiado){
		StringBuilder sql = new StringBuilder();
		
		// ATENÇÃO AO ALTERAR O RETORNO DA CONSULTA, POIS PODE HAVER CODIGO REFERENCIANDO SUA POSIÇÃO
		sql.append("select distinct ul.id_usuario, ul.ds_nome, ulms.in_magistrado_titular, ojc.id_orgao_julgador_cargo, in_recebe_distribuicao ");
		sql.append("from tb_usu_local_mgtdo_servdor ulms ");
		sql.append("inner join tb_orgao_julgador_cargo ojc on ojc.id_orgao_julgador_cargo = ulms.id_orgao_julgador_cargo ");
		sql.append("inner join tb_usuario_localizacao uloc on uloc.id_usuario_localizacao = ulms.id_usu_local_mgstrado_servidor "); 
		sql.append("inner join tb_usuario_login ul on ul.id_usuario = uloc.id_usuario ");
		sql.append("where ojc.id_orgao_julgador = :idOrgaoJulgador ");
		sql.append("and (ulms.dt_final IS NULL OR ulms.dt_final >= :data) ");

		if(idOrgaoJulgadorColegiado != null) {
			sql.append("and ulms.id_orgao_julgador_colegiado = :idOrgaoJulgadorColegiado ");
		}
		
		return sql.toString();
		
	}
		

}
