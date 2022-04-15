package org.ijsberg.iglu.usermanagement.multitenancy.component;

import org.ijsberg.iglu.access.AccessConstants;
import org.ijsberg.iglu.access.Request;
import org.ijsberg.iglu.access.User;
import org.ijsberg.iglu.access.component.RequestRegistry;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.usermanagement.multitenancy.model.TenantAwareInput;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

public class MultiTenantAwareComponent extends StandardComponent {

    private RequestRegistry accessManager;

    public MultiTenantAwareComponent(Object implementation) {
        super(implementation);
    }

    public RequestRegistry getAccessManager() {
        if(this.accessManager == null) {
            System.out.println(new LogEntry("setting access manager"));
            this.accessManager = getProxyForComponentReference(RequestRegistry.class);
            if(this.accessManager == null) {
                throw new ConfigurationException("implementation " + implementation.getClass().getSimpleName() + " must have an injected reference to RequestRegistry");
            }
        }
        return accessManager;
    }


    private void checkInput(Object[] parameters) {
        if(parameters != null) {
            for (Object parameter : parameters) {
                if (parameter instanceof TenantAwareInput && !userIsAdmin()) {
                    System.out.println(new LogEntry("=========     MultiTenantAwareComponent : Found TenantAwareInput, tenant: " + ((TenantAwareInput) parameter).getTenantId()) + " AGAINST " + getUserGroupNames());
                    if(!getUserGroupNames().contains(((TenantAwareInput) parameter).getTenantId())) {
                        throw new SecurityException("user not allowed to interfere with other tenant");
                    }
                }
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] parameters)
            throws Throwable {

        checkInput(parameters);

        Object returnValue = super.invoke(proxy, method, parameters);

/*        if(returnValue != null) {
            System.out.println(new LogEntry("MTAC invoked, method: " + method.getName() + ", " + "retval: " + returnValue.getClass().getSimpleName()));
        }
*/
        //check output
        if(returnValue instanceof TenantAwareInput && !userIsAdmin()) {
            returnValue = ((TenantAwareInput)returnValue).filterOutOtherTenants(getUserGroupNames());
        }

        return returnValue;
    }

        @Override
    public Object invoke(String methodName, Object... parameters) throws InvocationTargetException, NoSuchMethodException, IllegalArgumentException {

        //check input
        checkInput(parameters);

        Object returnValue = super.invoke(methodName, parameters);

        //check output
        if(returnValue instanceof TenantAwareInput && !userIsAdmin()) {
            returnValue = ((TenantAwareInput)returnValue).filterOutOtherTenants(getUserGroupNames());
        }

        return returnValue;
    }

    private boolean userIsAdmin() {
        Request request = getAccessManager().getCurrentRequest();
        if(request != null) {
            User user = request.getUser();
            if(user != null) {
                return user.hasRole(AccessConstants.ADMIN_ROLE_NAME);
            }
        }
        return false;
    }

    public Set<String> getUserGroupNames() {
        Request request = getAccessManager().getCurrentRequest();
        if(request != null) {
            User user = request.getUser();
            if(user != null) {
                return user.getGroupNames();
            }
        }
        return Collections.EMPTY_SET;
    }

}
