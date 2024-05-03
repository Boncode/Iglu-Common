package org.ijsberg.iglu.access.asset;

import org.ijsberg.iglu.persistence.BasicEntityPersister;

import java.util.List;


public class StandardAssetAccessManager implements AssetAccessManager {

    private final BasicEntityPersister<AssetAccessSettings> settingsRepository;

    public StandardAssetAccessManager() {
        settingsRepository = new BasicEntityPersister<>("data", AssetAccessSettings.class,
            "id", "assetId", "ownerUserId", "publicAsset", "sharedUserGroupIds"
        ).withUniqueIndexOn("assetId");
    }

    @Override
    public void registerAsset(String assetId, String ownerUserId) {
        settingsRepository.create(new AssetAccessSettings(assetId, ownerUserId));
    }

    @Override
    public AssetAccessSettings getAssetAccessSettings(String assetId) {
        return getSettingsByAssetId(assetId);
    }

    @Override
    public void updateAssetAccessSettings(AssetAccessSettings accessSettings) {
        AssetAccessSettings existingSettings = settingsRepository.read(accessSettings.getId());
        if(existingSettings != null) {
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
