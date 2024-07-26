package org.ijsberg.iglu.usermanagement.multitenancy.component;

import org.ijsberg.iglu.access.Request;
import org.ijsberg.iglu.access.User;
import org.ijsberg.iglu.access.asset.AssetAccessManager;
import org.ijsberg.iglu.access.asset.AssetAccessSettings;
import org.ijsberg.iglu.access.asset.SecuredAssetData;
import org.ijsberg.iglu.access.component.RequestRegistry;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.ijsberg.iglu.access.asset.AssetAccessHelper.*;

public class MultiTenantAwareComponent extends StandardComponent {

    private RequestRegistry requestRegistry;
    private AssetAccessManager assetAccessManager;

    public MultiTenantAwareComponent(Object implementation) {
        super(implementation);
        System.out.println(new LogEntry(Level.DEBUG, "implementation " + implementation + " is MultiTenantAwareComponent"));
    }

    public RequestRegistry getRequestRegistry() {
        if(requestRegistry == null) {
            requestRegistry = getProxyForComponentReference(RequestRegistry.class);
            if(requestRegistry == null) {
                throw new ConfigurationException("implementation " + implementation.getClass().getSimpleName() + " is a " + this.getClass().getSimpleName() +  ", and must have an injected reference to RequestRegistry");
            }
        }
        return requestRegistry;
    }

    public AssetAccessManager getAssetAccessManager() {
        if(assetAccessManager == null) {
            assetAccessManager = getProxyForComponentReference(AssetAccessManager.class);
            if(assetAccessManager == null) {
                throw new ConfigurationException("implementation " + implementation.getClass().getSimpleName() + " is a " + this.getClass().getSimpleName() +  ", and must have an injected reference to AssetAccessManager");
            }
        }
        return assetAccessManager;
    }


    //todo try to merge checking input / output

    @Override
    public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
        checkInput(parameters);

        Object returnValue = super.invoke(proxy, method, parameters);

        return checkOutput(returnValue);
    }

    @Override
    public Object invoke(String methodName, Object... parameters) throws InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
        checkInput(parameters);

        Object returnValue = super.invoke(methodName, parameters);

        if(returnValue != null) {
            if (returnValue instanceof SecuredAssetData) {
                User user = getUserFromRequest();
                if(!checkUserAccessForAssetData(user, (SecuredAssetData) returnValue)) {
                    //todo need to do something about these nulls maybe?
                    // see metricDataFiltered throwing 500s after nullpointers
                    return null;
                }
            }
        }
        return returnValue;
    }

    private void checkInput(Object[] parameters) {
        if(parameters != null) {
            for (Object parameter : parameters) {
                if(parameter instanceof SecuredAssetData) {
                    System.out.println(new LogEntry(Level.VERBOSE, "-----> SecuredAssetData input found: " + ((SecuredAssetData) parameter).getRelatedAssetId()));

                    User user = getUserFromRequest();
                    if(!checkUserAccessForAssetData(user, (SecuredAssetData)parameter)) {
                        throw new SecurityException("user not allowed access to input data");
                    }
                }
            }
        }
    }

    private Object checkOutput(Object returnValue) throws InstantiationException {
        if(returnValue != null) {
            if(returnValue instanceof SecuredAssetData) {
                User user = getUserFromRequest();

                if(!checkUserAccessForAssetData(user, (SecuredAssetData)returnValue)) {
                    return null;
                }
            }

            if(returnValue instanceof Collection && !((Collection)returnValue).isEmpty() && ((Collection)returnValue).iterator().next() instanceof SecuredAssetData) {
                User user = getUserFromRequest();

                return checkOutputOfCollection(user, returnValue);
            }
        }
        return returnValue;
    }

    private Collection checkOutputOfCollection(User user, Object returnValue) throws InstantiationException {
        Collection collectionReturnValue = (Collection) ReflectionSupport.instantiateClass(returnValue.getClass());
        Iterator<SecuredAssetData> iterator = ((Collection) returnValue).iterator();
        while (iterator.hasNext()) {
            SecuredAssetData securedAssetData = iterator.next();
            if(checkUserAccessForAssetData(user, securedAssetData)) {
                collectionReturnValue.add(securedAssetData);
            }
        }
        return collectionReturnValue;
    }

    private boolean checkUserAccessForAssetData(User user, SecuredAssetData assetData) {
        System.out.println(new LogEntry(Level.VERBOSE, "-----> SecuredAssetData found: " + assetData.getRelatedAssetId()));

        if(userIsTenantSurpassing(user)) {
            System.out.println(new LogEntry(Level.VERBOSE, "User is tenant surpassing and has access."));
            return true;
        }

        if(assetData.getRelatedAssetId() != null) {
            AssetAccessSettings assetAccessSettings = getAssetAccessManager().getAssetAccessSettings(assetData.getRelatedAssetId());
            if (assetAccessSettings != null && user != null) {
                if(userHasAssetAccess(user, assetAccessSettings)) {
                    System.out.println(new LogEntry(Level.VERBOSE, "User has access by means of asset access settings."));
                    return true;
                }
            }
        }
        return false;
    }

    private User getUserFromRequest() {
        Request request = getRequestRegistry().getCurrentRequest();
        User user = null;
        if(request != null) {
            user = request.getUser();
        }
        return user;
    }
}
