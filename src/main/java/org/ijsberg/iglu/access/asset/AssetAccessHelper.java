package org.ijsberg.iglu.access.asset;

import org.ijsberg.iglu.access.AccessConstants;
import org.ijsberg.iglu.access.User;

import java.util.Set;

import static org.ijsberg.iglu.access.Permissions.TENANT_SURPASSING;

public class AssetAccessHelper {

    /**
     * Checks if a user has the administrator role or permission to surpass any tenant checks
     * @param user
     * @return
     */
    public static boolean userIsTenantSurpassing(User user) {
        if(user != null) {
            return user.hasRole(AccessConstants.ADMIN_ROLE_NAME) || user.hasOneOfRights(TENANT_SURPASSING);
        }
        return false;
    }

    /**
     * Checks if a user has administrator role, directly granting access
     * @param user
     * @return
     */
    public static boolean userHasAdministratorRole(User user) {
        if(user != null) {
            return user.hasRole(AccessConstants.ADMIN_ROLE_NAME);
        }
        return false;
    }

    /**
     * Checks if given user has access to asset, by checking all aspects of asset access:
     * - public assets are always accessible
     * - if the user owns the asset
     * - if the asset is shared with a user group that the user is a member of
     * @param user
     * @param assetAccessSettings
     * @return
     */
    public static boolean userHasAssetAccessByMeansOfSettings(User user, AssetAccessSettings assetAccessSettings) {
        return assetAccessSettings.isPublicAsset()
                || assetIsOwnedByUser(user, assetAccessSettings)
                || assetIsSharedWithUser(user, assetAccessSettings)
                || assetIsSharedAndUserIsTenantSurpassing(user, assetAccessSettings);
    }

    /**
     * In the case the user isn't part of a team with which the asset is shared, the user can still be granted access
     * if the user has the permission TENANT_SURPASSING (basically, the power user).
     * @param user
     * @param assetAccessSettings
     * @return
     */
    private static boolean assetIsSharedAndUserIsTenantSurpassing(User user, AssetAccessSettings assetAccessSettings) {
        if(!assetAccessSettings.getSharedUserGroupIds().isEmpty()) {
            return userIsTenantSurpassing(user);
        }
        return false;
    }

    /**
     * Checks asset access settings against the user groups of given user.
     * @param user
     * @param assetAccessSettings
     * @return
     */
    private static boolean assetIsSharedWithUser(User user, AssetAccessSettings assetAccessSettings) {
        Set<Long> userGroups = user.getGroupIds();
        return assetAccessSettings.getSharedUserGroupIds().stream().anyMatch(userGroups::contains);
    }

    /**
     * Checks if given user is the owner of the asset
     * @param user
     * @param assetAccessSettings
     * @return
     */
    private static boolean assetIsOwnedByUser(User user, AssetAccessSettings assetAccessSettings) {
        return assetAccessSettings.getOwnerUserId() != null && assetAccessSettings.getOwnerUserId().equals(user.getId());
    }

}
