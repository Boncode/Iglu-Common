package org.ijsberg.iglu.usermanagement.multitenancy.component;

import org.ijsberg.iglu.access.AccessConstants;
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

import static org.ijsberg.iglu.access.Permissions.TENANT_SURPASSING;

public class MultiTenantAwareComponent extends StandardComponent {

    private RequestRegistry requestRegistry;
    private AssetAccessManager assetAccessManager;

    public MultiTenantAwareComponent(Object implementation) {
        super(implementation);
        System.out.println(new LogEntry("implementation " + implementation + " is MultiTenantAwareComponent"));
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


    private void checkInput(Object[] parameters) {
        if(parameters != null) {
            for (Object parameter : parameters) {
                if(parameter instanceof SecuredAssetData) {
                    System.out.println(new LogEntry(Level.DEBUG, "-----> SecuredAssetData input found: " + ((SecuredAssetData) parameter).getRelatedAssetId()));
                    if(!userHasAssetAccess((SecuredAssetData)parameter)) {
                        throw new SecurityException("user not allowed access to input data");
                    }
                }
            }
        }
    }

    private boolean userHasAssetAccess(SecuredAssetData assetData) {
        System.out.println(new LogEntry(Level.DEBUG, "-----> SecuredAssetData found: " + assetData.getRelatedAssetId()));
        if(userIsTenantSurpassing()) {
            System.out.println(new LogEntry(Level.DEBUG, "User is tenant surpassing and has access."));
            return true;
        }

        if(assetData.getRelatedAssetId() != null) {
            AssetAccessSettings assetAccessSettings = getAssetAccessManager().getAssetAccessSettings(assetData.getRelatedAssetId());
            User user = getRequestRegistry().getCurrentRequest().getUser();
            if (assetAccessSettings != null && user != null) {
                if(assetAccessSettings.isPublicAsset()
                    || assetIsOwnedByUser(assetAccessSettings, user)
                    || assetIsSharedWithUser(assetAccessSettings, user)
                ) {
                    System.out.println(new LogEntry(Level.DEBUG, "User has access by means of asset access settings."));
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean assetIsSharedWithUser(AssetAccessSettings assetAccessSettings, User user) {
        Set<Long> userGroups = user.getGroupIds();
        return assetAccessSettings.getSharedUserGroupIds().stream().anyMatch(userGroups::contains);
    }

    private static boolean assetIsOwnedByUser(AssetAccessSettings assetAccessSettings, User user) {
        return assetAccessSettings.getOwnerUserId() != null && assetAccessSettings.getOwnerUserId().equals(user.getId());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
        checkInput(parameters);

        Object returnValue = super.invoke(proxy, method, parameters);

        return checkOutput(returnValue);
    }

    private Object checkOutput(Object returnValue) throws InstantiationException {
        if(returnValue != null) {
            if(returnValue instanceof SecuredAssetData) {
                if(!userHasAssetAccess((SecuredAssetData)returnValue)) {
                    return null;
                }
            }

            if(returnValue instanceof Collection && !((Collection)returnValue).isEmpty() && ((Collection)returnValue).iterator().next() instanceof SecuredAssetData) {
                return checkOutputOfCollection(returnValue);
            }
        }
        return returnValue;
    }

    private Collection checkOutputOfCollection(Object returnValue) throws InstantiationException {
        Collection collectionReturnValue = (Collection) ReflectionSupport.instantiateClass(returnValue.getClass());
        Iterator<SecuredAssetData> iterator = ((Collection) returnValue).iterator();
        while (iterator.hasNext()) {
            SecuredAssetData securedAssetData = iterator.next();
            if(userHasAssetAccess(securedAssetData)) {
                collectionReturnValue.add(securedAssetData);
            }
        }
        return collectionReturnValue;
    }

    @Override
    public Object invoke(String methodName, Object... parameters) throws InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
        checkInput(parameters);

        Object returnValue = super.invoke(methodName, parameters);

        if(returnValue != null) {
            if (returnValue instanceof SecuredAssetData) {
                if(!userHasAssetAccess((SecuredAssetData) returnValue)) {
                    return null; //todo need to do something about these nulls maybe
                }
            }
        }
        return returnValue;
    }

    private boolean userIsTenantSurpassing() {
        Request request = getRequestRegistry().getCurrentRequest();
        if(request != null) {
            User user = request.getUser();
            if(user != null) {
                return user.hasRole(AccessConstants.ADMIN_ROLE_NAME) || user.hasOneOfRights(TENANT_SURPASSING);
            }
        }
        return false;
    }

    public Set<String> getUserGroupNames() {
        Request request = getRequestRegistry().getCurrentRequest();
        if(request != null) {
            User user = request.getUser();
            if(user != null) {
                return user.getGroupNames();
            }
        }
        return Collections.EMPTY_SET;
    }
}
