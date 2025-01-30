package org.ijsberg.iglu.access.asset;

import org.ijsberg.iglu.access.component.RequestRegistry;
import org.ijsberg.iglu.persistence.BasicEntityPersister;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class StandardAssetAccessManager implements AssetAccessManager {

    private final BasicEntityPersister<AssetAccessSettings> settingsRepository;

    public StandardAssetAccessManager() {
        settingsRepository = new BasicEntityPersister<>("data", AssetAccessSettings.class,
            "id", "assetId", "ownerUserId", "publicAsset", "sharedUserGroupIds", "name"
        ).withUniqueIndexOn("assetId");
    }

    private RequestRegistry requestRegistry;
    public void setRequestRegistry(RequestRegistry requestRegistry) {
        this.requestRegistry = requestRegistry;
    }

    @Override
    public void registerAsset(String assetId, String name) {
        settingsRepository.create(new AssetAccessSettings(assetId, requestRegistry.getCurrentRequest().getUser().getId(), name));
    }

    @Override
    public void registerAsset(String assetId, Long userGroupId, String name) {
        //FIXME temporary method to do initial conversion
        AssetAccessSettings assetAccessSettings = new AssetAccessSettings(assetId, name);
        if(userGroupId != null) {
            Set<Long> sharedUserGroupIds = new HashSet<>();
            sharedUserGroupIds.add(userGroupId);
            assetAccessSettings.setSharedUserGroupIds(sharedUserGroupIds);
        }
        settingsRepository.create(assetAccessSettings);
    }

    @Override
    public AssetAccessSettings getAssetAccessSettings(String assetId) {
        return getSettingsByAssetId(assetId);
    }

    @Override
    public List<AssetAccessSettings> getAssetAccessSettings() {
        return settingsRepository.readAll();
    }

    @Override
    public void updateAssetAccessSettings(AssetAccessSettings accessSettings) {
        AssetAccessSettings existingSettings = settingsRepository.read(accessSettings.getId());
        if(existingSettings != null) {
            if(existingSettings.isPublicAsset() && !accessSettings.isPublicAsset()) {
                // asset now made private
                // check shared teams reset
                accessSettings.setSharedUserGroupIds(new HashSet<>());
                // check owner set, if not, then current user is owner. Admins can always access assets.
                if(existingSettings.getOwnerUserId().isEmpty()) {
                    accessSettings.setOwnerUserId(requestRegistry.getCurrentRequest().getUser().getId());
                }
            }

            if(!existingSettings.getSharedUserGroupIds().isEmpty() && accessSettings.getSharedUserGroupIds().isEmpty()) {
                if(!accessSettings.isPublicAsset()) {
                    // asset now made private
                    if(existingSettings.getOwnerUserId() == null) {
                        accessSettings.setOwnerUserId(requestRegistry.getCurrentRequest().getUser().getId());
                    }
                }
            }

            settingsRepository.update(accessSettings);
        }
    }

    @Override
    public void deregisterAssetAccessSettings(String assetId) {
        AssetAccessSettings existingSettings = getSettingsByAssetId(assetId);
        if(existingSettings != null) {
            settingsRepository.delete(existingSettings.getId());
        }
    }

    private AssetAccessSettings getSettingsByAssetId(String assetId) {
        List<AssetAccessSettings> assetAccessSettingsItems = settingsRepository.readByField("assetId", assetId);
        if(!assetAccessSettingsItems.isEmpty()) {
            return assetAccessSettingsItems.get(0);
        }
        return null;
    }
}
