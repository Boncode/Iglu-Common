package org.ijsberg.iglu.access.asset;

import java.util.Collection;
import java.util.List;

public interface AssetAccessManager {

    void registerAsset(String assetId, String type, String name);

    AssetAccessSettings getAssetAccessSettings(String assetId);

    List<AssetAccessSettings> getAssetAccessSettings();

    void updateAssetAccessSettings(AssetAccessSettings accessSettings);

    void deregisterAssetAccessSettings(String assetId);

    void sanityCheck(Collection<? extends SecuredAssetData> securedAssets, String assetType);
}
