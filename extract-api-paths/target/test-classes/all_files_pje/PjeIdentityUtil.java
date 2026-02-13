package br.jus.cnj.pje.util;

import java.security.Principal;
import java.util.Enumeration;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.SimpleGroup;

@Name(value = PjeIdentityUtil.NAME)
@Scope(ScopeType.EVENT)
public class PjeIdentityUtil {

	public static final String NAME = "pjeIdentityUtil";

	public void removeRoles() {
		Set<SimpleGroup> groups = Identity.instance().getSubject().getPrincipals(SimpleGroup.class);

		for (SimpleGroup group : groups) {
			if (Identity.ROLES_GROUP.equals(group.getName())) {
				Enumeration<?> members = group.members();

				while (members.hasMoreElements()) {
					Principal member = (Principal) members.nextElement();
					group.removeMember(member);
				}
			}
		}
	}
}