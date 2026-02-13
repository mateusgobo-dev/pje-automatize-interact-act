package br.com.itx.test.util;

import junit.framework.Assert;

import org.junit.Test;

import br.com.infox.access.home.PapelHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

public class EntityUtilTest {

	@Test
	public void isEntityTest() {
		UsuarioLogin login = new UsuarioLogin();
		Assert.assertTrue(EntityUtil.isEntity(login));
		Assert.assertFalse(EntityUtil.isEntity(new Object()));
	}

	@Test
	public void getEntityIdObjectTest() {
		UsuarioLogin login = new UsuarioLogin();
		int idUsuario = 10;
		login.setIdUsuario(idUsuario);
		Assert.assertEquals(idUsuario, EntityUtil.getEntityIdObject(login));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getParameterizedTypeClassTest() {
		Class parameterizedTypeClass = EntityUtil.getParameterizedTypeClass(PapelHome.class);
		Assert.assertEquals(Papel.class, parameterizedTypeClass);
	}

	@Test
	public void cloneEntityTest() throws InstantiationException, IllegalAccessException {
		UsuarioLogin login = new UsuarioLogin();
		login.setNome("Teste");
		Papel papel = new Papel();
		papel.setIdentificador("ident_papel");
		login.getPapelSet().add(papel);
		UsuarioLogin clone1 = EntityUtil.cloneEntity(login, false);
		Assert.assertEquals(clone1.getNome(), login.getNome());
		Assert.assertEquals(0, clone1.getPapelSet().size());
		UsuarioLogin clone2 = EntityUtil.cloneEntity(login, true);
		Assert.assertEquals(1, clone2.getPapelSet().size());
	}

}
