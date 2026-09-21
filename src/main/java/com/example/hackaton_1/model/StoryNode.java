package com.example.hackaton_1.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import java.time.Instant;

@Entity
@Table(name = "story_node")
public class StoryNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nodeCode;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sceneText;

    @Column(nullable = false)
    private Integer branchCapacity;

    @Column(nullable = false)
    private Integer currentBranches;

    private String primaryBranchCode;

    private String glitchBranchCode;

    @Column(nullable = false)
    private Instant createdAt;

    protected StoryNode() {
    }

    public StoryNode(
            String nodeCode,
            String title,
            String sceneText,
            Integer branchCapacity,
            Integer currentBranches,
            String primaryBranchCode,
            String glitchBranchCode,
            Instant createdAt
    ) {
        this.nodeCode = nodeCode;
        this.title = title;
        this.sceneText = sceneText;
        this.branchCapacity = branchCapacity;
        this.currentBranches = currentBranches;
        this.primaryBranchCode = primaryBranchCode;
        this.glitchBranchCode = glitchBranchCode;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
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

    @OneToMany(mappedBy = "currentNode")
    private List<Playthrough> playthroughs = new ArrayList<>();

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<Playthrough> getPlaythroughs() {
        return playthroughs;
    }

    public void setPlaythroughs(List<Playthrough> playthroughs) {
        this.playthroughs = playthroughs;
    }
}