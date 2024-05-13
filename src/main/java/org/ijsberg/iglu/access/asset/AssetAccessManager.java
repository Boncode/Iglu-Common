package org.ijsberg.iglu.access.asset;

import java.util.List;

public interface AssetAccessManager {

    void registerAsset(String assetId);

    void registerAsset(String assetId, Long userGroupId);

    AssetAccessSettings getAssetAccessSettings(String assetId);

    List<AssetAccessSettings> getAssetAccessSettings();

    void updateAssetAccessSettings(AssetAccessSettings accessSettings);

    void deregisterAssetAccessSettings(String assetId);
}
