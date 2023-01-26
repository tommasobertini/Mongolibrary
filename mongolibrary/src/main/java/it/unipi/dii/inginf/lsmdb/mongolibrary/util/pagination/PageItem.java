package it.unipi.dii.inginf.lsmdb.mongolibrary.util.pagination;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageItem {

    private PageItemType pageItemType;

    private int index;

    private boolean active;

}