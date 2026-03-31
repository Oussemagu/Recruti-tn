package tn.recruti.recruti_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> data;       // ⚠️ Changé de "content" à "data"
    private int page;
    private int limit;
    private long total;         // ⚠️ Changé de "totalElements" à "total"
    private int totalPages;
}