package com.tuckersoft.branchengine.dto.Playthrough;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PlaythroughRequestDTO {

    @NotBlank(message = "playerTag is required")
    @Size(min = 2, max = 40, message = "playerTag must contain between 2 and 40 characters")
    private String playerTag;

    @NotBlank(message = "startNodeCode is required")
    private String startNodeCode;

    public PlaythroughRequestDTO() {
    }

    public String getPlayerTag() {
        return playerTag;
    }

    public void setPlayerTag(String playerTag) {
        this.playerTag = playerTag;
    }

    public String getStartNodeCode() {
        return startNodeCode;
    }

    public void setStartNodeCode(String startNodeCode) {
        this.startNodeCode = startNodeCode;
    }
}
