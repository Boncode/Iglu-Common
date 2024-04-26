package org.ijsberg.iglu.usermanagement.multitenancy.component;

import org.ijsberg.iglu.access.AccessConstants;
import org.ijsberg.iglu.access.Request;
import org.ijsberg.iglu.access.User;
import org.ijsberg.iglu.access.component.RequestRegistry;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.usermanagement.multitenancy.model.TenantAwareData;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static org.ijsberg.iglu.access.Permissions.TENANT_SURPASSING;

public class MultiTenantAwareComponent extends StandardComponent {

    private RequestRegistry accessManager;

    public MultiTenantAwareComponent(Object implementation) {
        super(implementation);
        System.out.println(new LogEntry("implementation " + implementation + " is MultiTenantAwareComponent"));
    }

    public RequestRegistry getAccessManager() {
        if(this.accessManager == null) {
            //System.out.println(new LogEntry("setting access manager"));
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
                if (parameter instanceof TenantAwareData && !userIsTenantSurpassing()) {
                    System.out.println(new LogEntry("=========     MultiTenantAwareComponent : Found TenantAwareInput, tenant: " + ((TenantAwareData) parameter).getTenantId()) + " AGAINST " + getUserGroupNames());
                    if(!getUserGroupNames().contains(((TenantAwareData) parameter).getTenantId())) {
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
        if(returnValue != null) {
            if (returnValue instanceof TenantAwareData && !userIsTenantSurpassing()) {
                returnValue = ((TenantAwareData) returnValue).filterOutOtherTenants(getUserGroupNames());
            }
//            if (returnValue instanceof Collection) {
//                System.out.println(returnValue);
//            }
            if (returnValue instanceof Collection && !((Collection)returnValue).isEmpty() && ((Collection)returnValue).iterator().next() instanceof TenantAwareData && !userIsTenantSurpassing()) {
                Collection collectionReturnValue = (Collection) ReflectionSupport.instantiateClass(returnValue.getClass());

                if(!((Collection)returnValue).isEmpty() && ((Collection)returnValue).iterator().next() instanceof TenantAwareData) {
                    Iterator<TenantAwareData> iterator = ((Collection)returnValue).iterator();
                    while (iterator.hasNext()){
                        TenantAwareData input = iterator.next();
                        Object filteredInput = input.filterOutOtherTenants(getUserGroupNames());
                        if(filteredInput != null) {
                            collectionReturnValue.add(filteredInput);
                        }
                    }
                }
                return collectionReturnValue;
            }
        }

        return returnValue;
    }

        @Override
    public Object invoke(String methodName, Object... parameters) throws InvocationTargetException, NoSuchMethodException, IllegalArgumentException {

        //check input
        checkInput(parameters);

        Object returnValue = super.invoke(methodName, parameters);

        //check output
        if(returnValue instanceof TenantAwareData && !userIsTenantSurpassing()) {
            returnValue = ((TenantAwareData)returnValue).filterOutOtherTenants(getUserGroupNames());
        }

        return returnValue;
    }

    private boolean userIsTenantSurpassing() {
        Request request = getAccessManager().getCurrentRequest();
        if(request != null) {
            User user = request.getUser();
            if(user != null) {
                return user.hasRole(AccessConstants.ADMIN_ROLE_NAME) || user.hasOneOfRights(TENANT_SURPASSING);
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
