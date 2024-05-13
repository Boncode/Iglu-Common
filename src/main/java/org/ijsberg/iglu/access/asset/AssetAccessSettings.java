package org.ijsberg.iglu.access.asset;

import java.util.HashSet;
import java.util.Set;

public class AssetAccessSettings implements SecuredAssetData {

    private long id;

    private String assetId;
    private String ownerUserId;

    private boolean publicAsset;
    private Set<Long> sharedUserGroupIds = new HashSet<>();

    public AssetAccessSettings() {
        //empty constructor for entity persister
    }

    public AssetAccessSettings(String assetId) {
        this.assetId = assetId;
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

    public Set<Long> getSharedUserGroupIds() {
        return sharedUserGroupIds;
    }

    public void setSharedUserGroupIds(Set<Long> sharedUserGroupIds) {
        this.sharedUserGroupIds = sharedUserGroupIds;
    }

    public long getId() {
        return id;
    }

    @Override
    public String getRelatedAssetId() {
        return assetId;
    }

    public void setRelatedAssetId(String relatedAssetId) {
        //needed for json mapping, ignored... might want a DTO in analysis infrastructure
    }
}
