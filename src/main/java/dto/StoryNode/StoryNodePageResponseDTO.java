package dto.StoryNode;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StoryNodePageResponseDTO {

    private List<StoryNodeResponseDTO> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int size;

    public StoryNodePageResponseDTO() {
    }

    public StoryNodePageResponseDTO(
            List<StoryNodeResponseDTO> content,
            long totalElements,
            int totalPages,
            int currentPage,
            int size
    ) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.size = size;
    }
}