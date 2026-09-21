package dto.StoryNode;

import java.time.Instant;

public class StoryNodeResponseDTO {

    private Long id;
    private String nodeCode;
    private String title;
    private String sceneText;
    private Integer branchCapacity;
    private Integer currentBranches;
    private String primaryBranchCode;
    private String glitchBranchCode;
    private Instant createdAt;

    public StoryNodeResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSceneText() {
        return sceneText;
    }

    public void setSceneText(String sceneText) {
        this.sceneText = sceneText;
    }

    public Integer getBranchCapacity() {
        return branchCapacity;
    }

    public void setBranchCapacity(Integer branchCapacity) {
        this.branchCapacity = branchCapacity;
    }

    public Integer getCurrentBranches() {
        return currentBranches;
    }

    public void setCurrentBranches(Integer currentBranches) {
        this.currentBranches = currentBranches;
    }

    public String getPrimaryBranchCode() {
        return primaryBranchCode;
    }

    public void setPrimaryBranchCode(String primaryBranchCode) {
        this.primaryBranchCode = primaryBranchCode;
    }

    public String getGlitchBranchCode() {
        return glitchBranchCode;
    }

    public void setGlitchBranchCode(String glitchBranchCode) {
        this.glitchBranchCode = glitchBranchCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}