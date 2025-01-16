package org.ijsberg.iglu.access.asset;

import java.util.HashSet;
import java.util.Set;

public class AssetAccessSettings implements SecuredAssetData {

    private long id;

    private String assetId;
    private String ownerUserId;

    private String name;

    private boolean publicAsset;
    private Set<Long> sharedUserGroupIds = new HashSet<>();

    public AssetAccessSettings() {
        //empty constructor for entity persister
    }

    public AssetAccessSettings(String assetId, String ownerUserId, String name) {
        this.assetId = assetId;
        this.ownerUserId = ownerUserId;
        this.name = name;
    }

    // todo remove after conversion
    public AssetAccessSettings(String assetId, String name) {
        this.assetId = assetId;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getRelatedAssetId() {
        return assetId;
    }

    public void setRelatedAssetId(String relatedAssetId) {
        //todo needed for json mapping, ignored... might want a DTO in analysis infrastructure
    }
}
