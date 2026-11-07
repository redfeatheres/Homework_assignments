import java.util.Objects;

public class MyHashMap<K, V> {
    private static final int INITIAL_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;
    private int size;
    private MyEntry<K, V>[] entries = new MyEntry[INITIAL_CAPACITY];

    public V get(K key) {
        Objects.requireNonNull(key, "key is null");
        MyEntry<K, V> foundEntry = findKey(key, entries);
        if (foundEntry != null) {
            return foundEntry.value;
        } else {
            return null;
        }
    }

    public boolean put(K key, V value) {
        Objects.requireNonNull(key, "key is null");
        Objects.requireNonNull(value, "value is null");
        MyEntry<K, V> foundEntry = findKey(key, entries);
        if (foundEntry != null) {
            if (foundEntry.value.equals(value)) {
                return false;
            } else {
                foundEntry.value = value;
                return true;
            }
        } else {
            ensureCapacity();
            int position = getPosition(key, entries);
            MyEntry<K, V> oldHead = entries[position];
            entries[position] = new MyEntry<>(key, value);
            entries[position].next = oldHead;
            size++;
            return true;
        }
    }

    public V remove(K key) {
        Objects.requireNonNull(key, "key is null");
        int position = getPosition(key, entries);
        MyEntry<K, V> currentEntry = entries[position];
        MyEntry<K, V> previousEntry = null;
        while (currentEntry != null) {
            if (currentEntry.key.equals(key)) {
                if (previousEntry == null) {
                    entries[position] = currentEntry.next;
                } else {
                    previousEntry.next = currentEntry.next;
                }
                size--;
                return currentEntry.value;
            }
            previousEntry = currentEntry;
            currentEntry = currentEntry.next;
        }
        return null;
    }

    public int size() {
        return size;
    }

    private MyEntry<K, V> findKey(K key, MyEntry<K, V>[] entries) {
        int position = getPosition(key, entries);
        MyEntry<K, V> currentEntry = entries[position];
        while (currentEntry != null) {
            if (currentEntry.key.equals(key)) {
                return currentEntry;
            }
            currentEntry = currentEntry.next;
        }
        return null;
    }

    private int getPosition(K key, MyEntry<K, V>[] entries) {
        return Math.floorMod(key.hashCode(), entries.length);
    }

    private void ensureCapacity() {
        if (size >= entries.length * LOAD_FACTOR) {
            increaseSize();
        }
    }

    private void increaseSize() {
        MyEntry<K, V>[] newEntries = new MyEntry[entries.length * 2];
        for (int i = 0; i < entries.length; i++) {
            MyEntry<K, V> current = entries[i];
            while (current != null) {
                MyEntry<K, V> next = current.next;
                int position = getPosition(current.key, newEntries);
                current.next = newEntries[position];
                newEntries[position] = current;
                current = next;
            }
        }
        entries = newEntries;
    }

    private static class MyEntry<K, V> {
        private final K key;
        private V value;
        MyEntry<K, V> next;

        public MyEntry(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }
}

