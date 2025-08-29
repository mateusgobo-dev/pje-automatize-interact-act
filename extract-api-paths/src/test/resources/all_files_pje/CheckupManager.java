package br.jus.cnj.pje.util.checkup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.util.checkup.spi.CheckupWorker;
import br.jus.cnj.pje.util.checkup.spi.ProgressBean;

@Name(CheckupManager.NAME)
@Scope(ScopeType.CONVERSATION)
public class CheckupManager implements Serializable {

	public static final String NAME = "checkupManager";
	@Logger
	private Log log;
	
	@In(create=true)
	private transient DataSource nonBinaryRepository;
	
	public void put(CheckupWorker worker, ProgressBean progressBean) throws SQLException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(progressBean);
		oos.close();
		//TODO extrair para um DAO
		//TODO melhorar essa retorno com uso do JMS, inves de usar banco para armazenar o retorno dos metodos assincronos
		Connection conn = nonBinaryRepository.getConnection();
		PreparedStatement pstmt2 = conn.prepareStatement("select count(*) from tb_progress_bean_checkups where checkup_worker_id = ?");
		pstmt2.setString(1, worker.getID());
		ResultSet rs = pstmt2.executeQuery();
		if (rs.next()){
			Long count = rs.getLong(1);
			if (count > 0) {
				PreparedStatement pstmt = conn.prepareStatement("update tb_progress_bean_checkups set ob_progress_beans = ? where checkup_worker_id = ?");
				pstmt.setString(2, worker.getID());
				pstmt.setBytes(1, baos.toByteArray());
				pstmt.executeUpdate();
				baos.close();
				conn.close();
				pstmt.close();
			} else {
				PreparedStatement pstmt = conn.prepareStatement("insert into tb_progress_bean_checkups values(?, ?)");
				pstmt.setString(1, worker.getID());
				pstmt.setBytes(2, baos.toByteArray());
				pstmt.executeUpdate();
				baos.close();
				conn.close();
				pstmt.close();
			}
		}
		pstmt2.close();
		rs.close();
	}
	
	public ProgressBean getProgressBean(CheckupWorker worker) {
		if (worker == null) {
			return null;
		}
		
		byte[] data = null;
		Connection conn = null;
		PreparedStatement pstmt = null;

		try{

			conn = nonBinaryRepository.getConnection();

			pstmt = conn.prepareStatement("select ob_progress_beans from tb_progress_bean_checkups where checkup_worker_id = ?");
			pstmt.setString(1, worker.getID());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()){
				data = rs.getBytes(1);
				
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				ObjectInputStream ois = new ObjectInputStream(bais);
				ProgressBean pB = (ProgressBean) ois.readObject();
				ois.close();
				bais.close();
				
				return pB;
			}

		} catch (Exception e){
			throw new PJeDAOException(e);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					log.error(e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.error(e);
				}
			}
		}

		return new ProgressBean();
	}

	@SuppressWarnings("rawtypes")
	@Asynchronous
	public void workAsynchronous(Class checkupWorkerClass) {
		log.info("iniciado worker " + checkupWorkerClass);
		CheckupWorker checkupWorker = (CheckupWorker) Component.getInstance(checkupWorkerClass);
		ProgressBean progressBean = getProgressBean(checkupWorker);
		
		progressBean.setIsFinished(false);
		if (progressBean.getErrors() != null) {
			progressBean.getErrors().clear();
		}
		try {
			put(checkupWorker, progressBean);
		} catch (Exception e) {
			log.error("Erro ao tentar salvar o resultado do verificador " + checkupWorkerClass.toString(), e);
		}
		try {
			Thread.sleep(((long)(Math.random()*5000))); //distribuir a carga dos checkups
		} catch (InterruptedException e) {
		}
		try {
			progressBean.setErrors(checkupWorker.work());
		} catch (Exception e) {
			log.error("Erro ao tentar executar o verificador " + checkupWorkerClass.toString(), e);
		}
		progressBean.setIsFinished(true);
		try {
			put(checkupWorker, progressBean);
		} catch (Exception e) {
			log.error("Erro ao tentar salvar o resultado do verificador " + checkupWorkerClass.toString(), e);
		}
		log.info("fim worker " + checkupWorkerClass);
	}

	public void clearMapProgress() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = nonBinaryRepository.getConnection();
			pstmt = conn.prepareStatement("delete from tb_progress_bean_checkups");
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			log.error("Erro ao deletar registros do tb_progress_bean_checkups", e);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					log.error(e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.error(e);
				}
			}
		}
	}
	
}
