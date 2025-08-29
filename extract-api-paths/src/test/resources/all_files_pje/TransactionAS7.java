package org.jboss.seam.transaction;

import static org.jboss.seam.annotations.Install.APPLICATION;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Naming;

 

@Name("org.jboss.seam.transaction.transaction")

@Scope(ScopeType.EVENT)

@Install(precedence = APPLICATION) // overrides Seam default component of BUILT_IN

@BypassInterceptors

public class TransactionAS7 extends Transaction {

 

    @Override

    protected UserTransaction getUserTransaction() throws NamingException {

        final InitialContext context = Naming.getInitialContext();

 

        try {

          return (UserTransaction) context.lookup("java:comp/UserTransaction");

        } catch (final NamingException ne) {

            try {

                // JBoss AS7 (with patch from https://issues.jboss.org/browse/AS7-1358)

                return (UserTransaction) context.lookup("java:jboss/UserTransaction");

            } catch (final Exception cause) {

                // ignore this so we let the code carry on to try the final JNDI name

            }

 

            try {

                // Embedded JBoss has no java:comp/UserTransaction

                final UserTransaction ut = (UserTransaction) context.lookup("UserTransaction");

                ut.getStatus(); // for glassfish, which can return an unusable UT

                return ut;

            } catch (final Exception e) {

                throw ne;

            }

        }

    }

}