package org.ijsberg.iglu.usermanagement.multitenancy.component;

import org.ijsberg.iglu.access.AccessConstants;
import org.ijsberg.iglu.access.Request;
import org.ijsberg.iglu.access.User;
import org.ijsberg.iglu.access.asset.AssetAccessManager;
import org.ijsberg.iglu.access.asset.AssetAccessSettings;
import org.ijsberg.iglu.access.asset.AssetSecurityException;
import org.ijsberg.iglu.access.asset.SecuredAssetData;
import org.ijsberg.iglu.access.component.RequestRegistry;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.usermanagement.multitenancy.model.TenantAwareData;
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
                if(parameter instanceof SecuredAssetData /*&& !userIsTenantSurpassing()*/) {
                    System.out.println(new LogEntry(Level.DEBUG, "-----> SecuredAssetData input found: " + ((SecuredAssetData) parameter).getRelatedAssetId()));
                    checkAssetAccess((SecuredAssetData)parameter); //todo throw based on false
                }

                if (parameter instanceof TenantAwareData && !userIsTenantSurpassing()) {
                    System.out.println(new LogEntry("=========     MultiTenantAwareComponent : Found TenantAwareInput, tenant: " + ((TenantAwareData) parameter).getTenantId()) + " AGAINST " + getUserGroupNames());
                    if(!getUserGroupNames().contains(((TenantAwareData) parameter).getTenantId())) {
                        throw new SecurityException("user not allowed to interfere with other tenant");
                    }
                }
            }
        }
    }

    private boolean checkAssetAccess(SecuredAssetData assetData) {
        AssetAccessSettings assetAccessSettings = getAssetAccessManager().getAssetAccessSettings(assetData.getRelatedAssetId());
        User user = getRequestRegistry().getCurrentRequest().getUser();
        if(assetAccessSettings != null && user != null) {
            //asset is public
            if(assetAccessSettings.isPublicAsset()) {
                System.out.println(new LogEntry(Level.DEBUG, "Asset is public: " + assetData.getRelatedAssetId()));
                return true;
            }

            //user is owner
            if(assetAccessSettings.getOwnerUserId() != null && assetAccessSettings.getOwnerUserId().equals(user.getId())) {
                System.out.println(new LogEntry(Level.DEBUG, "Asset " + assetData.getRelatedAssetId() + " is owned by user: " + user.getId()));
                return true;
            }

            //user is member of group with access
            Set<Long> userGroups = user.getGroupIds();
            if(assetAccessSettings.getSharedUserGroupIds().stream().anyMatch(userGroups::contains)) {
                System.out.println(new LogEntry(Level.DEBUG, "Asset " + assetData.getRelatedAssetId() + " is owned by user: " + user.getId()));
                return true;
            }
        }
        return false;
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

            if(returnValue instanceof SecuredAssetData /*&& !userIsTenantSurpassing()*/) {
                System.out.println(new LogEntry(Level.DEBUG, "-----> SecuredAssetData output found: " + ((SecuredAssetData) returnValue).getRelatedAssetId()));
                checkAssetAccess((SecuredAssetData)returnValue);
            }

            // todo find solution for AnalysisPropertiesMap,
            if(returnValue instanceof Collection && !((Collection)returnValue).isEmpty() && ((Collection)returnValue).iterator().next() instanceof SecuredAssetData /*&& !userIsTenantSurpassing()*/) {
                Collection collectionReturnValue = (Collection) ReflectionSupport.instantiateClass(returnValue.getClass());
                Iterator<SecuredAssetData> iterator = ((Collection)returnValue).iterator();
                while (iterator.hasNext()) {
                    SecuredAssetData securedAssetData = iterator.next();
                    if(checkAssetAccess(securedAssetData)) {
                        collectionReturnValue.add(securedAssetData);
                    }
                }
            }

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
