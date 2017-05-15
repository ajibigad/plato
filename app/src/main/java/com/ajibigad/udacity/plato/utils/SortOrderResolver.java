package com.ajibigad.udacity.plato.utils;

/**
 * Created by Julius on 13/05/2017.
 */
public interface SortOrderResolver {

    enum SortCriteria {
        POPULARITY("popularity"),
        RATINGS("vote_average");

        private String value;

        SortCriteria(String value) {
            this.value = value;
        }

//        @Override
//        public String toString() {
//            return value;
//        }

        public String getValue() {
            return value;
        }
    }

    ;

    enum SortDirection {
        ASC("asc"),
        DESC("desc");
        private String value;

        SortDirection(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    ;

    String getSortOrderQuery(SortCriteria sortCriteria, SortDirection sortDirection);
}
