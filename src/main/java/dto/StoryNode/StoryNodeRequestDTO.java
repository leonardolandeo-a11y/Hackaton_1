package dto.StoryNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class StoryNodeRequestDTO {

    @NotBlank(message = "nodeCode is required")
    @Size(min = 3, max = 40, message = "nodeCode must contain between 3 and 40 characters")
    private String nodeCode;

    @NotBlank(message = "title is required")
    @Size(min = 3, max = 80, message = "title must contain between 3 and 80 characters")
    private String title;

    @NotBlank(message = "sceneText is required")
    @Size(min = 10, message = "sceneText must contain at least 10 characters")
    private String sceneText;

    @NotNull(message = "branchCapacity is required")
    @PositiveOrZero(message = "branchCapacity cannot be negative")
    private Integer branchCapacity;

    private String primaryBranchCode;

    private String glitchBranchCode;

    public StoryNodeRequestDTO() {
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
}