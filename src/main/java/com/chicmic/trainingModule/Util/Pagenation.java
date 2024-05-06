package com.chicmic.trainingModule.Util;

import java.util.List;

public class Pagenation {
    public static <T> List<T> paginate(List<T> list, int pageNumber, int pageSize) {
        pageNumber /= pageSize;
        int totalItems = list.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int fromIndex = pageNumber * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        if (fromIndex <= toIndex) {
            return list.subList(fromIndex, toIndex);
        } else {
            return List.of();
        }
    }
    public static <T> List<T> paginateWithoutPageIndexConversion(List<T> list, int pageNumber, int pageSize) {
        int totalItems = list.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int fromIndex = pageNumber * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        if (fromIndex <= toIndex) {
            return list.subList(fromIndex, toIndex);
        } else {
            return List.of();
        }
    }
}
