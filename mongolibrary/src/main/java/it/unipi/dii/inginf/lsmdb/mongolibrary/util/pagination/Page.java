package it.unipi.dii.inginf.lsmdb.mongolibrary.util.pagination;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Page<T> {
    List<T> content;
    int totalPages;
}
