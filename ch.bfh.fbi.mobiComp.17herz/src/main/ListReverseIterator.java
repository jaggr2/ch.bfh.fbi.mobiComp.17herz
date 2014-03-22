package main;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Copyright 2014 blastbeat syndicate gmbh
 * Author: Roger Jaggi <roger.jaggi@blastbeatsyndicate.com>
 * Date: 14.03.14
 * Time: 11:19
 */
class ListReverseIterator<T> implements Iterable<T> {
    private ListIterator<T> listIterator;

    public ListReverseIterator(List<T> wrappedList) {
        this.listIterator = wrappedList.listIterator(wrappedList.size());
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            public boolean hasNext() {
                return listIterator.hasPrevious();
            }

            public T next() {
                return listIterator.previous();
            }

            public void remove() {
                listIterator.remove();
            }

        };
    }

}