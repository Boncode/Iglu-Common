package org.ijsberg.iglu.access.asset;

import java.util.List;

public class AssetAccessSettings {

    private long id;

    private final String assetId;
    private String ownerUserId;

    private boolean publicAsset;
    private List<Long> sharedUserGroupIds;

    public AssetAccessSettings(String assetId, String ownerUserId) {
        this.assetId = assetId;
        this.ownerUserId = ownerUserId;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public boolean isPublicAsset() {
        return publicAsset;
    }

    public void setPublicAsset(boolean publicAsset) {
        this.publicAsset = publicAsset;
    }

    public List<Long> getSharedUserGroupIds() {
        return sharedUserGroupIds;
    }

    public void setSharedUserGroupIds(List<Long> sharedUserGroupIds) {
        this.sharedUserGroupIds = sharedUserGroupIds;
    }

    public long getId() {
        return id;
    }
}
