package org.ijsberg.iglu.access.asset;

public interface AssetAccessManager {

    void registerAsset(String assetId, String ownerUserId);

    AssetAccessSettings getAssetAccessSettings(String assetId);

    void updateAssetAccessSettings(AssetAccessSettings accessSettings);

    void deregisterAssetAccessSettings(String assetId);
}
