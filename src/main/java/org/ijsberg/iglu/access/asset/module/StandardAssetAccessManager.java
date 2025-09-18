package org.ijsberg.iglu.access.asset.module;

import org.ijsberg.iglu.access.asset.AssetAccessManager;
import org.ijsberg.iglu.access.asset.AssetAccessSettings;
import org.ijsberg.iglu.access.asset.SecuredAssetData;
import org.ijsberg.iglu.access.component.RequestRegistry;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.persistence.json.BasicJsonPersister;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class StandardAssetAccessManager implements AssetAccessManager {

    private BasicJsonPersister<AssetAccessSettings> settingsRepository;

    public StandardAssetAccessManager() {

    }

    public void setProperties(Properties properties) {
        String dataDirPath = properties.getProperty("home") + "/data";
        settingsRepository = new BasicJsonPersister<>(
                dataDirPath, AssetAccessSettings.class
        ).withUniqueAttributeName("assetId");
    }

    private RequestRegistry requestRegistry;
    public void setRequestRegistry(RequestRegistry requestRegistry) {
        this.requestRegistry = requestRegistry;
    }

    @Override
    public void registerAsset(String assetId, String type, String name) {
        settingsRepository.create(new AssetAccessSettings(assetId, requestRegistry.getCurrentRequest().getUser().getId(), type, name));
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

    @Override
    public void sanityCheck(Collection<? extends SecuredAssetData> securedAssets, String assetType) {
        System.out.println(new LogEntry("performing sanity check for " + securedAssets.size() + " secured asset(s) of type " + assetType));
        for(SecuredAssetData securedAssetData : securedAssets) {
            AssetAccessSettings settings = getAssetAccessSettings(securedAssetData.getRelatedAssetId());
            if(settings == null) {
                System.out.println(new LogEntry(Level.CRITICAL, "settings for asset " + securedAssetData.getRelatedAssetId() + " of type " + assetType + " not found"));
            } else {
               // TODO
            }
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
