package org.ijsberg.iglu.asset.component;

import org.ijsberg.iglu.asset.AssetAccessManager;
import org.ijsberg.iglu.asset.AssetAccessSettings;
import org.ijsberg.iglu.asset.SecuredAssetData;
import org.ijsberg.iglu.access.RequestRegistry;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.persistence.json.BasicJsonPersister;

import java.util.*;

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
        return assetId != null ? getSettingsByAssetId(assetId) : null;
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
                if(existingSettings.getOwnerUserId() == null || existingSettings.getOwnerUserId().isEmpty()) {
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
        checkIfSettingsExistForActualAssets(securedAssets, assetType);
        checkIfActualAssetsExistForSettings(securedAssets, assetType);
        //TODO cleanup deprecated assets in a later stage
    }

    private void checkIfSettingsExistForActualAssets(Collection<? extends SecuredAssetData> securedAssets, String assetType) {
        for(SecuredAssetData securedAssetData : securedAssets) {
            AssetAccessSettings settings = getAssetAccessSettings(securedAssetData.getRelatedAssetId());
            if(settings == null) {
                System.out.println(new LogEntry(Level.CRITICAL, "settings for asset " + securedAssetData.getRelatedAssetId() + " of type " + assetType + " not found"));//; Will be added"));
                //TODO add
                //registerAsset(securedAssetData.getRelatedAssetId(), assetType, securedAssetData.getName());
            } else {
                if(settings.getType() == null) {
                    System.out.println(new LogEntry(Level.CRITICAL, "type for asset " + securedAssetData.getRelatedAssetId() + " not set; Will be set to " + assetType));
                    settings.setType(assetType);
                    updateAssetAccessSettings(settings);
                }
            }
        }
    }

    private void checkIfActualAssetsExistForSettings(Collection<? extends SecuredAssetData> securedAssets, String assetType) {
        Set<String> assetIds = new HashSet<>();
        securedAssets.stream().map(SecuredAssetData::getRelatedAssetId).forEach(assetIds::add);
        for(AssetAccessSettings settings : getSettingsByType(assetType)) {
            if(!assetIds.contains(settings.getAssetId())) {
                System.out.println(new LogEntry(Level.CRITICAL, "asset settings of type " + assetType + ", with id " + settings.getAssetId() + " do not represent existing asset"));// Will be removed"));
                //TODO deregister
            }
        }
    }

    private List<AssetAccessSettings> getSettingsByType(String assetType) {
        return settingsRepository.readByField("type", assetType);
    }

    private AssetAccessSettings getSettingsByAssetId(String assetId) {
        List<AssetAccessSettings> assetAccessSettingsItems = settingsRepository.readByField("assetId", assetId);
        if(!assetAccessSettingsItems.isEmpty()) {
            return assetAccessSettingsItems.get(0);
        }
        return null;
    }
}
