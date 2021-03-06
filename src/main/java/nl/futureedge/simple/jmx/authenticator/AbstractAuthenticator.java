package nl.futureedge.simple.jmx.authenticator;


import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Base authenticator implementation.
 */
class AbstractAuthenticator implements JMXAuthenticator {

    private final String name;
    private final Configuration configuration;

    /**
     * Create a new default authenticator.
     */
    AbstractAuthenticator(final String name, final Configuration configuration) {
        this.name = name;
        this.configuration = configuration;
    }

    @Override
    public final Subject authenticate(final Object credentials) {
        // Setup login context
        final LoginContext loginContext;
        try {
            loginContext =
                    AccessController.doPrivileged(
                            (PrivilegedExceptionAction<LoginContext>) () -> new LoginContext(name, null, new JMXCallbackHandler(credentials), configuration));
        } catch (final PrivilegedActionException pae) {
            throw new SecurityException("Could not create loginContext", pae);
        }

        // Perform authentication
        try {
            loginContext.login();
            final Subject subject = loginContext.getSubject();
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                subject.setReadOnly();
                return null;
            });

            return subject;
        } catch (final LoginException le) {
            throw new SecurityException(le);
        }
    }
}
