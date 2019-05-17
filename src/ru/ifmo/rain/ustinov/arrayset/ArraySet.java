package ru.ifmo.rain.ustinov.arrayset;

import java.util.*;

class DescendingList<E> extends AbstractList<E> implements RandomAccess {
    private List<E> list;

    DescendingList(List<E> list) {
        this.list = list;
    }

    @Override
    public E get(int index) {
        return list.get(list.size() - index - 1);
    }

    @Override
    public int size() {
        return list.size();
    }
}

@SuppressWarnings("unused")
public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private List<E> array;
    private DescendingList<E> descendingArray;
    private Comparator<E> comparator;

    private boolean isSorted(Collection<E> collection, Comparator<E> comparator) {
        Iterator<E> f = collection.iterator();
        Iterator<E> s = collection.iterator();
        if (!s.hasNext()) return true;
        s.next();
        while (s.hasNext()) {
            if (compare(f.next(), s.next()) >= 0) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    public ArraySet() {
        array = Collections.emptyList();
        descendingArray = new DescendingList<>(array);
        comparator = null;
    }

    @SuppressWarnings("unused")
    public ArraySet(Collection<E> collection, Comparator<E> comparator) {
        this.comparator = comparator;
        if (isSorted(collection, comparator)) {
            array = new ArrayList<>(collection);
            descendingArray = new DescendingList<>(array);
        } else {
            SortedSet<E> tmpSet = new TreeSet<>(comparator);
            tmpSet.addAll(collection);
            array = new ArrayList<>(tmpSet);
            descendingArray = new DescendingList<>(array);
        }
    }

    private ArraySet(List<E> list, Comparator<E> comparator) {
        array = list;
        this.comparator = comparator;
        descendingArray = new DescendingList<>(array);
    }

    @SuppressWarnings("unused")
    public ArraySet(Collection<E> collection) {
        this(collection,null);
    }

    private int binarySearch(E e, boolean equal, boolean higher) {
        int index = Collections.binarySearch(array, e, comparator);
        if (index >= 0 && equal) {
            return index;
        }
        if (higher) {
            if (index >= 0) {
                return index + 1;
            } else return -index - 1;
        } else {
            if (index >= 0) {
                return index - 1;
            } else return -index - 2;
        }
    }

    @Override
    public E lower(E e) {
        int index = binarySearch(e, false, false);
        return index < 0 ? null : array.get(index);
    }

    @Override
    public E floor(E e) {
        int index = binarySearch(e, true, false);
        return index < 0 ? null : array.get(index);
    }

    @Override
    public E ceiling(E e) {
        int index = binarySearch(e, true, true);
        return index == array.size() ? null : array.get(index);
    }

    @Override
    public E higher(E e) {
        int index = binarySearch(e, false, true);
        return index == array.size() ? null : array.get(index);
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(array).iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(descendingArray, Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingArray.iterator();
    }

    @SuppressWarnings("unchecked")
    private int compare(E first, E second) {
        if (comparator == null) {
            return ((Comparable) first).compareTo(second);
        } else return comparator.compare(first, second);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                                  E toElement, boolean toInclusive) throws IllegalArgumentException {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("first > second");
        }
        int first = binarySearch(fromElement, fromInclusive, true);
        int second = binarySearch(toElement, toInclusive, false);
        if (first > second || first == -1 || second == -1) {
            return new ArraySet<>(Collections.emptyList(), comparator);
        }
        return new ArraySet<>(array.subList(first, second + 1), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E fromElement, boolean inclusive) {
        try {
            return subSet(first(), true, fromElement, inclusive);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return new ArraySet<>(Collections.emptyList(), comparator);
        }
    }

    @Override
    public NavigableSet<E> tailSet(E toElement, boolean inclusive) throws IllegalArgumentException {
        try {
            return subSet(toElement, inclusive, last(), true);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return new ArraySet<>(Collections.emptyList(), comparator);
        }
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) throws IllegalArgumentException {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E fromElement) throws IllegalArgumentException {
        return headSet(fromElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E toElement) throws IllegalArgumentException {
        return tailSet(toElement, true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return array.get(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return array.get(array.size() - 1);
    }

    @Override
    public int size() {
        return array.size();
    }

    @Override
    public boolean isEmpty() {
        return array.isEmpty();
    }

    @Override
    public boolean contains(Object element) {
        return Collections.binarySearch(array, (E) element, comparator) >= 0;
    }
}
